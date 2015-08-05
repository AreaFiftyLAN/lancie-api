package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.UserDTO;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
    public User create(UserDTO userDTO) {
        String passwordHash = new BCryptPasswordEncoder().encode(userDTO.getPassword());
        User user = new User(userDTO.getUsername(), passwordHash,  userDTO.getEmail());
        user.setRole(userDTO.getRole());

        return userRepository.saveAndFlush(user);
    }

    @Override
    public User save(User user) {
        return userRepository.saveAndFlush(user);
    }

}