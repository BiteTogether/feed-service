package com.bitetogether.feed.service.inter;

import com.bitetogether.common.dto.ApiResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FirebaseStorageService {
  ApiResponseDTO<String> uploadFile(MultipartFile file);

  ApiResponseDTO<String> deleteFile(String fileUrl);
}
