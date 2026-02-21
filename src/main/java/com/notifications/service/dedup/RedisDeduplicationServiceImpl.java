package com.notifications.service.dedup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisDeduplicationServiceImpl implements DeduplicationService {

    private static final Duration TTL = Duration.ofHours(48);
    private static final String KEY_PREFIX = "dedup:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean isDuplicate(UUID userId, String gmailMessageId) {
        String key = KEY_PREFIX + userId;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, gmailMessageId);
        return Boolean.TRUE.equals(isMember);
    }

    @Override
    public void markProcessed(UUID userId, String gmailMessageId) {
        String key = KEY_PREFIX + userId;
        redisTemplate.opsForSet().add(key, gmailMessageId);
        redisTemplate.expire(key, TTL);
    }
}
