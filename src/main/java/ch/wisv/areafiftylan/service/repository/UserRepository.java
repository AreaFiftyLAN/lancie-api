package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findOneByUsername(String username);

    Optional<User> findOneByEmail(String email);

}
