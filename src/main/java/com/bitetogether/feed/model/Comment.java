package com.bitetogether.feed.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends BaseModel {
  @Id private String id;

  @Field("post_id")
  private String postId;

  @Field("user_id")
  private Long userId;

  @Field("content")
  private String content;

  @Field("like_count")
  private Integer likeCount = 0;

  @Field("parent_comment_id")
  private String parentCommentId; // null for top-level comments, not null for replies
}
