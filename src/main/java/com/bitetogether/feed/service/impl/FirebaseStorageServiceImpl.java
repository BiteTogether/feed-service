package com.bitetogether.feed.service.impl;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.exception.AppException;
import com.bitetogether.common.util.ApiResponseUtil;
import com.bitetogether.feed.configuration.firebase.FirebaseProperties;
import com.bitetogether.feed.exception.ErrorCode;
import com.bitetogether.feed.service.inter.FirebaseStorageService;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FirebaseStorageServiceImpl implements FirebaseStorageService {

  Storage storage;
  FirebaseProperties firebaseProperties;

  private static final String[] ALLOWED_IMAGE_TYPES = {
    "image/jpeg", "image/png", "image/jpg", "image/gif", "image/webp"
  };
  private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB

  @Override
  public ApiResponseDTO<String> uploadFile(MultipartFile file) {
    validateFile(file);

    try {
      String folder = "posts";
      String fileName = generateFileName(file, folder);
      String bucketName = firebaseProperties.getStorageBucket();

      BlobId blobId = BlobId.of(bucketName, fileName);
      BlobInfo blobInfo =
          BlobInfo.newBuilder(blobId)
              .setContentType(file.getContentType())
              .setCacheControl("public, max-age=86400") // Cache for 1 day
              .build();

      // Upload file to Firebase Storage
      storage.create(blobInfo, file.getBytes());

      // Generate public URL
      String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
      String publicUrl =
          String.format(
              "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
              bucketName, encodedFileName);

      log.info("File uploaded successfully: {}", publicUrl);
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.SUCCESS, "File uploaded successfully", publicUrl);

    } catch (IOException e) {
      log.error("Error uploading file to Firebase Storage", e);
      throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
    }
  }

  @Override
  public ApiResponseDTO<String> deleteFile(String fileUrl) {
    try {
      String fileName = extractFileNameFromUrl(fileUrl);
      if (fileName.isEmpty()) {
        log.warn("Invalid file URL format: {}", fileUrl);
        throw new AppException(ErrorCode.INVALID_FILE_URL);
      }

      String bucketName = firebaseProperties.getStorageBucket();
      BlobId blobId = BlobId.of(bucketName, fileName);
      boolean deleted = storage.delete(blobId);

      if (deleted) {
        log.info("File deleted successfully: {}", fileName);
        return ApiResponseUtil.buildApiResponse(
            ApiResponseStatus.SUCCESS, "File deleted successfully", fileName);
      } else {
        log.warn("File not found for deletion: {}", fileName);
        throw new AppException(ErrorCode.FILE_NOT_FOUND);
      }

    } catch (AppException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error deleting file from Firebase Storage", e);
      throw new AppException(ErrorCode.FILE_DELETE_FAILED);
    }
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new AppException(ErrorCode.FILE_REQUIRED);
    }

    // Check file size
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new AppException(ErrorCode.FILE_SIZE_EXCEEDED);
    }

    // Check file type
    String contentType = file.getContentType();
    boolean isValidType = false;
    for (String allowedType : ALLOWED_IMAGE_TYPES) {
      if (allowedType.equals(contentType)) {
        isValidType = true;
        break;
      }
    }

    if (!isValidType) {
      throw new AppException(ErrorCode.INVALID_FILE_TYPE);
    }
  }

  private String generateFileName(MultipartFile file, String folder) {
    String originalFileName = file.getOriginalFilename();
    String extension = "";
    if (originalFileName != null && originalFileName.contains(".")) {
      extension = originalFileName.substring(originalFileName.lastIndexOf("."));
    }
    String uniqueId = UUID.randomUUID().toString();
    return folder + "/" + uniqueId + extension;
  }

  private String extractFileNameFromUrl(String fileUrl) {
    // Extract file name from Firebase Storage URL
    // Format: https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{fileName}?alt=media
    String[] parts = fileUrl.split("/o/");
    if (parts.length > 1) {
      String encodedFileName = parts[1].split("\\?")[0];
      return java.net.URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8);
    }
    return "";
  }
}
