package ch.wisv.areafiftylan.service;


import ch.wisv.areafiftylan.dto.ProfileDTO;
import ch.wisv.areafiftylan.dto.UserDTO;
import ch.wisv.areafiftylan.model.Profile;
import ch.wisv.areafiftylan.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Optional;

public interface UserService {
    User getUserById(long id);

    Optional<User> getUserByEmail(String email);

    Optional<User> getUserByUsername(String username);

    Collection<User> getAllUsers();

    User create(UserDTO userDTO, HttpServletRequest request);

    User replace(Long userId, UserDTO userDTO);

    void delete(Long userId);

    User edit(Long userId, UserDTO userDTO);

    User save(User user);

    User addProfile(Long userId, ProfileDTO profileDTO);

    User changeProfile(Long userId, ProfileDTO profileDTO);

    Profile resetProfile(Long userId);

    void lock(Long userId);

    void unlock(Long userId);

    void verify(Long userId);

    void requestResetPassword(User user, HttpServletRequest request);

    void resetPassword(Long userId, String password);

    Boolean checkEmailAvailable(String email);

    Boolean checkUsernameAvailable(String username);
}
