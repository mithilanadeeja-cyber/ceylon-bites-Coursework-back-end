package lk.ceylonbites.repository;

import java.util.*;
import lk.ceylonbites.entity.FoodItem;
import org.springframework.data.jpa.repository.*;

public interface FoodItemRepository extends JpaRepository<FoodItem, String> {
  List<FoodItem> findByDeletedFalseOrderByNameAsc();

  Optional<FoodItem> findByIdAndDeletedFalse(String id);

  boolean existsByCategoryIdAndDeletedFalse(String categoryId);
}
