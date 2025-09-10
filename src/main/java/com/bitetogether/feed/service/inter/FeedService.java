package com.bitetogether.feed.service.inter;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.feed.dto.FeedDTO;
import com.bitetogether.feed.model.Feed;

public interface FeedService {
    ApiResponse<Feed> createFeed (FeedDTO feed);
}
