package com.petbuddy.petbuddystore.controller;

import com.petbuddy.petbuddystore.common.response.ApiResponse;
import com.petbuddy.petbuddystore.dto.response.FileUploadResponse;
import com.petbuddy.petbuddystore.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {

    //=============================================//
    //AWS_S3_BUCKET=petbuddy-images-516232034948 (vo .env nha may bro)
    //=============================================//

    FileService fileService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadImage(
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Upload image successfully",
                        fileService.uploadImage(file)
                )
        );
    }
}