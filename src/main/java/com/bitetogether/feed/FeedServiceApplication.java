package com.bitetogether.feed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.bitetogether.feed", "com.bitetogether.common"})
@EnableTransactionManagement
public class FeedServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(FeedServiceApplication.class, args);
  }
}
