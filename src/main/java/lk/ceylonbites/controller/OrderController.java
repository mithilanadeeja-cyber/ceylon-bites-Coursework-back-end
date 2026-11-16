package lk.ceylonbites.controller;

import jakarta.validation.Valid;
import java.util.List;
import lk.ceylonbites.dto.Dtos.*;
import lk.ceylonbites.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
  private final OrderService service;

  @GetMapping
  public List<OrderView> list(@AuthenticationPrincipal Jwt jwt) {
    return service.mine(jwt.getSubject());
  }

  @GetMapping("/{id}")
  public OrderView one(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
    return service.one(jwt.getSubject(), id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrderView place(
      @AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody Checkout input,
      @RequestHeader(value = "Idempotency-Key", required = false) String key) {
    return service.place(jwt.getSubject(), input, key);
  }

  @PutMapping("/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public OrderView update(@PathVariable String id, @Valid @RequestBody StatusInput input) {
    return service.status(id, input.status());
  }

  @PutMapping("/{id}/cancel")
  public OrderView cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
    return service.cancel(jwt.getSubject(), id);
  }
}
