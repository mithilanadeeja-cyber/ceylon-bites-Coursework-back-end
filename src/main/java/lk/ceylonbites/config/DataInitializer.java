package lk.ceylonbites.config;

import java.math.BigDecimal;
import lk.ceylonbites.entity.*;
import lk.ceylonbites.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {
  private final UserRepository users;
  private final CartRepository carts;
  private final CategoryRepository categories;
  private final FoodItemRepository foods;
  private final PasswordEncoder encoder;

  @Value("${app.seed.admin-email}")
  private String adminEmail;

  @Value("${app.seed.admin-password}")
  private String adminPassword;

  @Override
  @Transactional
  public void run(String... args) {
    if (!users.existsByEmail(adminEmail.toLowerCase())) {
      if (adminPassword.length() < 12)
        throw new IllegalStateException(
            "Set ADMIN_PASSWORD to at least 12 characters for first startup");
      var u = new User();
      u.setName("Restaurant Admin");
      u.setEmail(adminEmail.toLowerCase());
      u.setPasswordHash(encoder.encode(adminPassword));
      u.setRole(Role.ADMIN);
      users.save(u);
      var c = new Cart();
      c.setUser(u);
      carts.save(c);
      log.info("Initial administrator created");
    }
    if (categories.count() == 0) {
      String[] names = {"Rice & curry", "Kottu", "Burgers", "Sides", "Drinks"};
      var cs = new java.util.ArrayList<Category>();
      for (String name : names) {
        var c = new Category();
        c.setName(name);
        cs.add(categories.save(c));
      }
      add(
          cs.get(0),
          "Signature chicken rice",
          "Fragrant rice, slow-cooked chicken and island spices.",
          1250,
          "photo-1512058564366-18510be2db19",
          false);
      add(
          cs.get(1),
          "Classic chicken kottu",
          "Chopped roti, tender chicken and our house curry.",
          1100,
          "photo-1547592180-85f173990554",
          false);
      add(
          cs.get(2),
          "The crispy chicken burger",
          "Golden chicken, crunchy slaw and a little heat.",
          1350,
          "photo-1568901346375-23c9450c58cd",
          false);
      add(
          cs.get(0),
          "Garden harvest bowl",
          "Seasonal vegetables and fragrant rice.",
          950,
          "photo-1512621776951-a57141f2eefd",
          true);
      add(
          cs.get(3),
          "Golden loaded fries",
          "Crispy fries with our signature herb seasoning.",
          650,
          "photo-1573080496219-bb080dd4f877",
          true);
      add(
          cs.get(4),
          "Fresh lime cooler",
          "Freshly squeezed lime, mint and a splash of sunshine.",
          450,
          "photo-1513558161293-cdaf765edfd7",
          true);
    }
  }

  private void add(
      Category category, String name, String description, int price, String image, boolean veg) {
    var f = new FoodItem();
    f.setCategory(category);
    f.setName(name);
    f.setDescription(description);
    f.setPrice(BigDecimal.valueOf(price));
    f.setImage(image);
    f.setVegetarian(veg);
    foods.save(f);
  }
}
