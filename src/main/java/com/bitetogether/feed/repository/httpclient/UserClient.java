package com.bitetogether.feed.repository.httpclient;

import com.bitetogether.feed.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${feign.user-service.url}")
public interface UserClient {
  @GetMapping("/api/v1/users/{id}")
  UserDTO getUserById(@PathVariable Long id);
}
