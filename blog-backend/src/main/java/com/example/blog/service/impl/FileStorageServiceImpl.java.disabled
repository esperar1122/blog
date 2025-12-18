package com.example.blog.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.example.blog.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${aliyun.oss.endpoint:}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId:}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret:}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName:}")
    private String bucketName;

    @Value("${aliyun.oss.domainName:}")
    private String domainName;

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    private OSS getOSSClient() {
        if (endpoint == null || endpoint.isEmpty() ||
            accessKeyId == null || accessKeyId.isEmpty() ||
            accessKeySecret == null || accessKeySecret.isEmpty() ||
            bucketName == null || bucketName.isEmpty()) {

            log.warn("OSS configuration not found, using local storage simulation");
            return null;
        }

        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    @Override
    public String uploadFile(MultipartFile file, String type, Long userId) {
        try {
            String uniqueFileName = generateUniqueFileName(file.getOriginalFilename(), userId);
            String objectKey = buildObjectKey(type, uniqueFileName);

            OSS ossClient = getOSSClient();

            if (ossClient != null) {
                // Upload to Alibaba Cloud OSS
                return uploadToOSS(ossClient, file, objectKey);
            } else {
                // Fallback to local storage simulation (for development)
                return uploadToLocal(file, objectKey);
            }

        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String fileUrl, Long userId) {
        try {
            OSS ossClient = getOSSClient();

            if (ossClient != null && fileUrl.contains(bucketName)) {
                // Delete from OSS
                String objectKey = extractObjectKeyFromUrl(fileUrl);
                if (objectKey != null) {
                    ossClient.deleteObject(bucketName, objectKey);
                    ossClient.shutdown();
                    return true;
                }
            } else {
                // Simulate local deletion
                log.info("Simulating local file deletion for URL: {}", fileUrl);
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("File delete failed", e);
            return false;
        }
    }

    @Override
    public boolean fileExists(String fileUrl) {
        try {
            OSS ossClient = getOSSClient();

            if (ossClient != null && fileUrl.contains(bucketName)) {
                String objectKey = extractObjectKeyFromUrl(fileUrl);
                if (objectKey != null) {
                    boolean exists = ossClient.doesObjectExist(bucketName, objectKey);
                    ossClient.shutdown();
                    return exists;
                }
            } else {
                // Simulate local file existence check
                return fileUrl != null && !fileUrl.isEmpty();
            }

            return false;

        } catch (Exception e) {
            log.error("File existence check failed", e);
            return false;
        }
    }

    @Override
    public String generateUniqueFileName(String originalFilename, Long userId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String fileExtension = getFileExtension(originalFilename);

        return String.format("%s_%s_%s%s", userId, timestamp, uuid, fileExtension);
    }

    private String uploadToOSS(OSS ossClient, MultipartFile file, String objectKey) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        ossClient.putObject(bucketName, objectKey, file.getInputStream(), metadata);
        ossClient.shutdown();

        // Build file URL
        if (domainName != null && !domainName.isEmpty()) {
            return domainName + "/" + objectKey;
        } else {
            return "https://" + bucketName + "." + endpoint + "/" + objectKey;
        }
    }

    private String uploadToLocal(MultipartFile file, String objectKey) {
        // Simulate local upload - in production, this would save to local filesystem
        String simulatedUrl = "http://localhost:8080/uploads/" + objectKey;
        log.info("Simulating local upload. File would be saved at: {}", objectKey);
        log.info("Simulated URL: {}", simulatedUrl);
        return simulatedUrl;
    }

    private String buildObjectKey(String type, String fileName) {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("%s/%s/%s", uploadPath, type, datePath, fileName);
    }

    private String extractObjectKeyFromUrl(String fileUrl) {
        try {
            if (fileUrl.contains(bucketName)) {
                int startIndex = fileUrl.indexOf(bucketName) + bucketName.length() + 1;
                if (startIndex < fileUrl.length()) {
                    return fileUrl.substring(startIndex);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to extract object key from URL: {}", fileUrl, e);
            return null;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}