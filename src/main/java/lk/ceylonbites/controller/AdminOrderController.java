package lk.ceylonbites.controller;

import java.util.List;
import lk.ceylonbites.dto.Dtos.*;
import lk.ceylonbites.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
  private final OrderService service;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<OrderView> all() {
    return service.all();
  }
}
