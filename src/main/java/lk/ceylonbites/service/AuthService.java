package lk.ceylonbites.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import lk.ceylonbites.dto.Dtos.*;
import lk.ceylonbites.entity.*;
import lk.ceylonbites.exception.ApiException;
import lk.ceylonbites.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
  private final UserRepository users;
  private final CartRepository carts;
  private final PasswordEncoder passwords;
  private final JwtEncoder encoder;

  @Value("${app.jwt.issuer}")
  private String issuer;

  @Value("${app.jwt.ttl-minutes}")
  private long ttl;

  @Transactional
  public UserView register(Register input) {
    String email = input.email().trim().toLowerCase(Locale.ROOT);
    if (users.existsByEmail(email)) throw ApiException.conflict("Email is already registered");
    if (input.password().getBytes(StandardCharsets.UTF_8).length > 72)
      throw ApiException.bad("Password must not exceed 72 UTF-8 bytes");
    var u = new User();
    u.setName(input.name().trim());
    u.setEmail(email);
    u.setPasswordHash(passwords.encode(input.password()));
    u.setRole(Role.CUSTOMER);
    users.saveAndFlush(u);
    var cart = new Cart();
    cart.setUser(u);
    carts.save(cart);
    log.info("Customer registered userId={}", u.getId());
    return Views.user(u);
  }

  @Transactional(readOnly = true)
  public AuthView login(Login input) {
    var u =
        users
            .findByEmail(input.email().trim().toLowerCase(Locale.ROOT))
            .orElseThrow(
                () -> new ApiException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect"));
    if (!u.isEnabled() || !passwords.matches(input.password(), u.getPasswordHash()))
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect");
    Instant now = Instant.now();
    var claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(ttl * 60))
            .subject(u.getId())
            .claim("role", u.getRole().name())
            .build();
    String token =
        encoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
    log.info("Sign in successful userId={}", u.getId());
    return new AuthView(token, Views.user(u), ttl * 60);
  }

  @Transactional(readOnly = true)
  public User current(String id) {
    return users
        .findById(id)
        .filter(User::isEnabled)
        .orElseThrow(
            () -> new ApiException(HttpStatus.UNAUTHORIZED, "User session is no longer valid"));
  }

  @Transactional(readOnly = true)
  public UserView me(String id) {
    return Views.user(current(id));
  }

  @Transactional
  public UserView profile(String id, NameInput input) {
    var u = current(id);
    u.setName(input.name().trim());
    return Views.user(u);
  }

  @Transactional(readOnly = true)
  public java.util.List<UserView> list() {
    return users.findAll().stream().map(Views::user).toList();
  }
}
