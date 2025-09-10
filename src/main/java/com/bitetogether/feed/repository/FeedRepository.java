package com.bitetogether.feed.repository;

import com.bitetogether.feed.model.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface FeedRepository extends JpaRepository<Feed, Long> {
}
