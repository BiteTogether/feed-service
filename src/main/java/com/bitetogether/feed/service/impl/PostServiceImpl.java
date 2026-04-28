package com.bitetogether.feed.service.impl;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.util.ApiResponseUtil;
import com.bitetogether.common.util.UserContextUtils;
import com.bitetogether.feed.dto.FriendDTO;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.request.PostRequest;
import com.bitetogether.feed.dto.response.PostResponse;
import com.bitetogether.feed.mapper.PostMapper;
import com.bitetogether.feed.model.Like;
import com.bitetogether.feed.model.Post;
import com.bitetogether.feed.model.SavePost;
import com.bitetogether.feed.repository.LikeRepository;
import com.bitetogether.feed.repository.PostRepository;
import com.bitetogether.feed.repository.SavePostRepository;
import com.bitetogether.feed.repository.httpclient.UserClient;
import com.bitetogether.feed.service.inter.FirebaseStorageService;
import com.bitetogether.feed.service.inter.PostService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostServiceImpl implements PostService {
  PostRepository postRepository;
  PostMapper postMapper;
  UserClient userClient;
  LikeRepository likeRepository;
  SavePostRepository savePostRepository;
  FirebaseStorageService firebaseStorageService;

  @Override
  @Transactional
  public ApiResponseDTO<PostResponse> createPost(PostRequest postRequest) {
    Long currentUserId = UserContextUtils.getCurrentUserId();

    log.info("Creating feed with request: {}", postRequest);
    Post postEntity = postMapper.toPost(postRequest);
    postEntity.setUserId(currentUserId);
    Post savedPost = postRepository.save(postEntity);
    log.info("Feed saved with ID: {}", savedPost.getId());
    PostResponse response = mapPostToPostResponseWithUser(savedPost);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS, ApiResponseStatus.SUCCESS.getDefaultMessage(), response);
  }

  @Override
  @Transactional
  public ApiResponseDTO<PostResponse> getPostById(String id) {
    Post post =
        postRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        mapPostToPostResponseWithUser(post));
  }

  @Override
  @Transactional
  public ApiResponsePaginationDTO<PostResponse> getPostsByUserId(Long userId, int page, int size) {
    log.info(
        "Getting posts for user ID: {} with pagination - page: {}, size: {}", userId, page, size);
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Post> feedPage = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

    // Batch query to avoid N+1 problem for alreadyLiked
    List<PostResponse> responses = mapPostsToResponsesWithBatchLikes(feedPage.getContent());

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        responses,
        feedPage.getNumber(),
        feedPage.getTotalPages(),
        feedPage.getTotalElements());
  }

  private List<PostResponse> mapPostsToResponsesWithBatchLikes(List<Post> posts) {
    if (posts.isEmpty()) {
      return List.of();
    }

    List<String> postIds = posts.stream().map(Post::getId).toList();
    Set<String> likedPostIds = Set.of();
    Set<String> savedPostIds = Set.of();
    try {
      Long currentUserId = UserContextUtils.getCurrentUserId();
      likedPostIds =
          likeRepository.findByUserIdAndPostIdIn(currentUserId, postIds).stream()
              .map(Like::getPostId)
              .collect(Collectors.toSet());
      savedPostIds =
          savePostRepository.findByUserIdAndPostIdIn(currentUserId, postIds).stream()
              .map(SavePost::getPostId)
              .collect(Collectors.toSet());
    } catch (Exception ex) {
      log.debug("Could not fetch batch like/saved flags: {}", ex.getMessage());
    }

    Set<String> finalLikedPostIds = likedPostIds;
    Set<String> finalSavedPostIds = savedPostIds;
    return posts.stream()
        .map(
            post -> {
              PostResponse response = mapPostToPostResponseWithUser(post);
              response.setAlreadyLiked(finalLikedPostIds.contains(post.getId()));
              response.setAlreadySaved(finalSavedPostIds.contains(post.getId()));
              return response;
            })
        .toList();
  }

  @Override
  @Transactional
  public ApiResponseDTO<PostResponse> updatePost(String id, PostRequest postRequest) {
    Post post =
        postRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

    Long currentUserId = UserContextUtils.getCurrentUserId();
    if (!post.getUserId().equals(currentUserId)) {
      log.warn(
          "User {} attempted to update post {} owned by user {}",
          currentUserId,
          id,
          post.getUserId());
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.FORBIDDEN, "You are not authorized to update this post", null);
    }

    // Delete old image if photoUrl is being changed and old one exists
    String oldPhotoUrl = post.getPhotoUrl();
    String newPhotoUrl = postRequest.getPhotoUrl();
    if (StringUtils.isEmpty(oldPhotoUrl)
        && StringUtils.isEmpty(newPhotoUrl)
        && !oldPhotoUrl.equals(newPhotoUrl)) {
      log.info("Deleting old image from post {}: {}", id, oldPhotoUrl);
      firebaseStorageService.deleteFile(oldPhotoUrl);
    }

    postMapper.updatePostFromPostRequest(postRequest, post);
    postRepository.save(post);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        mapPostToPostResponseWithUser(post));
  }

  @Override
  @Transactional
  public ApiResponseDTO<String> deletePost(String id) {
    Post post =
        postRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

    Long currentUserId = UserContextUtils.getCurrentUserId();
    if (!post.getUserId().equals(currentUserId)) {
      log.warn(
          "User {} attempted to delete post {} owned by user {}",
          currentUserId,
          id,
          post.getUserId());
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.FORBIDDEN, "You are not authorized to delete this post", null);
    }

    // Delete image from Firebase Storage if exists
    if (post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty()) {
      log.info("Deleting image from post {}: {}", id, post.getPhotoUrl());
      firebaseStorageService.deleteFile(post.getPhotoUrl());
    }

    postRepository.delete(post);
    savePostRepository.deleteAllByPostId(id);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        "Feed deleted successfully");
  }

  @Override
  @Transactional
  public ApiResponsePaginationDTO<PostResponse> getNewFeedLocationBased(
      int page,
      int size,
      double latitude,
      double longitude,
      double latitudeDelta,
      double longitudeDelta) {
    log.info(
        "Fetching map-based feed page={}, size={}, latitude={}, longitude={}, latitudeDelta={}, longitudeDelta={}",
        page,
        size,
        latitude,
        longitude,
        latitudeDelta,
        longitudeDelta);
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    List<FriendDTO> friends;
    try {
      ResponseEntity<ApiResponsePaginationDTO<FriendDTO>> friendResponse =
          userClient.getFriendList(0, 100);
      friends = friendResponse.getBody() != null ? friendResponse.getBody().getData() : List.of();
    } catch (Exception ex) {
      log.error("Failed to fetch friend list: {}", ex.getMessage(), ex);
      friends = List.of();
    }
    Long currentUserId = UserContextUtils.getCurrentUserId();
    List<Long> friendIds =
        Stream.concat(extractUserIds(friends).stream(), Stream.of(currentUserId))
            .distinct()
            .toList();

    double halfLatitudeDelta = Math.abs(latitudeDelta) / 2.0;
    double halfLongitudeDelta = Math.abs(longitudeDelta) / 2.0;
    double minLatitude = Math.max(-90.0, latitude - halfLatitudeDelta);
    double maxLatitude = Math.min(90.0, latitude + halfLatitudeDelta);
    double minLongitude = Math.max(-180.0, longitude - halfLongitudeDelta);
    double maxLongitude = Math.min(180.0, longitude + halfLongitudeDelta);

    Page<Post> feedPage =
        postRepository.findByUserIdInAndCreatedAtAfterAndLatitudeBetweenAndLongitudeBetween(
            friendIds,
            Instant.now().minus(365, ChronoUnit.DAYS),
            minLatitude,
            maxLatitude,
            minLongitude,
            maxLongitude,
            pageable);

    // Use batch optimization for alreadyLiked
    List<PostResponse> responses = mapPostsToResponsesWithBatchLikes(feedPage.getContent());

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        responses,
        feedPage.getNumber(),
        feedPage.getTotalPages(),
        feedPage.getTotalElements());
  }

  @Override
  @Transactional
  public ApiResponsePaginationDTO<PostResponse> getNewFeedTimeBased(int page, int size) {
    log.info("Fetching time-based feed page={}, size={}", page, size);
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    List<FriendDTO> friends;
    try {
      ResponseEntity<ApiResponsePaginationDTO<FriendDTO>> friendResponse =
          userClient.getFriendList(0, 100);
      friends = friendResponse.getBody() != null ? friendResponse.getBody().getData() : List.of();
    } catch (Exception ex) {
      log.error("Failed to fetch friend list: {}", ex.getMessage(), ex);
      friends = List.of();
    }

    Long currentUserId = UserContextUtils.getCurrentUserId();
    List<Long> friendIds =
        Stream.concat(extractUserIds(friends).stream(), Stream.of(currentUserId))
            .distinct()
            .toList();

    Page<Post> feedPage = postRepository.findByUserIdInOrderByCreatedAtDesc(friendIds, pageable);

    List<PostResponse> responses = mapPostsToResponsesWithBatchLikes(feedPage.getContent());

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        responses,
        feedPage.getNumber(),
        feedPage.getTotalPages(),
        feedPage.getTotalElements());
  }

  private PostResponse mapPostToPostResponseWithUser(Post post) {
    log.info("Mapping post to response, post: {}", post);
    UserDTO userDTO = null;
    try {
      ResponseEntity<ApiResponseDTO<UserDTO>> userResponse =
          userClient.getUserById(post.getUserId());
      userDTO = userResponse.getBody() != null ? userResponse.getBody().getData() : null;
    } catch (Exception ex) {
      log.error("Failed to fetch userDTO for userId {}: {}", post.getUserId(), ex.getMessage(), ex);
    }
    PostResponse postResponse = postMapper.toPostResponse(post);
    postResponse.setUser(userDTO);

    try {
      Long currentUserId = UserContextUtils.getCurrentUserId();
      boolean alreadyLiked = likeRepository.existsByUserIdAndPostId(currentUserId, post.getId());
      boolean alreadySaved =
          savePostRepository.existsByUserIdAndPostId(currentUserId, post.getId());
      postResponse.setAlreadyLiked(alreadyLiked);
      postResponse.setAlreadySaved(alreadySaved);
    } catch (Exception ex) {
      log.debug("Could not determine alreadyLiked/alreadySaved status: {}", ex.getMessage());
      postResponse.setAlreadyLiked(false);
      postResponse.setAlreadySaved(false);
    }

    return postResponse;
  }

  private List<Long> extractUserIds(List<FriendDTO> friends) {
    return friends.stream().map(FriendDTO::getId).distinct().toList();
  }
}
