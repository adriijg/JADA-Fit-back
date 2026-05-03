package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.CatalogFood;
import es.jadafit.jadafit_api.model.FoodSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogFoodRepository extends JpaRepository<CatalogFood, UUID> {

    Optional<CatalogFood> findFirstByBarcodeAndSource(
            String barcode,
            FoodSource source
    );

    Optional<CatalogFood> findFirstByExternalFoodIdAndSource(
            String externalFoodId,
            FoodSource source
    );

    Optional<CatalogFood> findByIdAndOwnerUserId(
            UUID id,
            UUID ownerUserId
    );

    @Query("""
            select food
            from CatalogFood food
            where lower(food.name) like lower(concat('%', :query, '%'))
            and (food.ownerUser.id = :userId or food.ownerUser is null)
            order by food.name asc
            """)
    List<CatalogFood> searchAvailableFoods(
            String query,
            UUID userId
    );

    List<CatalogFood> findByOwnerUserIdOrderByCreatedAtDesc(UUID ownerUserId);
}