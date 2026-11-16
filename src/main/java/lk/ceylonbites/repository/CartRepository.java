package lk.ceylonbites.repository;

import jakarta.persistence.LockModeType;
import java.util.*;
import lk.ceylonbites.entity.Cart;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, String> {
  Optional<Cart> findByUserId(String userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from Cart c where c.user.id = :userId")
  Optional<Cart> lockByUserId(@Param("userId") String userId);
}
