package com.bitetogether.feed.service;

import com.bitetogether.common.exception.AppException;
import com.bitetogether.feed.configuration.firebase.FirebaseProperties;
import com.bitetogether.feed.service.impl.FirebaseStorageServiceImpl;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FirebaseStorageServiceImplTest {

  @Mock private Storage storage;

  @Mock private FirebaseProperties firebaseProperties;

  @InjectMocks private FirebaseStorageServiceImpl service;

  @Test
  void uploadFile_success_returnsPublicUrl() {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[] {1, 2, 3});

    Mockito.when(firebaseProperties.getStorageBucket()).thenReturn("test-bucket");
    Mockito.when(storage.create(Mockito.any(BlobInfo.class), Mockito.any(byte[].class)))
        .thenReturn(null);

    String result = service.uploadFile(file, "posts");

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.contains("test-bucket"));
    Assertions.assertTrue(result.contains("posts"));
    Mockito.verify(storage).create(Mockito.any(BlobInfo.class), Mockito.any(byte[].class));
  }

  @Test
  void uploadFile_whenFileIsNull_throwsAppException() {
    Assertions.assertThrows(AppException.class, () -> service.uploadFile(null, "posts"));
  }

  @Test
  void uploadFile_whenFileIsEmpty_throwsAppException() {
    MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

    Assertions.assertThrows(AppException.class, () -> service.uploadFile(file, "posts"));
  }

  @Test
  void uploadFile_whenFileTooLarge_throwsAppException() {
    byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
    MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", largeContent);

    Assertions.assertThrows(AppException.class, () -> service.uploadFile(file, "posts"));
  }

  @Test
  void uploadFile_whenInvalidContentType_throwsAppException() {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[] {1, 2, 3});

    Assertions.assertThrows(AppException.class, () -> service.uploadFile(file, "posts"));
  }

  @Test
  void uploadFile_whenGenericContentType_validatesExtension_success() {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.png", "application/octet-stream", new byte[] {1, 2, 3});

    Mockito.when(firebaseProperties.getStorageBucket()).thenReturn("test-bucket");
    Mockito.when(storage.create(Mockito.any(BlobInfo.class), Mockito.any(byte[].class)))
        .thenReturn(null);

    String result = service.uploadFile(file, "posts");

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.contains("test-bucket"));
  }

  @Test
  void uploadFile_whenGenericContentType_invalidExtension_throwsAppException() {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.txt", "application/octet-stream", new byte[] {1, 2, 3});

    Assertions.assertThrows(AppException.class, () -> service.uploadFile(file, "posts"));
  }

  @Test
  void uploadFile_whenNullContentType_andValidExtension_success() {
    MockMultipartFile file = new MockMultipartFile("file", "test.gif", null, new byte[] {1, 2, 3});

    Mockito.when(firebaseProperties.getStorageBucket()).thenReturn("test-bucket");
    Mockito.when(storage.create(Mockito.any(BlobInfo.class), Mockito.any(byte[].class)))
        .thenReturn(null);

    String result = service.uploadFile(file, "posts");

    Assertions.assertNotNull(result);
  }

  @Test
  void uploadFile_whenStorageThrowsIOException_throwsAppException() throws IOException {
    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.isEmpty()).thenReturn(false);
    Mockito.when(file.getSize()).thenReturn(100L);
    Mockito.when(file.getContentType()).thenReturn("image/jpeg");
    Mockito.when(file.getOriginalFilename()).thenReturn("test.jpg");
    Mockito.when(file.getBytes()).thenThrow(new IOException("read error"));
    Mockito.when(firebaseProperties.getStorageBucket()).thenReturn("test-bucket");

    Assertions.assertThrows(AppException.class, () -> service.uploadFile(file, "posts"));
  }

  @Test
  void deleteFile_success_callsStorageDelete() {
    String fileUrl =
        "https://firebasestorage.googleapis.com/v0/b/test-bucket/o/posts%2Ffile.jpg?alt=media";

    Mockito.when(firebaseProperties.getStorageBucket()).thenReturn("test-bucket");
    Mockito.when(storage.delete(Mockito.any(BlobId.class))).thenReturn(true);

    service.deleteFile(fileUrl);

    Mockito.verify(storage).delete(Mockito.any(BlobId.class));
  }

  @Test
  void deleteFile_whenFileNotFound_doesNotThrow() {
    String fileUrl =
        "https://firebasestorage.googleapis.com/v0/b/test-bucket/o/posts%2Ffile.jpg?alt=media";

    Mockito.when(firebaseProperties.getStorageBucket()).thenReturn("test-bucket");
    Mockito.when(storage.delete(Mockito.any(BlobId.class))).thenReturn(false);

    Assertions.assertDoesNotThrow(() -> service.deleteFile(fileUrl));
  }

  @Test
  void deleteFile_whenExceptionThrown_doesNotPropagate() {
    String fileUrl =
        "https://firebasestorage.googleapis.com/v0/b/test-bucket/o/posts%2Ffile.jpg?alt=media";

    Mockito.when(firebaseProperties.getStorageBucket()).thenReturn("test-bucket");
    Mockito.when(storage.delete(Mockito.any(BlobId.class)))
        .thenThrow(new RuntimeException("storage error"));

    Assertions.assertDoesNotThrow(() -> service.deleteFile(fileUrl));
  }

  @Test
  void uploadPostImage_delegatesToUploadFileWithPostsFolder() {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.webp", "image/webp", new byte[] {1, 2, 3});

    Mockito.when(firebaseProperties.getStorageBucket()).thenReturn("test-bucket");
    Mockito.when(storage.create(Mockito.any(BlobInfo.class), Mockito.any(byte[].class)))
        .thenReturn(null);

    String result = service.uploadPostImage(file);

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.contains("posts"));
  }

  @Test
  void uploadFile_whenNoExtensionInFilename_throwsAppException() {
    MockMultipartFile file =
        new MockMultipartFile("file", "noextension", null, new byte[] {1, 2, 3});

    Assertions.assertThrows(AppException.class, () -> service.uploadFile(file, "posts"));
  }
}
