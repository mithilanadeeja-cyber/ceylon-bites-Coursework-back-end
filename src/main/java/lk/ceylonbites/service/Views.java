package lk.ceylonbites.service;

import java.math.BigDecimal;
import lk.ceylonbites.dto.Dtos.*;
import lk.ceylonbites.entity.*;

/** Maps entities while the service transaction is open, avoiding lazy-load leaks. */
public final class Views {
  private Views() {}

  public static UserView user(User u) {
    return new UserView(u.getId(), u.getName(), u.getEmail(), u.getRole());
  }

  public static CategoryView category(Category c) {
    return new CategoryView(c.getId(), c.getName());
  }

  public static FoodView food(FoodItem f) {
    return new FoodView(
        f.getId(),
        f.getName(),
        f.getDescription(),
        f.getPrice(),
        f.getCategory().getId(),
        f.getImage(),
        f.isVegetarian(),
        f.getStatus());
  }

  public static CartView cart(Cart c) {
    var items =
        c.getItems().stream()
            .map(i -> new CartLine(i.getId(), food(i.getFood()), i.getQuantity()))
            .toList();
    var subtotal =
        c.getItems().stream()
            .map(i -> i.getFood().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    var fee = items.isEmpty() ? BigDecimal.ZERO : new BigDecimal("250.00");
    return new CartView(c.getId(), items, subtotal, fee, subtotal.add(fee));
  }

  public static OrderView order(CustomerOrder o) {
    return new OrderView(
        o.getId(),
        o.getUser().getId(),
        o.getItems().stream()
            .map(
                i ->
                    new OrderLine(
                        i.getId(),
                        i.getFood().getId(),
                        i.getFoodName(),
                        i.getImage(),
                        i.getUnitPrice(),
                        i.getQuantity()))
            .toList(),
        o.getSubtotal(),
        o.getDeliveryFee(),
        o.getTotal(),
        o.getAddress(),
        o.getPhone(),
        o.getStatus(),
        o.getPayment().getMethod(),
        o.getPayment().getStatus(),
        o.getPayment().getId(),
        o.getCreatedAt());
  }

  public static PaymentView payment(Payment p) {
    return new PaymentView(
        p.getId(),
        p.getOrder().getId(),
        p.getAmount(),
        p.getMethod(),
        p.getStatus(),
        p.getCreatedAt());
  }
}
