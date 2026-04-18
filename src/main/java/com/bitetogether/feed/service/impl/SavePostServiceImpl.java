package com.bitetogether.feed.service.impl;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.util.ApiResponseUtil;
import com.bitetogether.common.util.UserContextUtils;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.response.PostResponse;
import com.bitetogether.feed.mapper.PostMapper;
import com.bitetogether.feed.model.Post;
import com.bitetogether.feed.model.SavePost;
import com.bitetogether.feed.repository.LikeRepository;
import com.bitetogether.feed.repository.PostRepository;
import com.bitetogether.feed.repository.SavePostRepository;
import com.bitetogether.feed.repository.httpclient.UserClient;
import com.bitetogether.feed.service.inter.SavePostService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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
public class SavePostServiceImpl implements SavePostService {

  SavePostRepository savePostRepository;
  PostRepository postRepository;
  PostMapper postMapper;
  UserClient userClient;
  LikeRepository likeRepository;

  private static final String SORT_BY_CREATED_AT = "createdAt";

  @Override
  @Transactional
  public ApiResponseDTO<PostResponse> savePost(String postId) {
    Long currentUserId = UserContextUtils.getCurrentUserId();

    if (postId == null || postId.isBlank()) {
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.BAD_REQUEST, "postId is required", null);
    }

    Post post = postRepository.findById(postId).orElse(null);
    if (post == null) {
      return ApiResponseUtil.buildApiResponse(ApiResponseStatus.NOT_FOUND, "Post not found", null);
    }

    if (savePostRepository.existsByUserIdAndPostId(currentUserId, postId)) {
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.CONFLICT, "Post already saved", null);
    }

    SavePost savePost = new SavePost();
    savePost.setUserId(currentUserId);
    savePost.setPostId(postId);
    try {
      savePostRepository.save(savePost);
    } catch (DuplicateKeyException ex) {
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.CONFLICT, "Post already saved", null);
    }

    PostResponse response = mapPostToPostResponseWithUser(post);
    response.setAlreadySaved(true);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS, "Post saved successfully", response);
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponsePaginationDTO<PostResponse> getSavedPosts(int page, int size) {
    Long currentUserId = UserContextUtils.getCurrentUserId();
    Pageable pageable =
        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, SORT_BY_CREATED_AT));
    Page<SavePost> savedPostPage = savePostRepository.findByUserId(currentUserId, pageable);

    List<String> postIds = savedPostPage.getContent().stream().map(SavePost::getPostId).toList();
    Map<String, Post> postMap = new HashMap<>();
    if (!postIds.isEmpty()) {
      List<Post> posts = postRepository.findAllById(postIds);
      postMap =
          posts.stream().collect(java.util.stream.Collectors.toMap(Post::getId, post -> post));
    }

    Set<String> likedPostIds = fetchLikedPostIds(postIds, currentUserId);
    Set<String> savedPostIds = fetchSavedPostIds(postIds, currentUserId);

    Map<String, Post> finalPostMap = postMap;
    List<PostResponse> responses =
        postIds.stream()
            .map(finalPostMap::get)
            .filter(java.util.Objects::nonNull)
            .map(
                post ->
                    mapPostToPostResponseWithUser(
                        post,
                        likedPostIds.contains(post.getId()),
                        savedPostIds.contains(post.getId())))
            .toList();

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        responses,
        savedPostPage.getNumber(),
        savedPostPage.getTotalPages(),
        savedPostPage.getTotalElements());
  }

  @Override
  @Transactional
  public ApiResponseDTO<String> deleteSavedPost(String postId) {
    Long currentUserId = UserContextUtils.getCurrentUserId();

    SavePost savePost =
        savePostRepository.findByUserIdAndPostId(currentUserId, postId).orElse(null);
    if (savePost == null) {
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.NOT_FOUND, "Saved post not found", null);
    }

    savePostRepository.delete(savePost);
    return ApiResponseUtil.buildApiResponse(ApiResponseStatus.SUCCESS, "Saved post removed", null);
  }

  private Set<String> fetchLikedPostIds(List<String> postIds, Long currentUserId) {
    if (postIds.isEmpty()) {
      return Set.of();
    }
    try {
      return likeRepository.findByUserIdAndPostIdIn(currentUserId, postIds).stream()
          .map(like -> like.getPostId())
          .collect(java.util.stream.Collectors.toSet());
    } catch (Exception ex) {
      log.debug("Could not fetch liked posts for saved posts: {}", ex.getMessage());
      return Set.of();
    }
  }

  private Set<String> fetchSavedPostIds(List<String> postIds, Long currentUserId) {
    if (postIds.isEmpty()) {
      return Set.of();
    }
    try {
      return savePostRepository.findByUserIdAndPostIdIn(currentUserId, postIds).stream()
          .map(SavePost::getPostId)
          .collect(java.util.stream.Collectors.toSet());
    } catch (Exception ex) {
      log.debug("Could not fetch saved posts flags: {}", ex.getMessage());
      return Set.of();
    }
  }

  private PostResponse mapPostToPostResponseWithUser(Post post) {
    return mapPostToPostResponseWithUser(post, null, null);
  }

  private PostResponse mapPostToPostResponseWithUser(
      Post post, Boolean alreadyLikedOverride, Boolean alreadySavedOverride) {
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

    if (alreadyLikedOverride != null) {
      postResponse.setAlreadyLiked(alreadyLikedOverride);
    }
    if (alreadySavedOverride != null) {
      postResponse.setAlreadySaved(alreadySavedOverride);
    }

    if (alreadyLikedOverride != null && alreadySavedOverride != null) {
      return postResponse;
    }

    try {
      Long currentUserId = UserContextUtils.getCurrentUserId();
      if (alreadyLikedOverride == null) {
        boolean alreadyLiked = likeRepository.existsByUserIdAndPostId(currentUserId, post.getId());
        postResponse.setAlreadyLiked(alreadyLiked);
      }
      if (alreadySavedOverride == null) {
        boolean alreadySaved =
            savePostRepository.existsByUserIdAndPostId(currentUserId, post.getId());
        postResponse.setAlreadySaved(alreadySaved);
      }
    } catch (Exception ex) {
      log.debug("Could not determine alreadyLiked/alreadySaved status: {}", ex.getMessage());
      if (alreadyLikedOverride == null) {
        postResponse.setAlreadyLiked(false);
      }
      if (alreadySavedOverride == null) {
        postResponse.setAlreadySaved(false);
      }
    }

    return postResponse;
  }
}
