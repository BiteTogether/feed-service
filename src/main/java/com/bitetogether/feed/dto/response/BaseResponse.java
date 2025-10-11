package com.bitetogether.feed.dto.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseResponse {
  private Instant createdAt;
  private Instant updatedAt;
  private String createdBy;
  private String updatedBy;
}
