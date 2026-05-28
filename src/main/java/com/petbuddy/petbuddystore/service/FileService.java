package com.petbuddy.petbuddystore.service;

import com.petbuddy.petbuddystore.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String uploadImage(MultipartFile file);
}