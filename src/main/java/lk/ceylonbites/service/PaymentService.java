package lk.ceylonbites.service;

import java.time.Instant;
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
public class PaymentService {
  private final PaymentRepository payments;
  private final CustomerOrderRepository orders;
  private final AuthService auth;

  @Transactional(readOnly = true)
  public List<PaymentView> list(String userId) {
    var u = auth.current(userId);
    return (u.getRole() == Role.ADMIN
            ? payments.findAllByOrderByCreatedAtDesc()
            : payments.findByOrderUserIdOrderByCreatedAtDesc(userId))
        .stream().map(Views::payment).toList();
  }

  @Transactional(readOnly = true)
  public PaymentView one(String userId, String id) {
    var p = payments.findById(id).orElseThrow(() -> ApiException.notFound("Payment"));
    var u = auth.current(userId);
    if (u.getRole() != Role.ADMIN && !p.getOrder().getUser().getId().equals(userId))
      throw ApiException.notFound("Payment");
    return Views.payment(p);
  }

  /** Cash collection is recorded by an administrator, never by a customer. */
  @Transactional
  public PaymentView update(String id, PaymentStatus target) {
    var initial = payments.findById(id).orElseThrow(() -> ApiException.notFound("Payment"));
    var o =
        orders
            .lockById(initial.getOrder().getId())
            .orElseThrow(() -> ApiException.notFound("Order"));
    var p = o.getPayment();
    if (p.getStatus() == target) return Views.payment(p);
    if (p.getStatus() != PaymentStatus.PENDING)
      throw ApiException.conflict("Finalized payments cannot be changed");
    if (target == PaymentStatus.PENDING) throw ApiException.bad("Select COMPLETED or FAILED");
    if (target == PaymentStatus.COMPLETED && o.getStatus() != OrderStatus.DELIVERED)
      throw ApiException.conflict("Record cash collection only after delivery");
    if (o.getStatus() == OrderStatus.CANCELLED)
      throw ApiException.conflict("A cancelled order cannot be paid");
    p.setStatus(target);
    p.setUpdatedAt(Instant.now());
    log.info("Payment status updated paymentId={} status={}", id, target);
    return Views.payment(p);
  }
}
