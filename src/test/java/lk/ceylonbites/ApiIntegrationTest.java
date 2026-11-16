package lk.ceylonbites;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import java.math.BigDecimal;
import java.util.*;
import lk.ceylonbites.entity.*;
import lk.ceylonbites.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired UserRepository users;
  @Autowired CartRepository carts;
  @Autowired CategoryRepository categories;
  @Autowired FoodItemRepository foods;
  @Autowired PasswordEncoder encoder;
  @Autowired CustomerOrderRepository orders;
  String customer, admin, other, foodId, categoryId, customerId;

  JsonNode body(MvcResult r) throws Exception {
    return json.readTree(r.getResponse().getContentAsString());
  }

  String token(String email, String password) throws Exception {
    return body(mvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(Map.of("email", email, "password", password))))
            .andExpect(status().isOk())
            .andReturn())
        .get("accessToken")
        .asText();
  }

  String register(String email) throws Exception {
    return body(mvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        json.writeValueAsString(
                            Map.of(
                                "name",
                                "Test Customer",
                                "email",
                                email,
                                "password",
                                "TestPass123!"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("role").value("CUSTOMER"))
            .andExpect(jsonPath("passwordHash").doesNotExist())
            .andReturn())
        .get("id")
        .asText();
  }

  @BeforeEach
  void setup() throws Exception {
    String suffix = UUID.randomUUID().toString();
    String email = "customer-" + suffix + "@test.com";
    customerId = register(email);
    customer = token(email, "TestPass123!");
    String otherEmail = "other-" + suffix + "@test.com";
    register(otherEmail);
    other = token(otherEmail, "TestPass123!");
    var u = new User();
    u.setName("Admin");
    u.setEmail("admin-" + suffix + "@test.com");
    u.setPasswordHash(encoder.encode("AdminTest123!"));
    u.setRole(Role.ADMIN);
    users.save(u);
    var c = new Cart();
    c.setUser(u);
    carts.save(c);
    admin = token(u.getEmail(), "AdminTest123!");
    var category = new Category();
    category.setName("Category " + suffix);
    categoryId = categories.save(category).getId();
    var f = new FoodItem();
    f.setName("Test Rice");
    f.setDescription("Testing rice dish");
    f.setPrice(new BigDecimal("1000.00"));
    f.setImage("photo-test");
    f.setCategory(category);
    foodId = foods.save(f).getId();
  }

  void add(int quantity) throws Exception {
    mvc.perform(
            post("/api/cart/items")
                .header("Authorization", "Bearer " + customer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("foodId", foodId, "quantity", quantity))))
        .andExpect(status().isOk());
  }

  JsonNode order(String key) throws Exception {
    return body(
        mvc.perform(
                post("/api/orders")
                    .header("Authorization", "Bearer " + customer)
                    .header("Idempotency-Key", key)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"address\":\"10 Test Street, Demo"
                            + " City\",\"phone\":\"0700000000\",\"paymentMethod\":\"CASH_ON_DELIVERY\"}"))
            .andExpect(status().isCreated())
            .andReturn());
  }

  void setOrderStatus(String id, String status, int expected) throws Exception {
    mvc.perform(
            put("/api/orders/" + id + "/status")
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("status", status))))
        .andExpect(status().is(expected));
  }

  @Test
  void authenticationAndAuthorization() throws Exception {
    assertThat(customer.split("\\.")).hasSize(3);
    assertThat(users.findById(customerId).orElseThrow().getPasswordHash()).startsWith("$2a$");
    mvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/cart").header("Authorization", "Bearer invalid.token.value"))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nobody@test.com\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/api/users").header("Authorization", "Bearer " + customer))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/users").header("Authorization", "Bearer " + admin))
        .andExpect(status().isOk());
    mvc.perform(
            post("/api/categories")
                .header("Authorization", "Bearer " + customer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Not allowed\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void registrationRejectsPrivilegeInjectionAndDuplicates() throws Exception {
    mvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Attacker\",\"email\":\"attacker@test.com\",\"password\":\"Pass12345!\",\"role\":\"ADMIN\"}"))
        .andExpect(status().isBadRequest());
    String email = users.findById(customerId).orElseThrow().getEmail();
    mvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(
                        Map.of("name", "Duplicate", "email", email, "password", "Test12345!"))))
        .andExpect(status().isConflict());
  }

  @Test
  void persistentCartCrudAndIsolation() throws Exception {
    add(2);
    mvc.perform(get("/api/cart").header("Authorization", "Bearer " + customer))
        .andExpect(jsonPath("subtotal").value(2000))
        .andExpect(jsonPath("total").value(2250));
    mvc.perform(get("/api/cart").header("Authorization", "Bearer " + other))
        .andExpect(jsonPath("items").isEmpty());
    mvc.perform(
            put("/api/cart/items/" + foodId)
                .header("Authorization", "Bearer " + customer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":3}"))
        .andExpect(jsonPath("total").value(3250));
    mvc.perform(
            put("/api/cart/items/" + foodId)
                .header("Authorization", "Bearer " + customer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":0}"))
        .andExpect(status().isBadRequest());
    mvc.perform(delete("/api/cart/items/" + foodId).header("Authorization", "Bearer " + customer))
        .andExpect(jsonPath("items").isEmpty());
  }

  @Test
  void transactionalCheckoutAndIdempotency() throws Exception {
    add(2);
    String key = UUID.randomUUID().toString();
    var placed = order(key);
    assertThat(placed.get("total").decimalValue()).isEqualByComparingTo("2250.00");
    assertThat(placed.get("paymentStatus").asText()).isEqualTo("PENDING");
    assertThat(order(key).get("id").asText()).isEqualTo(placed.get("id").asText());
    mvc.perform(get("/api/cart").header("Authorization", "Bearer " + customer))
        .andExpect(jsonPath("items").isEmpty());
    mvc.perform(
            get("/api/orders/" + placed.get("id").asText())
                .header("Authorization", "Bearer " + other))
        .andExpect(status().isNotFound());
    mvc.perform(
            get("/api/payments/" + placed.get("paymentId").asText())
                .header("Authorization", "Bearer " + other))
        .andExpect(status().isNotFound());
  }

  @Test
  void rejectsUnavailableFoodAndPreservesCart() throws Exception {
    add(1);
    var f = foods.findById(foodId).orElseThrow();
    f.setStatus(FoodStatus.OUT_OF_STOCK);
    foods.save(f);
    mvc.perform(
            post("/api/orders")
                .header("Authorization", "Bearer " + customer)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"address\":\"10 Test"
                        + " Street\",\"phone\":\"0700000000\",\"paymentMethod\":\"CASH_ON_DELIVERY\"}"))
        .andExpect(status().isConflict());
    mvc.perform(get("/api/cart").header("Authorization", "Bearer " + customer))
        .andExpect(jsonPath("items.length()").value(1));
  }

  @Test
  void orderTransitionsAndCashPayment() throws Exception {
    add(1);
    var placed = order(UUID.randomUUID().toString());
    String id = placed.get("id").asText(), paymentId = placed.get("paymentId").asText();
    setOrderStatus(id, "DELIVERED", 409);
    setOrderStatus(id, "PREPARING", 200);
    setOrderStatus(id, "DELIVERED", 200);
    mvc.perform(
            put("/api/payments/" + paymentId + "/status")
                .header("Authorization", "Bearer " + customer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}"))
        .andExpect(status().isForbidden());
    mvc.perform(
            put("/api/payments/" + paymentId + "/status")
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("status").value("COMPLETED"));
    setOrderStatus(id, "CANCELLED", 409);
  }

  @Test
  void customerCancellationAndImmutableHistoricalPrices() throws Exception {
    add(1);
    var placed = order(UUID.randomUUID().toString());
    var f = foods.findById(foodId).orElseThrow();
    f.setPrice(new BigDecimal("1500.00"));
    foods.save(f);
    mvc.perform(
            get("/api/orders/" + placed.get("id").asText())
                .header("Authorization", "Bearer " + customer))
        .andExpect(jsonPath("items[0].unitPrice").value(1000));
    mvc.perform(
            put("/api/orders/" + placed.get("id").asText() + "/cancel")
                .header("Authorization", "Bearer " + customer))
        .andExpect(status().isOk())
        .andExpect(jsonPath("status").value("CANCELLED"))
        .andExpect(jsonPath("paymentStatus").value("FAILED"));
  }

  @Test
  void catalogCrudAndProfile() throws Exception {
    String category =
        body(mvc.perform(
                    post("/api/categories")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New " + UUID.randomUUID() + "\"}"))
                .andExpect(status().isCreated())
                .andReturn())
            .get("id")
            .asText();
    mvc.perform(
            put("/api/categories/" + category)
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Renamed " + UUID.randomUUID() + "\"}"))
        .andExpect(status().isOk());
    mvc.perform(delete("/api/categories/" + category).header("Authorization", "Bearer " + admin))
        .andExpect(status().isNoContent());
    mvc.perform(delete("/api/categories/" + categoryId).header("Authorization", "Bearer " + admin))
        .andExpect(status().isConflict());
    mvc.perform(
            put("/api/users/me")
                .header("Authorization", "Bearer " + customer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated Customer\"}"))
        .andExpect(jsonPath("name").value("Updated Customer"));
    mvc.perform(delete("/api/foods/" + foodId).header("Authorization", "Bearer " + admin))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/foods/" + foodId)).andExpect(status().isNotFound());
  }

  @Test
  void rejectsClientPriceAndUnknownCheckoutFields() throws Exception {
    add(1);
    mvc.perform(
            post("/api/orders")
                .header("Authorization", "Bearer " + customer)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"address\":\"10 Test"
                        + " Street\",\"phone\":\"0700000000\",\"paymentMethod\":\"CASH_ON_DELIVERY\",\"total\":1}"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/cart").header("Authorization", "Bearer " + customer))
        .andExpect(jsonPath("items.length()").value(1));
  }
}
