package lk.ceylonbites.controller;

import jakarta.validation.Valid;
import lk.ceylonbites.dto.Dtos.*;
import lk.ceylonbites.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService service;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserView register(@Valid @RequestBody Register input) {
    return service.register(input);
  }

  @PostMapping("/login")
  public AuthView login(@Valid @RequestBody Login input) {
    return service.login(input);
  }
}
