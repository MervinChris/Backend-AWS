package com.example.fileupload.dto;

public record FileUploadResponse(
        String fileId,
        String fileName,
        long size,
        String status,
        String message
) {
}
