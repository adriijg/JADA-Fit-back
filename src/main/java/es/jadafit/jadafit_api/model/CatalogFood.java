package es.jadafit.jadafit_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "catalog_foods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogFood {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "external_id", unique = true)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(name = "calories_per_100g", nullable = false)
    private BigDecimal caloriesPer100g;

    @Builder.Default
    @Column(name = "protein_per_100g", nullable = false)
    private BigDecimal proteinPer100g = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "carbs_per_100g", nullable = false)
    private BigDecimal carbsPer100g = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "fats_per_100g", nullable = false)
    private BigDecimal fatsPer100g = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "is_custom", nullable = false)
    private Boolean isCustom = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}