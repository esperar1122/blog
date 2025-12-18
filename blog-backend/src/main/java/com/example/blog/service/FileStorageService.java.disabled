package com.example.blog.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Upload file to storage
     *
     * @param file   The file to upload
     * @param type   The type of file (e.g., "image", "avatar", "document")
     * @param userId The user ID who is uploading the file
     * @return The file URL
     */
    String uploadFile(MultipartFile file, String type, Long userId);

    /**
     * Delete file from storage
     *
     * @param fileUrl The file URL to delete
     * @param userId  The user ID who is deleting the file
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteFile(String fileUrl, Long userId);

    /**
     * Check if file exists
     *
     * @param fileUrl The file URL to check
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String fileUrl);

    /**
     * Generate a unique file name
     *
     * @param originalFilename The original file name
     * @param userId           The user ID
     * @return A unique file name
     */
    String generateUniqueFileName(String originalFilename, Long userId);
}