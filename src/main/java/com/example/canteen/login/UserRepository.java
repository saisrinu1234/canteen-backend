package com.example.canteen.login;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User1, UUID> {
    Optional<User1> findByEmail(String email);

    boolean existsByEmail(String email);

}
