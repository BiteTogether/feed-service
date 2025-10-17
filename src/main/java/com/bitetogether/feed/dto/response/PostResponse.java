package com.bitetogether.feed.dto.response;

import com.bitetogether.feed.dto.UserDTO;
import lombok.Data;

@Data
public class PostResponse extends BaseResponse {
  private String id;
  private Long placeId;
  private String content;
  private Integer rating;
  private String photoUrl;
  private Integer likeCount;
  private Integer commentCount;
  private UserDTO user;
}
