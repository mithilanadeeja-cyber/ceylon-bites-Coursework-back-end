package lk.ceylonbites.controller;

import jakarta.validation.Valid;
import java.util.List;
import lk.ceylonbites.dto.Dtos.*;
import lk.ceylonbites.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {
  private final CatalogService service;

  @GetMapping
  public List<FoodView> list(
      @RequestParam(required = false) String categoryId,
      @RequestParam(required = false) String search) {
    return service.foods(categoryId, search);
  }

  @GetMapping("/{id}")
  public FoodView one(@PathVariable String id) {
    return service.food(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public FoodView create(@Valid @RequestBody FoodInput input) {
    return service.saveFood(null, input);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public FoodView update(@PathVariable String id, @Valid @RequestBody FoodInput input) {
    return service.saveFood(id, input);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable String id) {
    service.deleteFood(id);
  }
}
