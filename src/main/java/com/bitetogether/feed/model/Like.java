package com.bitetogether.feed.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "likes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Like extends BaseModel {
  @Id private String id;

  @Field("user_id")
  private Long userId;

  @Indexed
  @Field("post_id")
  private String postId;

  @Indexed
  @Field("comment_id")
  private String commentId;
}
