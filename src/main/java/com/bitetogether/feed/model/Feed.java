package com.bitetogether.feed.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
public class Feed {
    @Id
    private Long id;
    private Long userId;
    private Long placeId;
    private String content;
    private Integer rating;
    private String photoUrl;
}
