package chetmine.marketplace.parser.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.usertype.UserType;

import javax.security.auth.Subject;
import java.security.Principal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true, name = "password_hash")
    private String passwordHash;

    @Column(nullable = false)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Column(unique = true)
    private String sessionId;

    @Column(name = "is_registered")
    private boolean isRegistered = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public String getName() {
        return email;
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }
}
