package lk.ceylonbites.service;

import lk.ceylonbites.dto.Dtos.*;
import lk.ceylonbites.entity.*;
import lk.ceylonbites.exception.ApiException;
import lk.ceylonbites.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {
  private final CartRepository carts;
  private final CatalogService catalog;
  private final AuthService auth;

  public Cart locked(String userId) {
    auth.current(userId);
    return carts.lockByUserId(userId).orElseThrow(() -> ApiException.notFound("Cart"));
  }

  @Transactional(readOnly = true)
  public CartView get(String userId) {
    auth.current(userId);
    return Views.cart(carts.findByUserId(userId).orElseThrow(() -> ApiException.notFound("Cart")));
  }

  @Transactional
  public CartView add(String userId, CartInput input) {
    var cart = locked(userId);
    var food = catalog.requireFood(input.foodId());
    if (food.getStatus() != FoodStatus.AVAILABLE)
      throw ApiException.conflict("Food is out of stock");
    var line =
        cart.getItems().stream()
            .filter(i -> i.getFood().getId().equals(food.getId()))
            .findFirst()
            .orElse(null);
    int quantity = input.quantity() + (line == null ? 0 : line.getQuantity());
    if (quantity > 20) throw ApiException.bad("Maximum quantity is 20 per dish");
    if (line == null) {
      line = new CartItem();
      line.setCart(cart);
      line.setFood(food);
      cart.getItems().add(line);
    }
    line.setQuantity(quantity);
    carts.saveAndFlush(cart);
    return Views.cart(cart);
  }

  @Transactional
  public CartView quantity(String userId, String foodId, int quantity) {
    var c = locked(userId);
    var item =
        c.getItems().stream()
            .filter(i -> i.getFood().getId().equals(foodId))
            .findFirst()
            .orElseThrow(() -> ApiException.notFound("Cart item"));
    if (item.getFood().getStatus() != FoodStatus.AVAILABLE)
      throw ApiException.conflict("Food is out of stock");
    item.setQuantity(quantity);
    return Views.cart(c);
  }

  @Transactional
  public CartView remove(String userId, String foodId) {
    var c = locked(userId);
    if (!c.getItems().removeIf(i -> i.getFood().getId().equals(foodId)))
      throw ApiException.notFound("Cart item");
    return Views.cart(c);
  }

  @Transactional
  public void clear(String userId) {
    locked(userId).getItems().clear();
  }
}
