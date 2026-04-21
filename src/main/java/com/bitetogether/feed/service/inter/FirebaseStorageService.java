package com.bitetogether.feed.service.inter;

import org.springframework.web.multipart.MultipartFile;

public interface FirebaseStorageService {
  String uploadFile(MultipartFile file, String folder);

  void deleteFile(String fileUrl);

  String uploadPostImage(MultipartFile file);
}
