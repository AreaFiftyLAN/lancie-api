package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findOneByEmail(String email);

}
