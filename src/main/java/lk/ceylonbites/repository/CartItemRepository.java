package lk.ceylonbites.repository;

import java.util.*;
import lk.ceylonbites.entity.CartItem;
import org.springframework.data.jpa.repository.*;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
  void deleteByFoodId(String foodId);
}
