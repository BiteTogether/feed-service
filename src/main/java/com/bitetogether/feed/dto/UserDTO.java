package com.bitetogether.feed.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserDTO {
  private Long id;
  private String username;
  private String email;
  private String fullName;
  private String phoneNumber;
  private String avatar;
  private String role;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
