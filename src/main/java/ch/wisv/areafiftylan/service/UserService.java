package ch.wisv.areafiftylan.service;



import ch.wisv.areafiftylan.dto.UserDTO;
import ch.wisv.areafiftylan.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserService {
    Optional<User> getUserById(long id);

    Optional<User> getUserByEmail(String email);

    Collection<User> getAllUsers();

    User create(UserDTO userDTO);

    User save(User user);

}
