package com.bitetogether.feed.repository;

import com.bitetogether.feed.model.Feed;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
  List<Feed> findAllByUserId(Long userId);
}
