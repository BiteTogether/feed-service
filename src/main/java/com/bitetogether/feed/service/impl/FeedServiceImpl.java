package com.bitetogether.feed.service.impl;

import com.bitetogether.common.dto.ApiResponse;
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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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
    Feed savedFeed = feedRepository.save(feedEntity);
    log.info("Feed saved with ID: {}", savedFeed.getId());
    FeedResponse response = mapFeedToFeedResponseWithUser(savedFeed);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS, ApiResponseStatus.SUCCESS.getDefaultMessage(), response);
  }

  @Override
  @Transactional
  public ApiResponse<FeedResponse> getFeedById(String id) {
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
  public ApiResponse<List<FeedResponse>> getFeedsByUserId(Long userId, int page, int size) {
    log.info(
        "Getting feeds for user ID: {} with pagination - page: {}, size: {}", userId, page, size);
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Feed> feedPage = feedRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    Page<FeedResponse> responsePage = feedPage.map(this::mapFeedToFeedResponseWithUser);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        responsePage.getContent(),
        responsePage.getNumber(),
        responsePage.getTotalPages(),
        responsePage.getTotalElements());
  }

  @Override
  @Transactional
  public ApiResponse<FeedResponse> updateFeed(String id, FeedRequest feedRequest) {
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
  public ApiResponse<String> deleteFeed(String id) {
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
    log.info("Mapping feed to response, feed: {}", feed);
    UserDTO userDTO = null;
    try {
      ResponseEntity<ApiResponse<UserDTO>> userResponse = userClient.getUserById(feed.getUserId());
      userDTO = userResponse.getBody() != null ? userResponse.getBody().getData() : null;
      log.info("Fetched userDTO: {}", userDTO);
    } catch (Exception ex) {
      log.error("Failed to fetch userDTO for userId {}: {}", feed.getUserId(), ex.getMessage(), ex);
    }
    FeedResponse feedResponse = feedMapper.toFeedResponse(feed);
    log.info("Mapped feedResponse before setting user: {}", feedResponse);
    feedResponse.setUser(userDTO);
    log.info("Feed response after setting user: {}", feedResponse);
    return feedResponse;
  }
}
