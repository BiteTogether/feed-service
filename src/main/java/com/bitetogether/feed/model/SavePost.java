package com.bitetogether.feed.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "saved_posts")
@CompoundIndexes({
  @CompoundIndex(name = "uniq_user_post_save", def = "{'user_id': 1, 'post_id': 1}", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavePost extends BaseModel {
  @Id private String id;

  @Indexed
  @Field("user_id")
  private Long userId;

  @Indexed
  @Field("post_id")
  private String postId;
}
