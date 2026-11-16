package lk.ceylonbites.service;

import java.math.BigDecimal;
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
public class OrderService {
  private final CustomerOrderRepository orders;
  private final CartService carts;
  private final AuthService auth;

  /** Lock the customer's cart: create order/payment and clear cart in ONE transaction. */
  @Transactional
  public OrderView place(String userId, Checkout in, String requestKey) {
    if (requestKey == null || !requestKey.matches("[A-Za-z0-9-]{8,64}"))
      throw ApiException.bad(
          "Provide an Idempotency-Key header of 8–64 letters, digits or hyphens");
    var cart = carts.locked(userId);
    var previous = orders.findByUserIdAndRequestKey(userId, requestKey);
    if (previous.isPresent()) return Views.order(previous.get());
    if (cart.getItems().isEmpty()) throw ApiException.bad("Your cart is empty");
    var o = new CustomerOrder();
    o.setUser(cart.getUser());
    o.setAddress(in.address().trim());
    o.setPhone(in.phone());
    o.setRequestKey(requestKey);
    BigDecimal subtotal = BigDecimal.ZERO;
    for (var ci : cart.getItems()) {
      var f = ci.getFood();
      if (f.isDeleted() || f.getStatus() != FoodStatus.AVAILABLE)
        throw ApiException.conflict(f.getName() + " is no longer available");
      var line = new OrderItem();
      line.setOrder(o);
      line.setFood(f);
      line.setFoodName(f.getName());
      line.setImage(f.getImage());
      line.setUnitPrice(f.getPrice());
      line.setQuantity(ci.getQuantity());
      o.getItems().add(line);
      subtotal = subtotal.add(f.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
    }
    o.setSubtotal(subtotal);
    o.setDeliveryFee(new BigDecimal("250.00"));
    o.setTotal(subtotal.add(o.getDeliveryFee()));
    var payment = new Payment();
    payment.setOrder(o);
    payment.setAmount(o.getTotal());
    payment.setMethod(in.paymentMethod());
    o.setPayment(payment);
    orders.saveAndFlush(o);
    cart.getItems().clear();
    log.info("Order placed orderId={} userId={} total={}", o.getId(), userId, o.getTotal());
    return Views.order(o);
  }

  @Transactional(readOnly = true)
  public List<OrderView> mine(String userId) {
    auth.current(userId);
    return orders.findByUserIdOrderByCreatedAtDesc(userId).stream().map(Views::order).toList();
  }

  @Transactional(readOnly = true)
  public List<OrderView> all() {
    return orders.findAllByOrderByCreatedAtDesc().stream().map(Views::order).toList();
  }

  @Transactional(readOnly = true)
  public OrderView one(String userId, String id) {
    var u = auth.current(userId);
    var o = orders.findById(id).orElseThrow(() -> ApiException.notFound("Order"));
    if (u.getRole() != Role.ADMIN && !o.getUser().getId().equals(userId))
      throw ApiException.notFound("Order");
    return Views.order(o);
  }

  @Transactional
  public OrderView status(String id, OrderStatus target) {
    var o = orders.lockById(id).orElseThrow(() -> ApiException.notFound("Order"));
    if (o.getStatus() == target) return Views.order(o);
    boolean allowed =
        switch (o.getStatus()) {
          case PLACED -> target == OrderStatus.PREPARING || target == OrderStatus.CANCELLED;
          case PREPARING -> target == OrderStatus.DELIVERED || target == OrderStatus.CANCELLED;
          case DELIVERED, CANCELLED -> false;
        };
    if (!allowed) throw ApiException.conflict("Invalid order status transition");
    if (target == OrderStatus.CANCELLED && o.getPayment().getStatus() == PaymentStatus.COMPLETED)
      throw ApiException.conflict("Cannot cancel an order with a completed payment");
    o.setStatus(target);
    if (target == OrderStatus.CANCELLED) {
      o.getPayment().setStatus(PaymentStatus.FAILED);
      o.getPayment().setUpdatedAt(Instant.now());
    }
    log.info("Order status updated orderId={} status={}", id, target);
    return Views.order(o);
  }

  @Transactional
  public OrderView cancel(String userId, String id) {
    var o = orders.lockById(id).orElseThrow(() -> ApiException.notFound("Order"));
    if (!o.getUser().getId().equals(userId)) throw ApiException.notFound("Order");
    if (o.getStatus() != OrderStatus.PLACED)
      throw ApiException.conflict("Only newly placed orders can be cancelled by customers");
    return status(id, OrderStatus.CANCELLED);
  }
}
