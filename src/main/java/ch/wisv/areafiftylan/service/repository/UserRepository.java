package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PostAuthorize;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @PostAuthorize("returnObject.firstName == principal.username or hasRole('ROLE_ADMIN')")
    Optional<User> findByUsername(String username);

    Optional<User> findOneByEmail(String email);

}
