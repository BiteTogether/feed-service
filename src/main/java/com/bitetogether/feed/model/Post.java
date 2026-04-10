package com.bitetogether.feed.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseModel {
  @Id private String id;

  @Field("user_id")
  private Long userId;

  @Field("place_id")
  private Long placeId;

  @Field("latitude")
  private Double latitude;

  @Field("longitude")
  private Double longitude;

  @Field("content")
  private String content;

  @Field("rating")
  private Integer rating;

  @Field("photo_url")
  private String photoUrl;

  @Field("like_count")
  private Integer likeCount = 0;

  @Field("comment_count")
  private Integer commentCount = 0;
}
