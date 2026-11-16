package lk.ceylonbites.controller;

import jakarta.validation.Valid;
import java.util.List;
import lk.ceylonbites.dto.Dtos.*;
import lk.ceylonbites.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
  private final PaymentService service;

  @GetMapping
  public List<PaymentView> list(@AuthenticationPrincipal Jwt jwt) {
    return service.list(jwt.getSubject());
  }

  @GetMapping("/{id}")
  public PaymentView one(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
    return service.one(jwt.getSubject(), id);
  }

  @PutMapping("/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public PaymentView update(@PathVariable String id, @Valid @RequestBody PaymentInput input) {
    return service.update(id, input.status());
  }
}
