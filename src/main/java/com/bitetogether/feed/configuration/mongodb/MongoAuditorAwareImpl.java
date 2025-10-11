package com.bitetogether.feed.configuration.mongodb;

import static com.bitetogether.common.util.SecurityUtils.getCurrentUserId;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component("mongoAuditorProvider")
public class MongoAuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            Long currentUserId = getCurrentUserId();

            if (currentUserId != null) {
                return Optional.of("USER_" + currentUserId);
            }

        } catch (Exception ignored) {
        }

        return Optional.of("SYSTEM");
    }
}
