package com.bitetogether.feed.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendDTO {
  Long id;
  String username;
  String fullName;
  String avatar;
}
