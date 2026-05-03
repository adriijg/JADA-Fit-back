package es.jadafit.jadafit_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "catalog_foods",
        indexes = {
                @Index(name = "idx_catalog_foods_name", columnList = "name"),
                @Index(name = "idx_catalog_foods_barcode", columnList = "barcode"),
                @Index(name = "idx_catalog_foods_owner_user_id", columnList = "owner_user_id")
        }
)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User ownerUser;

    @Column(name = "external_food_id")
    private String externalFoodId;

    @Column(name = "barcode")
    private String barcode;

    @Column(nullable = false)
    private String name;

    @Column
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodSource source;

    @Column(name = "calories_per_100g", nullable = false)
    private BigDecimal caloriesPer100g;

    @Column(name = "protein_per_100g", nullable = false)
    private BigDecimal proteinPer100g;

    @Column(name = "carbs_per_100g", nullable = false)
    private BigDecimal carbsPer100g;

    @Column(name = "fats_per_100g", nullable = false)
    private BigDecimal fatsPer100g;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}