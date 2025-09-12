package com.bitetogether.feed.service.impl;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.util.ApiResponseUtil;
import com.bitetogether.feed.dto.FeedDTO;
import com.bitetogether.feed.mapper.FeedMapper;
import com.bitetogether.feed.model.Feed;
import com.bitetogether.feed.repository.FeedRepository;

import com.bitetogether.feed.service.inter.FeedService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedServiceImpl implements FeedService {
    FeedRepository feedRepository;
    FeedMapper feedMapper;

    @Override
    @Transactional
    public ApiResponse<Feed> createFeed (FeedDTO feed) {
        ApiResponse<Feed> response;
        try{
            Feed newFeed = feedRepository.save(feedMapper.toFeed(feed));
            response = ApiResponseUtil.buildResponse(
                    ApiResponseStatus.SUCCESS,
                    ApiResponseStatus.SUCCESS.getDefaultMessage(),
                    newFeed
            );
        } catch (Exception e) {
            response = ApiResponseUtil.buildResponse(
                    ApiResponseStatus.ERROR,
                    "Creating feed error: " + e.getMessage(),
                    null
            );
        }
        return response;
    }
}
