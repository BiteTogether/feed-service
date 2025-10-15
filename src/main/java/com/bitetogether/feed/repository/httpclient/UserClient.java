package com.bitetogether.feed.repository.httpclient;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.common.dto.PaginationRequest;
import com.bitetogether.common.util.Constants;
import com.bitetogether.feed.configuration.openfeign.FeignClientConfig;
import com.bitetogether.feed.dto.FriendDTO;
import com.bitetogether.feed.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "user-service",
    url = "${feign.user-service.url}",
    configuration = FeignClientConfig.class)
public interface UserClient {
  @GetMapping(Constants.PREFIX_REQUEST_MAPPING_USER + "/{id}")
  ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id);

  @GetMapping(Constants.PREFIX_REQUEST_MAPPING_FRIEND)
  ResponseEntity<ApiResponsePagination<FriendDTO>> getFriendList(
      @SpringQueryMap PaginationRequest paginationRequest);
}
