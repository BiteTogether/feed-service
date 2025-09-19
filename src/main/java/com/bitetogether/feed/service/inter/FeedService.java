package com.bitetogether.feed.service.inter;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.feed.dto.FeedDTO;
import com.bitetogether.feed.model.Feed;
import java.util.List;

public interface FeedService {
  ApiResponse<Feed> createFeed(FeedDTO feed);

  ApiResponse<Feed> getFeedById(Long id);

  ApiResponse<List<Feed>> getFeedByUserId(Long userId);

  ApiResponse<Feed> updateFeed(Long id, FeedDTO feedDTO);

  ApiResponse<String> deleteFeed(Long id);
}
