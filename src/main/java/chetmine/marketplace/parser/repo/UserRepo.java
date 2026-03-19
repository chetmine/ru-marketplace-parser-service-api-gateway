package chetmine.marketplace.parser.repo;

import chetmine.marketplace.parser.entity.User;
import chetmine.marketplace.parser.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    @Modifying
    @Query("DELETE FROM User u WHERE u.status = :status AND u.createdAt < :threshold")
    int deleteByStatusAndCreatedAtBefore(
            @Param("status") UserStatus status,
            @Param("threshold") LocalDateTime threshold
    );

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
