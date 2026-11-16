package lk.ceylonbites.repository;

import java.util.*;
import lk.ceylonbites.entity.OrderItem;
import org.springframework.data.jpa.repository.*;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {}
