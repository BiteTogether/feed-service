package com.bitetogether.feed.dto.response;

import com.bitetogether.feed.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentResponse extends BaseResponse {
  private String id;
  private String postId;
  private String content;
  private Integer likeCount;
  private UserDTO user;
}
