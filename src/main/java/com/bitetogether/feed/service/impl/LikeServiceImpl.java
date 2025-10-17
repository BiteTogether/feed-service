package com.bitetogether.feed.service.impl;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.util.ApiResponseUtil;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.request.LikeRequest;
import com.bitetogether.feed.dto.response.LikeResponse;
import com.bitetogether.feed.mapper.LikeMapper;
import com.bitetogether.feed.model.Like;
import com.bitetogether.feed.model.Post;
import com.bitetogether.feed.repository.LikeRepository;
import com.bitetogether.feed.repository.PostRepository;
import com.bitetogether.feed.repository.httpclient.UserClient;
import com.bitetogether.feed.service.inter.LikeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LikeServiceImpl implements LikeService {

  LikeRepository likeRepository;
  PostRepository postRepository;
  LikeMapper likeMapper;
  UserClient userClient;

  @Override
  @Transactional
  public ApiResponse<LikeResponse> like(LikeRequest request) {
    log.info(
        "User {} liking postId={}, commentId={}",
        request.getUserId(),
        request.getPostId(),
        request.getCommentId());

    if (request.getPostId() == null && request.getCommentId() == null)
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.BAD_REQUEST, "Invalid like target", null);

    boolean alreadyLiked =
        (request.getPostId() != null)
            ? likeRepository
                .findByUserIdAndPostId(
                    request.getUserId(), request.getPostId(), PageRequest.of(0, 1))
                .hasContent()
            : likeRepository
                .findByUserIdAndCommentId(
                    request.getUserId(), request.getCommentId(), PageRequest.of(0, 1))
                .hasContent();

    if (alreadyLiked)
      return ApiResponseUtil.buildApiResponse(ApiResponseStatus.CONFLICT, "Already liked", null);

    Like like = likeMapper.toLike(request);
    Like saved = likeRepository.save(like);

    Post post = postRepository.findById(request.getPostId()).orElse(null);
    if (post != null) {
      post.setLikeCount(post.getLikeCount() + 1);
      postRepository.save(post);
    }

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS, "Liked successfully", mapLikeToLikeResponseWithUser(saved));
  }

  @Override
  @Transactional
  public ApiResponse<String> unlike(LikeRequest request) {
    log.info(
        "User {} unliking postId={}, commentId={}",
        request.getUserId(),
        request.getPostId(),
        request.getCommentId());

    if (request.getPostId() == null && request.getCommentId() == null)
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.BAD_REQUEST, "Invalid unlike target", null);

    Like like =
        (request.getPostId() != null)
            ? likeRepository
                .findByUserIdAndPostId(
                    request.getUserId(), request.getPostId(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null)
            : likeRepository
                .findByUserIdAndCommentId(
                    request.getUserId(), request.getCommentId(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);

    if (like == null)
      return ApiResponseUtil.buildApiResponse(ApiResponseStatus.NOT_FOUND, "Like not found", null);

    likeRepository.delete(like);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS, "Unliked successfully", null);
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponsePagination<LikeResponse> getLikesByUser(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Like> likePage = likeRepository.findByUserId(userId, pageable);
    Page<LikeResponse> responsePage = likePage.map(this::mapLikeToLikeResponseWithUser);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        responsePage.getContent(),
        responsePage.getNumber(),
        responsePage.getTotalPages(),
        responsePage.getTotalElements());
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponsePagination<LikeResponse> getLikesByPost(String postId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Like> likePage = likeRepository.findByPostId(postId, pageable);
    Page<LikeResponse> responsePage = likePage.map(this::mapLikeToLikeResponseWithUser);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        responsePage.getContent(),
        responsePage.getNumber(),
        responsePage.getTotalPages(),
        responsePage.getTotalElements());
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponsePagination<LikeResponse> getLikesByComment(
      String commentId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Like> likePage = likeRepository.findByCommentId(commentId, pageable);
    Page<LikeResponse> responsePage = likePage.map(this::mapLikeToLikeResponseWithUser);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        responsePage.getContent(),
        responsePage.getNumber(),
        responsePage.getTotalPages(),
        responsePage.getTotalElements());
  }

    private LikeResponse mapLikeToLikeResponseWithUser(Like like) {
        log.info("Mapping like to response, like: {}", like);
        UserDTO userDTO = null;
        try {
            ResponseEntity<ApiResponse<UserDTO>> userResponse = userClient.getUserById(like.getUserId());
            userDTO = userResponse.getBody() != null ? userResponse.getBody().getData() : null;
            log.info("Fetched userDTO: {}", userDTO);
        } catch (Exception ex) {
            log.error("Failed to fetch userDTO for userId {}: {}", like.getUserId(), ex.getMessage(), ex);
        }
        LikeResponse likeResponse = likeMapper.toLikeResponse(like);
        log.info("Mapped postResponse before setting user: {}", likeResponse);
        likeResponse.setUser(userDTO);
        log.info("Post response after setting user: {}", likeResponse);
        return likeResponse;
    }
}
