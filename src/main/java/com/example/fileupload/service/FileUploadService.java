package com.example.fileupload.service;

import com.example.fileupload.dto.FileUploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Service
public class FileUploadService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${aws.access.key.id}")
    private String accessKey;

    @Value("${aws.secret.access.key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Override
    public FileUploadResponse upload(String fileId, MultipartFile file) {
        String trimmedFileId = fileId == null ? "" : fileId.trim();
        if (trimmedFileId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File ID is required");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }

        String fileName = StringUtils.cleanPath(
                StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "unknown");

        log.info("Received upload request - fileId: {}, fileName: {}, size: {} bytes, contentType: {}",
                trimmedFileId, fileName, file.getSize(), file.getContentType());

        String s3Key = "test-1905/" + fileName;

        try {
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putOb, RequestBody.fromBytes(file.getBytes()));
            
            log.info("Successfully uploaded {} to S3 bucket {} at key {}", fileName, bucketName, s3Key);
        } catch (IOException e) {
            log.error("Failed to read file for S3 upload", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read file");
        } catch (Exception e) {
            log.error("Failed to upload to S3", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload to S3: " + e.getMessage());
        }

        return new FileUploadResponse(
                trimmedFileId,
                fileName,
                file.getSize(),
                "UPLOADED",
                "File successfully uploaded to S3 in folder test-1905");
    }
}
