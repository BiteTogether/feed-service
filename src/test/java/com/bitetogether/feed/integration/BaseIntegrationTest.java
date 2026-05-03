package com.bitetogether.feed.integration;

import com.bitetogether.feed.model.Comment;
import com.bitetogether.feed.model.Like;
import com.bitetogether.feed.model.Post;
import com.bitetogether.feed.model.SavePost;
import com.bitetogether.feed.repository.CommentRepository;
import com.bitetogether.feed.repository.LikeRepository;
import com.bitetogether.feed.repository.PostRepository;
import com.bitetogether.feed.repository.SavePostRepository;
import com.bitetogether.feed.repository.httpclient.UserClient;
import com.bitetogether.feed.service.inter.FirebaseStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(IntegrationTestConfig.class)
public abstract class BaseIntegrationTest {

  protected static final String BASE_URL = "/api/v1/feeds";
  protected static final String X_USER_ID = "X-User-Id";

  @Autowired protected MockMvc mockMvc;
  @Autowired protected ObjectMapper objectMapper;
  @Autowired protected PostRepository postRepository;
  @Autowired protected CommentRepository commentRepository;
  @Autowired protected LikeRepository likeRepository;
  @Autowired protected SavePostRepository savePostRepository;

  @MockitoBean protected FirebaseApp firebaseApp;
  @MockitoBean protected Storage storage;
  @MockitoBean protected FirebaseStorageService firebaseStorageService;
  @MockitoBean protected UserClient userClient;

  @BeforeEach
  void cleanUp() {
    likeRepository.deleteAll();
    commentRepository.deleteAll();
    savePostRepository.deleteAll();
    postRepository.deleteAll();
  }

  protected Post createTestPost(Long userId, String content) {
    Post post = new Post();
    post.setUserId(userId);
    post.setContent(content);
    post.setPlaceId("place1");
    post.setPlaceName("Test Place");
    post.setPlaceAddress("123 Test St");
    post.setLatitude(10.7769);
    post.setLongitude(106.7009);
    post.setRating(5);
    post.setLikeCount(0);
    post.setCommentCount(0);
    return postRepository.save(post);
  }

  protected Comment createTestComment(String postId, Long userId, String content) {
    Comment comment = new Comment();
    comment.setPostId(postId);
    comment.setUserId(userId);
    comment.setContent(content);
    comment.setLikeCount(0);
    comment.setRepliesCount(0);
    return commentRepository.save(comment);
  }

  protected Comment createTestReply(
      String postId, Long userId, String content, String parentCommentId) {
    Comment reply = new Comment();
    reply.setPostId(postId);
    reply.setUserId(userId);
    reply.setContent(content);
    reply.setParentCommentId(parentCommentId);
    reply.setLikeCount(0);
    reply.setRepliesCount(0);
    return commentRepository.save(reply);
  }

  protected Like createTestLikeOnPost(Long userId, String postId) {
    Like like = new Like();
    like.setUserId(userId);
    like.setPostId(postId);
    return likeRepository.save(like);
  }

  protected Like createTestLikeOnComment(Long userId, String commentId) {
    Like like = new Like();
    like.setUserId(userId);
    like.setCommentId(commentId);
    return likeRepository.save(like);
  }

  protected SavePost createTestSavePost(Long userId, String postId) {
    SavePost savePost = new SavePost();
    savePost.setUserId(userId);
    savePost.setPostId(postId);
    return savePostRepository.save(savePost);
  }
}
