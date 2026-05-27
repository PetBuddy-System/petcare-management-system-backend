package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.dto.response.FileUploadResponse;
import com.petbuddy.petbuddystore.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE)
public class S3StorageService implements StorageService {

    final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    String bucketName;

    @Value("${aws.region}")
    String region;

    @Override
    public FileUploadResponse uploadImage(MultipartFile file) {
        validateImage(file);

        String imageKey = "products/" + UUID.randomUUID() + getFileExtension(file.getOriginalFilename());

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            String imageUrl = String.format(
                    "https://%s.s3.%s.amazonaws.com/%s",
                    bucketName,
                    region,
                    imageKey
            );

            return FileUploadResponse.builder()
                    .imageUrl(imageUrl)
                    .imageKey(imageKey)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Upload image failed");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }

        return filename.substring(filename.lastIndexOf("."));
    }
}