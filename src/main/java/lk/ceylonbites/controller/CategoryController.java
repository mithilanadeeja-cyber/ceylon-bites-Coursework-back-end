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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
  private final CatalogService service;

  @GetMapping
  public List<CategoryView> list() {
    return service.categories();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public CategoryView create(@Valid @RequestBody NameInput input) {
    return service.saveCategory(null, input);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public CategoryView update(@PathVariable String id, @Valid @RequestBody NameInput input) {
    return service.saveCategory(id, input);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable String id) {
    service.deleteCategory(id);
  }
}
