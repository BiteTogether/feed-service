package com.bitetogether.feed.service.impl;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.common.dto.PaginationRequest;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.util.ApiResponseUtil;
import com.bitetogether.feed.dto.FriendDTO;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.request.PostRequest;
import com.bitetogether.feed.dto.response.PostResponse;
import com.bitetogether.feed.mapper.PostMapper;
import com.bitetogether.feed.model.Post;
import com.bitetogether.feed.repository.PostRepository;
import com.bitetogether.feed.repository.httpclient.UserClient;
import com.bitetogether.feed.service.inter.PostService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostServiceImpl implements PostService {
  PostRepository postRepository;
  PostMapper postMapper;
  UserClient userClient;

  @Override
  @Transactional
  public ApiResponse<PostResponse> createPost(PostRequest postRequest) {
    log.info("Creating feed with request: {}", postRequest);
    Post postEntity = postMapper.toPost(postRequest);
    Post savedPost = postRepository.save(postEntity);
    log.info("Feed saved with ID: {}", savedPost.getId());
    PostResponse response = mapPostToPostResponseWithUser(savedPost);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS, ApiResponseStatus.SUCCESS.getDefaultMessage(), response);
  }

  @Override
  @Transactional
  public ApiResponse<PostResponse> getPostById(String id) {
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
  public ApiResponsePagination<PostResponse> getPostsByUserId(Long userId, int page, int size) {
    log.info(
        "Getting posts for user ID: {} with pagination - page: {}, size: {}", userId, page, size);
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Post> feedPage = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    Page<PostResponse> responsePage = feedPage.map(this::mapPostToPostResponseWithUser);
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
  public ApiResponse<PostResponse> updatePost(String id, PostRequest postRequest) {
    Post post =
        postRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    postMapper.updatePostFromPostRequest(postRequest, post);
    postRepository.save(post);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        mapPostToPostResponseWithUser(post));
  }

  @Override
  @Transactional
  public ApiResponse<String> deletePost(String id) {
    Post post =
        postRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    postRepository.delete(post);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        "Feed deleted successfully");
  }

  @Override
  @Transactional
  public ApiResponsePagination<PostResponse> getNewFeed(int page, int size) {
    log.info("Fetching new feed with pagination - page: {}, size: {}", page, size);
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

    List<FriendDTO> friends;
    try {
      PaginationRequest paginationRequest = new PaginationRequest();
      paginationRequest.setPage(0);
      paginationRequest.setSize(100);
      paginationRequest.setLimit(100);
      ResponseEntity<ApiResponsePagination<FriendDTO>> friendResponse =
          userClient.getFriendList(paginationRequest);
      friends = friendResponse.getBody() != null ? friendResponse.getBody().getData() : List.of();
    } catch (Exception ex) {
      log.error("Failed to fetch friend list: {}", ex.getMessage(), ex);
      friends = List.of();
    }
    List<Long> friendIds = extractUserIds(friends);

    Page<Post> feedPage =
        postRepository.findByUserIdInAndCreatedAtAfter(
            friendIds, Instant.now().minus(2, ChronoUnit.DAYS), pageable);
    Page<PostResponse> responsePage = feedPage.map(this::mapPostToPostResponseWithUser);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        responsePage.getContent(),
        responsePage.getNumber(),
        responsePage.getTotalPages(),
        responsePage.getTotalElements());
  }

  private PostResponse mapPostToPostResponseWithUser(Post post) {
    log.info("Mapping post to response, post: {}", post);
    UserDTO userDTO = null;
    try {
      ResponseEntity<ApiResponse<UserDTO>> userResponse = userClient.getUserById(post.getUserId());
      userDTO = userResponse.getBody() != null ? userResponse.getBody().getData() : null;
      log.info("Fetched userDTO: {}", userDTO);
    } catch (Exception ex) {
      log.error("Failed to fetch userDTO for userId {}: {}", post.getUserId(), ex.getMessage(), ex);
    }
    PostResponse postResponse = postMapper.toPostResponse(post);
    log.info("Mapped postResponse before setting user: {}", postResponse);
    postResponse.setUser(userDTO);
    log.info("Post response after setting user: {}", postResponse);
    return postResponse;
  }

  private List<Long> extractUserIds(List<FriendDTO> friends) {
    return friends.stream().map(FriendDTO::getId).distinct().toList();
  }
}
