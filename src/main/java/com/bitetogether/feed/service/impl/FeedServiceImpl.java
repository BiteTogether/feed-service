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

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedServiceImpl implements FeedService {
    FeedRepository feedRepository;
    FeedMapper feedMapper;

//    @Override
//    @Transactional(noRollbackFor = Exception.class)
//    public ApiResponse<Feed> createFeed (FeedDTO feed) {
//        ApiResponse<Feed> response;
//        try{
//            Feed newFeed = feedRepository.save(feedMapper.toFeed(feed));
//            response = ApiResponseUtil.buildResponse(
//                    ApiResponseStatus.SUCCESS,
//                    ApiResponseStatus.SUCCESS.getDefaultMessage(),
//                    newFeed
//            );
//        } catch (Exception e) {
//            response = ApiResponseUtil.buildResponse(
//                    ApiResponseStatus.ERROR,
//                    "Creating feed error: " + e.getMessage(),
//                    null
//            );
//        }
//        return response;
//    }

    @Override
    @Transactional
    public ApiResponse<Feed> createFeed (FeedDTO feed) {
        ApiResponse<Feed> response;
        Feed newFeed = feedRepository.save(feedMapper.toFeed(feed));
        response = ApiResponseUtil.buildResponse(
                ApiResponseStatus.SUCCESS,
                ApiResponseStatus.SUCCESS.getDefaultMessage(),
                newFeed
        );
        return response;
    }

    @Override
    @Transactional
    public ApiResponse<Feed> getFeedById (Long id) {
        ApiResponse<Feed> response;
        Feed feed = feedRepository.findById(id).orElseThrow(() -> new RuntimeException("Feed not found with id: " + id));
        response = ApiResponseUtil.buildResponse(
                ApiResponseStatus.SUCCESS,
                ApiResponseStatus.SUCCESS.getDefaultMessage(),
                feed
        );
        return response;
    }

    @Override
    @Transactional
    public ApiResponse<List<Feed>> getFeedByUserId (Long userId) {
        ApiResponse<List<Feed>> response;
        List<Feed> feeds = feedRepository.findAllByUserId(userId);
        response = ApiResponseUtil.buildResponse(
                ApiResponseStatus.SUCCESS,
                ApiResponseStatus.SUCCESS.getDefaultMessage(),
                feeds.isEmpty()? null: feeds
        );
        return response;
    }

    @Override
    @Transactional
    public ApiResponse<Feed> updateFeed (Long id, FeedDTO feedDTO) {
        ApiResponse<Feed> response;
        Feed feed = feedRepository.findById(id).orElseThrow(() -> new RuntimeException("Feed not found with id: " + id));
        feedMapper.updateFeedFromDTO(feedDTO, feed);
        feedRepository.save(feed);
        response = ApiResponseUtil.buildResponse(
                ApiResponseStatus.SUCCESS,
                ApiResponseStatus.SUCCESS.getDefaultMessage(),
                feed
        );
        return response;
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteFeed (Long id) {
        ApiResponse<String> response;
        Feed feed = feedRepository.findById(id).orElseThrow(() -> new RuntimeException("Feed not found with id: " + id));
        feedRepository.delete(feed);
        response = ApiResponseUtil.buildResponse(
                ApiResponseStatus.SUCCESS,
                ApiResponseStatus.SUCCESS.getDefaultMessage(),
                "Feed deleted successfully"
        );
        return response;
    }
}
