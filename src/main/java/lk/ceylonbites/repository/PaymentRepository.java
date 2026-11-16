package lk.ceylonbites.repository;

import java.util.*;
import lk.ceylonbites.entity.Payment;
import org.springframework.data.jpa.repository.*;

public interface PaymentRepository extends JpaRepository<Payment, String> {
  List<Payment> findByOrderUserIdOrderByCreatedAtDesc(String userId);

  List<Payment> findAllByOrderByCreatedAtDesc();
}
