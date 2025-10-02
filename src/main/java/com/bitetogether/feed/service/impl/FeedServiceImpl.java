package com.bitetogether.feed.service.impl;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.util.ApiResponseUtil;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.request.FeedRequest;
import com.bitetogether.feed.dto.response.FeedResponse;
import com.bitetogether.feed.mapper.FeedMapper;
import com.bitetogether.feed.model.Feed;
import com.bitetogether.feed.repository.FeedRepository;
import com.bitetogether.feed.repository.httpclient.UserClient;
import com.bitetogether.feed.service.inter.FeedService;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedServiceImpl implements FeedService {
  FeedRepository feedRepository;
  FeedMapper feedMapper;
  UserClient userClient;

  @Override
  @Transactional
  public ApiResponse<FeedResponse> createFeed(FeedRequest feed) {
    log.info("Creating feed with request: {}", feed);
    Feed feedEntity = feedMapper.toFeed(feed);
    log.info("Mapped feed entity: {}", feedEntity);

    Feed newFeed = feedRepository.save(feedEntity);
    log.info("Saved feed to database: {}", newFeed);

    Optional<Feed> verifyFeed = feedRepository.findById(newFeed.getId());
    log.info("Verification - Feed exists in DB: {}", verifyFeed.isPresent());
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        mapFeedToFeedResponseWithUser(newFeed));
  }

  @Override
  @Transactional
  public ApiResponse<FeedResponse> getFeedById(Long id) {
    Feed feed =
        feedRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Feed not found with id: " + id));
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        mapFeedToFeedResponseWithUser(feed));
  }

  @Override
  @Transactional
  public ApiResponsePagination<List<FeedResponse>> getFeedByUserId(Long userId, Pageable pageable) {
    Page<Feed> feeds = feedRepository.findAllByUserId(userId, pageable);
    List<FeedResponse> feedResponses =
        feeds.getContent().stream().map(this::mapFeedToFeedResponseWithUser).toList();
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        feedResponses.isEmpty() ? null : feedResponses,
        feeds.getNumber(),
        feeds.getTotalPages(),
        feeds.getTotalElements());
  }

  @Override
  @Transactional
  public ApiResponse<FeedResponse> updateFeed(Long id, FeedRequest feedRequest) {
    Feed feed =
        feedRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Feed not found with id: " + id));
    feedMapper.updateFeedFromFeedRequest(feedRequest, feed);
    feedRepository.save(feed);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        mapFeedToFeedResponseWithUser(feed));
  }

  @Override
  @Transactional
  public ApiResponse<String> deleteFeed(Long id) {
    Feed feed =
        feedRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Feed not found with id: " + id));
    feedRepository.delete(feed);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        "Feed deleted successfully");
  }

  private FeedResponse mapFeedToFeedResponseWithUser(Feed feed) {
    UserDTO userDTO = userClient.getUserById(feed.getUserId());
    FeedResponse feedResponse = feedMapper.toFeedResponse(feed);
    feedResponse.setUser(userDTO);
    log.info("Feed response: {}", feedResponse);
    return feedResponse;
  }
}
