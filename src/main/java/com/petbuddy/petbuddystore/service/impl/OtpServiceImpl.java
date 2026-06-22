package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.service.OtpService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpServiceImpl implements OtpService {
    PasswordEncoder passwordEncoder;
    DynamoDbClient dynamoDbClient;
    SecureRandom secureRandom = new SecureRandom();

    @NonFinal
    @Value("${otp.table}")
    String tableName;

    @NonFinal
    @Value("${otp.length}")
    int OTP_LENGTH;

    @NonFinal
    @Value("${otp.expiry.duration}")
    int OTP_EXPIRATION;

    @NonFinal
    @Value("${otp.max.attempts}")
    int MAX_ATTEMPTS;

    @NonFinal
    @Value("${otp.block.duration}")
    int BLOCK_DURATION;

    @NonFinal
    @Value("${otp.resend.cooldown}")
    int RESEND_COOLDOWN;

    @NonFinal
    @Value("${otp.rate.limit}")
    int RATE_LIMIT;

    private long now() {
        return Instant.now().getEpochSecond();
    }

    private String generateOtpCode(int length) {
        int bound = (int) Math.pow(10, length);
        return String.format("%0" + length + "d", secureRandom.nextInt(bound));
    }

    private Map<String, AttributeValue> getItem(String email) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("email", AttributeValue.fromS(email)))
                .build();

        return dynamoDbClient.getItem(request).item();
    }

    private void putItem(Map<String, AttributeValue> item) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build());
    }

    private void deleteItem(String email) {
        dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("email", AttributeValue.fromS(email)))
                .build());
    }

    private void checkRateLimit(Map<String, AttributeValue> item, String email) {
        long now = now();
        int count = 0;
        long reset = now + 3600;

        if (item != null && !item.isEmpty()) {
            count = Integer.parseInt(item.getOrDefault("rateLimitCount", AttributeValue.fromN("0")).n());
            reset = Long.parseLong(item.getOrDefault("rateLimitResetAt", AttributeValue.fromN(String.valueOf(now + 3600))).n());

            if (now > reset) {
                count = 0;
                reset = now + 3600;
            }
        }
        count++;

        if (count > RATE_LIMIT) {
            throw new AppException(ErrorCode.OTP_RATE_LIMIT);
        }
    }

    @Override
    public String generateOtp(String email) {
        Map<String, AttributeValue> item = getItem(email);
        checkRateLimit(item, email);
        long now = now();

        if (item != null && !item.isEmpty()) {
            long cooldown = Long.parseLong(item.getOrDefault("resendCooldownUntil", AttributeValue.fromN("0")).n());
            long blockUntil = Long.parseLong(item.getOrDefault("blockUntil", AttributeValue.fromN("0")).n());

            if (now < cooldown) {
                throw new AppException(ErrorCode.OTP_RESEND_COOLDOWN);
            }

            if (now < blockUntil) {
                throw new AppException(ErrorCode.OTP_MAX_ATTEMPT);
            }
        }

        String otp = generateOtpCode(OTP_LENGTH);
        String hash = passwordEncoder.encode(otp);

        Map<String, AttributeValue> newItem = new HashMap<>();
        newItem.put("email", AttributeValue.fromS(email));
        newItem.put("otpHash", AttributeValue.fromS(hash));
        newItem.put("attempts", AttributeValue.fromN("0"));
        newItem.put("expireAt", AttributeValue.fromN(String.valueOf(now + OTP_EXPIRATION)));
        newItem.put("resendCooldownUntil", AttributeValue.fromN(String.valueOf(now + RESEND_COOLDOWN)));

        newItem.put("rateLimitCount", AttributeValue.fromN("1"));
        newItem.put("rateLimitResetAt", AttributeValue.fromN(String.valueOf(now + 3600)));
        putItem(newItem);
        return otp;
    }

    @Override
    public void verifyOtp(String email, String otp) {
        Map<String, AttributeValue> item = getItem(email);
        if (item == null || item.isEmpty()) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        long now = now();
        long blockUntil = Long.parseLong(item.getOrDefault("blockUntil", AttributeValue.fromN("0")).n());
        if (now < blockUntil) {
            throw new AppException(ErrorCode.OTP_MAX_ATTEMPT);
        }

        String hash = item.get("otpHash").s();
        if (!passwordEncoder.matches(otp, hash)) {
            int attempts = Integer.parseInt(item.get("attempts").n()) + 1;

            if (attempts >= MAX_ATTEMPTS) {
                Map<String, AttributeValue> update = new HashMap<>(item);
                update.put("blockUntil", AttributeValue.fromN(String.valueOf(now + BLOCK_DURATION)));
                update.put("attempts", AttributeValue.fromN(String.valueOf(attempts)));
                putItem(update);

                throw new AppException(ErrorCode.OTP_MAX_ATTEMPT);
            }

            Map<String, AttributeValue> update = new HashMap<>(item);
            update.put("attempts", AttributeValue.fromN(String.valueOf(attempts)));
            putItem(update);

            throw new AppException(ErrorCode.OTP_INVALID);
        }

        deleteItem(email);

    }

    @Override
    public String resendOtp(String email) {
        return generateOtp(email);
    }
}
