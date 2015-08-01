package a5l.Service;

import a5l.Model.User;
import a5l.DTO.UserDTO;

import java.util.Collection;
import java.util.Optional;

public interface UserService {
    Optional<User> getUserById(long id);

    Optional<User> getUserByEmail(String email);

    Collection<User> getAllUsers();

    User create(UserDTO userDTO);

    User save(User user);

}
