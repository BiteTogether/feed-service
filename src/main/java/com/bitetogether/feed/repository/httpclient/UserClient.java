package com.bitetogether.feed.repository.httpclient;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.feed.configuration.openfeign.FeignClientConfig;
import com.bitetogether.feed.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "user-service",
    url = "${feign.user-service.url}",
    configuration = FeignClientConfig.class)
public interface UserClient {
  @GetMapping("/api/v1/users/{id}")
  ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id);
}
