package lk.ceylonbites.repository;

import java.util.*;
import lk.ceylonbites.entity.User;
import org.springframework.data.jpa.repository.*;

public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
}
