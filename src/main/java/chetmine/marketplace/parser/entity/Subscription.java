package chetmine.marketplace.parser.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "subscriptions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column
    private SubscriptionPlan plan = SubscriptionPlan.FREE;

    @Column(nullable = false)
    private Integer requestsUsed = 0;

    @Column(nullable = false)
    private Integer requestsLimit = 0;

    @Column(nullable = false)
    private Instant resetAt = Instant.now().plusSeconds(30 * 24 * 60 * 60);
}
