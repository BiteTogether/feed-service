package com.bitetogether.feed.exception;

import static com.bitetogether.common.enums.ApiResponseStatus.getDefaultMessage;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.exception.BaseErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode implements BaseErrorCode {
  // File upload errors
  FILE_REQUIRED(ApiResponseStatus.BAD_REQUEST, "File is required"),
  FILE_SIZE_EXCEEDED(
      ApiResponseStatus.BAD_REQUEST, "File size exceeds maximum allowed size of 10MB"),
  INVALID_FILE_TYPE(
      ApiResponseStatus.BAD_REQUEST,
      "Invalid file type. Only images are allowed (JPEG, PNG, GIF, WEBP)"),
  FILE_UPLOAD_FAILED(ApiResponseStatus.INTERNAL_SERVER_ERROR, "Failed to upload file to storage"),
  FILE_DELETE_FAILED(ApiResponseStatus.INTERNAL_SERVER_ERROR, "Failed to delete file from storage"),
  INVALID_FILE_URL(ApiResponseStatus.BAD_REQUEST, "Invalid file URL format"),
  FILE_NOT_FOUND(ApiResponseStatus.NOT_FOUND, "File not found in storage"),
  ;

  ApiResponseDTO<Void> response;

  ErrorCode(ApiResponseStatus status, String message) {
    this.response =
        ApiResponseDTO.<Void>builder().status(status.getCode()).message(message).data(null).build();
  }

  public String getMessage() {
    String defaultMessage = getDefaultMessage(response.getStatus());
    String message = response.getMessage();
    return message.isEmpty() ? defaultMessage : message;
  }
}
