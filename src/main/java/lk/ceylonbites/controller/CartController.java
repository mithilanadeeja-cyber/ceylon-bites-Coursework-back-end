package lk.ceylonbites.controller;

import jakarta.validation.Valid;
import lk.ceylonbites.dto.Dtos.*;
import lk.ceylonbites.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
  private final CartService service;

  @GetMapping
  public CartView get(@AuthenticationPrincipal Jwt jwt) {
    return service.get(jwt.getSubject());
  }

  @PostMapping("/items")
  public CartView add(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CartInput input) {
    return service.add(jwt.getSubject(), input);
  }

  @PutMapping("/items/{foodId}")
  public CartView quantity(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable String foodId,
      @Valid @RequestBody QuantityInput input) {
    return service.quantity(jwt.getSubject(), foodId, input.quantity());
  }

  @DeleteMapping("/items/{foodId}")
  public CartView remove(@AuthenticationPrincipal Jwt jwt, @PathVariable String foodId) {
    return service.remove(jwt.getSubject(), foodId);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void clear(@AuthenticationPrincipal Jwt jwt) {
    service.clear(jwt.getSubject());
  }
}
