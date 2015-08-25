package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.UserDTO;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.Collection;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> getUserById(long id) {
        return Optional.ofNullable(userRepository.findOne(id));
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findOneByEmail(email);
    }

    @Override
    public Collection<User> getAllUsers() {
        return userRepository.findAll(new Sort("email"));
    }

    @Override
    public String getCurrent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        return name;
    }

    @Override
    public User create(UserDTO userDTO) {
        String passwordHash = getPasswordHash(userDTO.getPassword());
        User user = new User(userDTO.getUsername(), passwordHash, userDTO.getEmail());
        user.setRole(userDTO.getRole());

        return userRepository.saveAndFlush(user);
    }

    @Override
    public User replace(Long userId, UserDTO userDTO) {
        User user = userRepository.getOne(userId);
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPasswordHash(getPasswordHash(userDTO.getPassword()));
        user.resetProfile();
        return userRepository.saveAndFlush(user);
    }

    @Override
    public void delete(Long userId) {
        userRepository.delete(userId);
    }

    @Override
    public User edit(Long userId, UserDTO userDTO) {
        User user = userRepository.findOne(userId);
        if (!Strings.isNullOrEmpty(userDTO.getUsername())) {
            user.setUsername(userDTO.getUsername());
        }
        if (!Strings.isNullOrEmpty(userDTO.getEmail())) {
            user.setEmail(userDTO.getEmail());
        }
        if (!Strings.isNullOrEmpty(userDTO.getPassword())) {
            user.setPasswordHash(getPasswordHash(userDTO.getPassword()));
        }
        return userRepository.saveAndFlush(user);

    }

    @Override
    public User save(User user) {
        return userRepository.saveAndFlush(user);
    }

    private String getPasswordHash(String plainTextPassword) {
        return new BCryptPasswordEncoder().encode(plainTextPassword);
    }

}