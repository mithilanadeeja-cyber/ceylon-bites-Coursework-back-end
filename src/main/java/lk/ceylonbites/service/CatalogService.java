package lk.ceylonbites.service;

import java.util.List;
import lk.ceylonbites.dto.Dtos.*;
import lk.ceylonbites.entity.*;
import lk.ceylonbites.exception.ApiException;
import lk.ceylonbites.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {
  private final FoodItemRepository foods;
  private final CategoryRepository categories;
  private final CartItemRepository cartItems;

  @Transactional(readOnly = true)
  public List<FoodView> foods(String categoryId, String search) {
    return foods.findByDeletedFalseOrderByNameAsc().stream()
        .filter(f -> categoryId == null || f.getCategory().getId().equals(categoryId))
        .filter(f -> search == null || f.getName().toLowerCase().contains(search.toLowerCase()))
        .map(Views::food)
        .toList();
  }

  @Transactional(readOnly = true)
  public FoodView food(String id) {
    return Views.food(requireFood(id));
  }

  public FoodItem requireFood(String id) {
    return foods.findByIdAndDeletedFalse(id).orElseThrow(() -> ApiException.notFound("Food"));
  }

  @Transactional
  public FoodView saveFood(String id, FoodInput in) {
    var f = id == null ? new FoodItem() : requireFood(id);
    var c =
        categories.findById(in.categoryId()).orElseThrow(() -> ApiException.notFound("Category"));
    f.setName(in.name().trim());
    f.setDescription(in.description().trim());
    f.setPrice(in.price());
    f.setImage(in.image());
    f.setVegetarian(in.vegetarian());
    f.setStatus(in.status());
    f.setCategory(c);
    foods.save(f);
    log.info("Food saved foodId={}", f.getId());
    return Views.food(f);
  }

  /** Keep referenced food rows for immutable historical order lines. */
  @Transactional
  public void deleteFood(String id) {
    var f = requireFood(id);
    f.setDeleted(true);
    f.setStatus(FoodStatus.OUT_OF_STOCK);
    cartItems.deleteByFoodId(id);
    log.info("Food archived foodId={}", id);
  }

  @Transactional(readOnly = true)
  public List<CategoryView> categories() {
    return categories.findAllByOrderByNameAsc().stream().map(Views::category).toList();
  }

  @Transactional
  public CategoryView saveCategory(String id, NameInput in) {
    String name = in.name().trim();
    var c =
        id == null
            ? new Category()
            : categories.findById(id).orElseThrow(() -> ApiException.notFound("Category"));
    if ((id == null || !c.getName().equalsIgnoreCase(name))
        && categories.existsByNameIgnoreCase(name))
      throw ApiException.conflict("Category name already exists");
    c.setName(name);
    return Views.category(categories.save(c));
  }

  @Transactional
  public void deleteCategory(String id) {
    var c = categories.findById(id).orElseThrow(() -> ApiException.notFound("Category"));
    if (!c.getFoods().isEmpty())
      throw ApiException.conflict(
          "This category is referenced by food items; move them to another category first");
    categories.delete(c);
  }
}
