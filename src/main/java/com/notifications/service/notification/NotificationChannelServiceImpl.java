package com.notifications.service.notification;

import com.notifications.domain.NotificationChannel;
import com.notifications.domain.User;
import com.notifications.dto.request.NotificationChannelRequest;
import com.notifications.dto.response.NotificationChannelResponse;
import com.notifications.exception.ResourceNotFoundException;
import com.notifications.mapper.NotificationChannelMapper;
import com.notifications.repository.NotificationChannelRepository;
import com.notifications.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationChannelServiceImpl implements NotificationChannelService {

    private final NotificationChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final NotificationChannelMapper channelMapper;

    @Override
    @Transactional
    public NotificationChannelResponse createChannel(UUID userId, NotificationChannelRequest request) {
        if (channelRepository.existsByUserIdAndChannelType(userId, request.getChannelType())) {
            throw new IllegalStateException("Channel type '" + request.getChannelType() + "' already exists for this user");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        NotificationChannel channel = channelMapper.toEntity(request);
        channel.setUser(user);
        channel.setStatus("active");

        if (request.isConsentGiven()) {
            channel.setConsentGiven(true);
            channel.setConsentGivenAt(Instant.now());
        }

        channel = channelRepository.save(channel);
        log.info("Notification channel created: {} for user {}", request.getChannelType(), userId);
        return channelMapper.toResponse(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationChannelResponse> listChannels(UUID userId) {
        return channelRepository.findByUserId(userId).stream()
                .map(channelMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public NotificationChannelResponse updateChannel(UUID userId, UUID channelId, NotificationChannelRequest request) {
        NotificationChannel channel = channelRepository.findByIdAndUserId(channelId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationChannel", channelId));

        channelMapper.updateEntity(request, channel);

        if (request.isConsentGiven() && !channel.isConsentGiven()) {
            channel.setConsentGiven(true);
            channel.setConsentGivenAt(Instant.now());
        }

        channel = channelRepository.save(channel);
        log.info("Notification channel updated: {} for user {}", channel.getChannelType(), userId);
        return channelMapper.toResponse(channel);
    }

    @Override
    @Transactional
    public void deleteChannel(UUID userId, UUID channelId) {
        NotificationChannel channel = channelRepository.findByIdAndUserId(channelId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationChannel", channelId));
        channelRepository.delete(channel);
        log.info("Notification channel deleted: {} for user {}", channel.getChannelType(), userId);
    }
}
