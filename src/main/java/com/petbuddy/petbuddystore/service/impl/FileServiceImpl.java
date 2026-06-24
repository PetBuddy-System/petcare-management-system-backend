package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FileServiceImpl implements FileService {

    static final String PRODUCT_FOLDER = "products";
    static final String PET_FOLDER     = "pets";
    static final String BLOG_FOLDER    = "blogs";

    S3Client s3Client;

    @NonFinal
    @Value("${aws.s3.bucket}")
    String bucketName;

    @Override
    public String uploadProductImage(MultipartFile file) {
        return uploadMultipartToS3(file, PRODUCT_FOLDER);
    }

    @Override
    public String uploadPetImage(MultipartFile file) {
        return uploadMultipartToS3(file, PET_FOLDER);
    }

    @Override
    public String uploadBlogImage(MultipartFile file) {
        return uploadMultipartToS3(file, BLOG_FOLDER);
    }

    @Override
    public String uploadProductImageFromBytes(byte[] bytes, String mimeType) {
        validateImageBytes(bytes, mimeType);
        String extension = resolveExtension(mimeType);
        return uploadBytesToS3(bytes, mimeType, PRODUCT_FOLDER, extension);
    }


    private String uploadMultipartToS3(MultipartFile file, String folder) {
        validateFile(file);
        try {
            String imageKey = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(imageKey)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
            return buildUrl(imageKey);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", e.getMessage());
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }

    private String uploadBytesToS3(byte[] bytes, String mimeType, String folder, String extension) {
        try {
            String imageKey = folder + "/" + UUID.randomUUID() + "." + extension;
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(imageKey)
                            .contentType(mimeType)
                            .build(),
                    RequestBody.fromBytes(bytes)
            );
            return buildUrl(imageKey);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to upload bytes to S3: {}", e.getMessage());
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }

    private String buildUrl(String imageKey) {
        return "https://" + bucketName + ".s3.amazonaws.com/" + imageKey;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {throw new AppException(ErrorCode.FILE_REQUIRED);}
        if (file.getSize() > 5 * 1024 * 1024) {throw new AppException(ErrorCode.FILE_TOO_LARGE);}
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/jpg")
                && !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private void validateImageBytes(byte[] bytes, String mimeType) {
        if (bytes == null || bytes.length == 0) {throw new AppException(ErrorCode.FILE_REQUIRED);}
        if (bytes.length > 5 * 1024 * 1024) {throw new AppException(ErrorCode.FILE_TOO_LARGE);}
        if (mimeType == null || !mimeType.toLowerCase().startsWith("image/")) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String resolveExtension(String mimeType) {
        String lower = mimeType.toLowerCase();
        if (lower.contains("png")) return "png";
        if (lower.contains("webp")) return "webp";
        if (lower.contains("gif")) return "gif";
        if (lower.contains("bmp")) return "bmp";
        return "jpg";
    }
}