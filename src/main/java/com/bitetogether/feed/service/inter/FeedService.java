package com.bitetogether.feed.service.inter;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.feed.dto.FeedDTO;
import com.bitetogether.feed.model.Feed;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface FeedService {
  ApiResponse<Feed> createFeed(FeedDTO feed);

  ApiResponse<Feed> getFeedById(Long id);

  ApiResponsePagination<List<Feed>> getFeedByUserId(Long userId, Pageable pageable);

  ApiResponse<Feed> updateFeed(Long id, FeedDTO feedDTO);

  ApiResponse<String> deleteFeed(Long id);
}
