package lk.ceylonbites.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA entity. Controllers expose DTOs, never persistence entities. */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(length = 36)
  private String id;

  @Column(nullable = false, unique = true, length = 80)
  private String name;

  @OneToMany(mappedBy = "category")
  private List<FoodItem> foods = new ArrayList<>();
}
