package com.example.fileupload.service;

import com.example.fileupload.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Storage abstraction so an S3-backed implementation can replace the current one.
 */
public interface FileStorageService {

    FileUploadResponse upload(String fileId, MultipartFile file);
}
