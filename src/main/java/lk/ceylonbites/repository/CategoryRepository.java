package lk.ceylonbites.repository;

import java.util.*;
import lk.ceylonbites.entity.Category;
import org.springframework.data.jpa.repository.*;

public interface CategoryRepository extends JpaRepository<Category, String> {
  boolean existsByNameIgnoreCase(String name);

  List<Category> findAllByOrderByNameAsc();
}
