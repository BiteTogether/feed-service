package com.bitetogether.feed.model;

import com.bitetogether.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "feeds")
public class Feed extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "place_id", nullable = false)
  private Long placeId;

  @Column(name = "content")
  private String content;

  @Column(name = "rating")
  private Integer rating;

  @Column(name = "photo_url")
  private String photoUrl;
}
