package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.FoodResponseDTO;
import es.jadafit.jadafit_api.service.FoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<FoodResponseDTO> getFoodByBarcode(
            @PathVariable String barcode
    ) {
        FoodResponseDTO response = foodService.getFoodByBarcode(barcode);
        return ResponseEntity.ok(response);
    }
}