package chetmine.marketplace.parser.repo;

import chetmine.marketplace.parser.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepo extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserId(Long userId);
}
