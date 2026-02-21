package com.notifications.repository;

import com.notifications.domain.FilterRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FilterRuleRepository extends JpaRepository<FilterRule, UUID> {
    List<FilterRule> findByUserId(UUID userId);
    List<FilterRule> findByUserIdAndIsActiveOrderByPriorityAsc(UUID userId, boolean isActive);
    Optional<FilterRule> findByIdAndUserId(UUID id, UUID userId);
}
