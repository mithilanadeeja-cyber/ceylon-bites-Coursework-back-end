package lk.ceylonbites.repository;

import jakarta.persistence.LockModeType;
import java.util.*;
import lk.ceylonbites.entity.CustomerOrder;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, String> {
  List<CustomerOrder> findByUserIdOrderByCreatedAtDesc(String userId);

  List<CustomerOrder> findAllByOrderByCreatedAtDesc();

  Optional<CustomerOrder> findByUserIdAndRequestKey(String userId, String requestKey);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from CustomerOrder o where o.id = :id")
  Optional<CustomerOrder> lockById(@Param("id") String id);
}
