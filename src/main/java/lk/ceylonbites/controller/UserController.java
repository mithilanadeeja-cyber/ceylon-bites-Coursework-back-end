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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final AuthService service;

  @GetMapping("/me")
  public UserView me(@AuthenticationPrincipal Jwt jwt) {
    return service.me(jwt.getSubject());
  }

  @PutMapping("/me")
  public UserView profile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody NameInput input) {
    return service.profile(jwt.getSubject(), input);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<UserView> users() {
    return service.list();
  }
}
