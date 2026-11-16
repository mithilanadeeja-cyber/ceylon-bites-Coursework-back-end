package lk.ceylonbites.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA entity. Controllers expose DTOs, never persistence entities. */
@Entity
@Table(name = "food_items")
@Getter
@Setter
@NoArgsConstructor
public class FoodItem {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(length = 36)
  private String id;

  @Column(nullable = false, length = 80)
  private String name;

  @Column(nullable = false, length = 300)
  private String description;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal price;

  @Column(nullable = false, length = 500)
  private String image;

  @Column(nullable = false)
  private boolean vegetarian;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private FoodStatus status = FoodStatus.AVAILABLE;

  @Column(nullable = false)
  private boolean deleted = false;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;
}
