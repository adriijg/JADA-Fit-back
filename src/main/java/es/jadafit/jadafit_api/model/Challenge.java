package es.jadafit.jadafit_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "challenges", indexes = {
        @Index(name = "idx_challenges_challenger_id", columnList = "challenger_id"),
        @Index(name = "idx_challenges_challenged_id", columnList = "challenged_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenger_id", nullable = false)
    private User challenger;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenged_id", nullable = false)
    private User challenged;

    @Column(name = "exercise_name", nullable = false)
    private String exerciseName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status;

    @Column(name = "challenger_weight", precision = 8, scale = 3)
    private BigDecimal challengerWeight;

    @Column(name = "challenged_weight", precision = 8, scale = 3)
    private BigDecimal challengedWeight;

    @Column(name = "challenger_start_weight", precision = 8, scale = 3)
    private BigDecimal challengerStartWeight;

    @Column(name = "challenged_start_weight", precision = 8, scale = 3)
    private BigDecimal challengedStartWeight;

    @Builder.Default
    @Column(name = "target_increase_kg", precision = 8, scale = 3)
    private BigDecimal targetIncreaseKg = BigDecimal.TEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "expires_at")
    private LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = createdAt.plusDays(7);
        }
        if (targetIncreaseKg == null) {
            targetIncreaseKg = BigDecimal.TEN;
        }
    }

    @Version
    private Long version;
}
