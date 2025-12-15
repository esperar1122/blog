package com.example.blog.controller;

import com.example.blog.common.Result;
import com.example.blog.service.FileStorageService;
import com.example.blog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final JwtUtil jwtUtil;

    @Value("${file.upload.max-size:10485760}") // Default 10MB
    private long maxFileSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_FILE_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    @PostMapping
    public Result<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "image") String type,
            HttpServletRequest request) {

        try {
            // Validate JWT token
            String authorization = request.getHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return Result.error("请先登录");
            }
            String token = authorization.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            // Validate file
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            if (file.getSize() > maxFileSize) {
                return Result.error("文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB");
            }

            String contentType = file.getContentType();
            if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
                return Result.error("不支持的文件类型，仅支持：jpg, png, gif, webp");
            }

            // Validate file extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return Result.error("文件名不能为空");
            }

            String fileExtension = getFileExtension(originalFilename).toLowerCase();
            if (!ALLOWED_FILE_EXTENSIONS.contains(fileExtension)) {
                return Result.error("不支持的文件扩展名，仅支持：jpg, png, gif, webp");
            }

            // Upload file
            String fileUrl = fileStorageService.uploadFile(file, type, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("filename", originalFilename);
            response.put("size", file.getSize());
            response.put("type", contentType);

            return Result.success("文件上传成功", response);

        } catch (Exception e) {
            log.error("File upload failed", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch")
    public Result<Map<String, Object>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "type", defaultValue = "image") String type,
            HttpServletRequest request) {

        try {
            // Validate JWT token
            String authorization = request.getHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return Result.error("请先登录");
            }
            String token = authorization.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            if (files.length == 0) {
                return Result.error("请选择要上传的文件");
            }

            if (files.length > 10) {
                return Result.error("一次最多只能上传10个文件");
            }

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> success = new HashMap<>();
            Map<String, Object> failed = new HashMap<>();

            int successCount = 0;
            int failedCount = 0;

            for (MultipartFile file : files) {
                try {
                    if (file.isEmpty()) {
                        failed.put(file.getOriginalFilename(), "文件为空");
                        failedCount++;
                        continue;
                    }

                    if (file.getSize() > maxFileSize) {
                        failed.put(file.getOriginalFilename(), "文件大小超过限制");
                        failedCount++;
                        continue;
                    }

                    String contentType = file.getContentType();
                    if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
                        failed.put(file.getOriginalFilename(), "不支持的文件类型");
                        failedCount++;
                        continue;
                    }

                    String fileExtension = getFileExtension(file.getOriginalFilename()).toLowerCase();
                    if (!ALLOWED_FILE_EXTENSIONS.contains(fileExtension)) {
                        failed.put(file.getOriginalFilename(), "不支持的文件扩展名");
                        failedCount++;
                        continue;
                    }

                    String fileUrl = fileStorageService.uploadFile(file, type, userId);
                    success.put(file.getOriginalFilename(), fileUrl);
                    successCount++;

                } catch (Exception e) {
                    failed.put(file.getOriginalFilename(), e.getMessage());
                    failedCount++;
                }
            }

            response.put("success", success);
            response.put("failed", failed);
            response.put("successCount", successCount);
            response.put("failedCount", failedCount);

            if (failedCount == 0) {
                return Result.success("所有文件上传成功", response);
            } else if (successCount == 0) {
                return Result.error("所有文件上传失败", response);
            } else {
                return Result.success("部分文件上传成功", response);
            }

        } catch (Exception e) {
            log.error("Batch file upload failed", e);
            return Result.error("批量上传失败：" + e.getMessage());
        }
    }

    @DeleteMapping
    public Result<Void> deleteFile(
            @RequestParam("url") String fileUrl,
            HttpServletRequest request) {

        try {
            // Validate JWT token
            String authorization = request.getHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return Result.error("请先登录");
            }
            String token = authorization.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            boolean deleted = fileStorageService.deleteFile(fileUrl, userId);

            if (deleted) {
                return Result.success("文件删除成功");
            } else {
                return Result.error("文件删除失败");
            }

        } catch (Exception e) {
            log.error("File delete failed", e);
            return Result.error("文件删除失败：" + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}