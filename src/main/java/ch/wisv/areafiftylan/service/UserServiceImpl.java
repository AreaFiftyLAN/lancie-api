package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.ProfileDTO;
import ch.wisv.areafiftylan.dto.UserDTO;
import ch.wisv.areafiftylan.model.Profile;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.security.PasswordResetToken;
import ch.wisv.areafiftylan.security.VerificationToken;
import ch.wisv.areafiftylan.service.repository.PasswordResetTokenRepository;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import ch.wisv.areafiftylan.service.repository.VerificationTokenRepository;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository, MailService mailService) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.mailService = mailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
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
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findOneByUsername(username);
    }

    @Override
    public Collection<User> getAllUsers() {
        return userRepository.findAll(new Sort("email"));
    }

    @Override
    public User create(UserDTO userDTO, HttpServletRequest request) {
        String passwordHash = getPasswordHash(userDTO.getPassword());
        User user = new User(userDTO.getUsername(), passwordHash, userDTO.getEmail());
        user.addRole(userDTO.getRole());
        // All users that register through the service have to be verified
        user.setEnabled(false);

        // Save the user so the verificationToken can be stored.
        user = userRepository.saveAndFlush(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.saveAndFlush(verificationToken);

        try {
            String confirmUrl = getAppUrl(request) + "/confirmRegistration?token=" + token;
            mailService.sendVerificationmail(user, confirmUrl);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return user;
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

    @Override
    public User addProfile(Long userId, ProfileDTO profileDTO) {
        User user = userRepository.findOne(userId);
        user.getProfile().setAllFields(profileDTO.getFirstName(), profileDTO.getLastName(), profileDTO.getDisplayName(),
                profileDTO.getGender(), profileDTO.getAddress(), profileDTO.getZipcode(), profileDTO.getCity(),
                profileDTO.getPhoneNumber(), profileDTO.getNotes());
        return userRepository.saveAndFlush(user);

    }

    @Override
    public User changeProfile(Long userId, ProfileDTO profileDTO) {
        return addProfile(userId, profileDTO);
    }

    @Override
    public Profile resetProfile(Long userId) {
        Profile profile = userRepository.findOne(userId).getProfile();
        userRepository.findOne(userId).resetProfile();
        return profile;
    }

    @Override
    public void lock(Long userId, boolean lock) {
        User user = userRepository.findOne(userId);
        user.setAccountNonLocked(!lock);
        userRepository.saveAndFlush(user);
    }

    @Override
    public void verify(Long userId) {
        User user = userRepository.findOne(userId);
        user.setEnabled(true);
        userRepository.saveAndFlush(user);
    }

    @Override
    public void requestResetPassword(User user, HttpServletRequest request) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.saveAndFlush(passwordResetToken);

        try {
            String passwordUrl = getAppUrl(request) + "/resetPassword?token=" + token;
            mailService.sendPasswordResetMail(user, passwordUrl);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resetPassword(Long userId, String password) {
        User user = userRepository.findOne(userId);
        user.setPasswordHash(new BCryptPasswordEncoder().encode(password));
        userRepository.saveAndFlush(user);
    }

    @Override
    public Boolean checkEmailAvailable(String email) {
        return !userRepository.findOneByEmail(email).isPresent();
    }

    @Override
    public Boolean checkUsernameAvailable(String username) {
        return !userRepository.findOneByUsername(username).isPresent();

    }

    private String getPasswordHash(String plainTextPassword) {
        return new BCryptPasswordEncoder().encode(plainTextPassword);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findOneByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    /**
     * Turns the request into a url to which the request is made. For example https://localhost:8080 TODO: This makes a
     * call directly to the API, this should be handled by the front-end instead
     *
     * @param request The HttpServletRequest of the call that is made
     *
     * @return Formatted string of the base URL
     */
    public String getAppUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }
}