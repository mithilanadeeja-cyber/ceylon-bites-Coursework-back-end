package lk.ceylonbites.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lk.ceylonbites.entity.*;

/** Request/response contracts: passwords and entity relationships are never serialized. */
public final class Dtos {
  private Dtos() {}

  public record Register(
      @NotBlank @Size(min = 2, max = 80) String name,
      @NotBlank @Email @Size(max = 160) String email,
      @NotBlank @Size(min = 8, max = 72) String password) {}

  public record Login(@NotBlank @Email String email, @NotBlank @Size(max = 72) String password) {}

  public record UserView(String id, String name, String email, Role role) {}

  public record AuthView(String accessToken, UserView user, long expiresIn) {}

  public record NameInput(@NotBlank @Size(min = 2, max = 80) String name) {}

  public record CategoryView(String id, String name) {}

  public record FoodInput(
      @NotBlank @Size(min = 2, max = 80) String name,
      @NotBlank @Size(max = 300) String description,
      @NotNull @DecimalMin("1.00") @DecimalMax("100000.00") @Digits(integer = 6, fraction = 2)
          BigDecimal price,
      @NotBlank String categoryId,
      @NotBlank @Size(max = 500) String image,
      boolean vegetarian,
      @NotNull FoodStatus status) {}

  public record FoodView(
      String id,
      String name,
      String description,
      BigDecimal price,
      String categoryId,
      String image,
      boolean vegetarian,
      FoodStatus status) {}

  public record CartInput(@NotBlank String foodId, @Min(1) @Max(20) int quantity) {}

  public record QuantityInput(@Min(1) @Max(20) int quantity) {}

  public record CartLine(String id, FoodView food, int quantity) {}

  public record CartView(
      String id,
      List<CartLine> items,
      BigDecimal subtotal,
      BigDecimal deliveryFee,
      BigDecimal total) {}

  public record Checkout(
      @NotBlank @Size(min = 10, max = 300) String address,
      @NotBlank @Pattern(regexp = "[+0-9 ()\\-]{9,20}") String phone,
      @NotNull PaymentMethod paymentMethod) {}

  public record StatusInput(@NotNull OrderStatus status) {}

  public record PaymentInput(@NotNull PaymentStatus status) {}

  public record OrderLine(
      String id,
      String foodId,
      String foodName,
      String image,
      BigDecimal unitPrice,
      int quantity) {}

  public record OrderView(
      String id,
      String userId,
      List<OrderLine> items,
      BigDecimal subtotal,
      BigDecimal deliveryFee,
      BigDecimal total,
      String address,
      String phone,
      OrderStatus status,
      PaymentMethod paymentMethod,
      PaymentStatus paymentStatus,
      String paymentId,
      Instant createdAt) {}

  public record PaymentView(
      String id,
      String orderId,
      BigDecimal amount,
      PaymentMethod method,
      PaymentStatus status,
      Instant createdAt) {}
}
