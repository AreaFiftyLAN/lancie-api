/*
 * Copyright (c) 2018  W.I.S.V. 'Christiaan Huygens'
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.wisv.areafiftylan.users.service;

import ch.wisv.areafiftylan.exception.CannotRemoveUserRoleException;
import ch.wisv.areafiftylan.exception.UserNotFoundException;
import ch.wisv.areafiftylan.security.token.PasswordResetToken;
import ch.wisv.areafiftylan.security.token.VerificationToken;
import ch.wisv.areafiftylan.security.token.repository.PasswordResetTokenRepository;
import ch.wisv.areafiftylan.security.token.repository.VerificationTokenRepository;
import ch.wisv.areafiftylan.users.model.*;
import ch.wisv.areafiftylan.utils.mail.MailService;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;

    private static final Set<Role> defaultRoles = new HashSet<>(Sets.newHashSet(Role.ROLE_USER));

    @Value("${a5l.mail.confirmUrl}")
    String requestUrl;
    @Value("${a5l.user.resetUrl}")
    String resetUrl;
    @Value("${a51.user.alcoholage : 18}")
    Long ALCOHOL_AGE;

    public static final int MIN_PASSWORD_LENGTH = 6;

    public UserServiceImpl(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository, MailService mailService) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.mailService = mailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Override
    public User getUserById(long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findOneByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User '" + email + "' not found"));
    }

    @Override
    public Collection<User> getAllUsers() {
        return userRepository.findAll(Sort.by("email"));
    }

    @Override
    public User create(UserDTO userDTO) throws DataIntegrityViolationException {
        handleDuplicateUserFields(userDTO);

        // Hash the plain password coming from the DTO
        String passwordHash = getPasswordHash(userDTO.getPassword());
        User user = new User(userDTO.getEmail(), passwordHash);
        // Add default roles to User
        for (Role defaultRole : defaultRoles) {
            user.addRole(defaultRole);
        }
        // All users that register through the service have to be verified
        user.setEnabled(false);

        // Save the user so the verificationToken can be stored.
        user = userRepository.saveAndFlush(user);

        generateAndSendToken(user, userDTO.getOrderId());

        return user;
    }

    private void handleDuplicateUserFields(UserDTO userDTO) throws DataIntegrityViolationException {
        // Check if the email is already in use
        userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).ifPresent(u -> {
            throw new DataIntegrityViolationException("Email already in use");
        });
    }

    private void generateAndSendToken(User user, Long orderId) {
        // Create a new Verificationcode with this UUID, and link it to the user
        VerificationToken verificationToken = new VerificationToken(user);
        verificationTokenRepository.saveAndFlush(verificationToken);

        // Build the URL and send this to the mailservice for sending.
        String confirmUrl = requestUrl + "?token=" + verificationToken.getToken();
        if (orderId != null) {
            confirmUrl += "&orderId=" + orderId;
        }
        mailService.sendVerificationmail(user, confirmUrl);
    }

    @Override
    public User replace(Long userId, UserDTO userDTO) {
        User user = userRepository.getOne(userId);

        user.setEmail(userDTO.getEmail());
        user.setPasswordHash(getPasswordHash(userDTO.getPassword()));
        user.resetProfile();

        return userRepository.saveAndFlush(user);
    }

    @Override
    public void delete(Long userId) {
        // We don't delete users, we lock them
        throw new UnsupportedOperationException("Can't delete users, lock them instead");
    }

    @Override
    public User edit(Long userId, UserDTO userDTO) {
        User user = getUserById(userId);
        if (!Strings.isNullOrEmpty(userDTO.getEmail())) {
            user.setEmail(userDTO.getEmail());
        }
        if (!Strings.isNullOrEmpty(userDTO.getPassword())) {
            user.setPasswordHash(getPasswordHash(userDTO.getPassword()));
        }
        return userRepository.saveAndFlush(user);

    }

    @Override
    public User addProfile(Long userId, ProfileDTO profileDTO) throws DataIntegrityViolationException {
        /*
            No two identical displayNames are allowed. If the displayName already exists in the database we check
            whether this is a different user. If this is the case, we throw an exception, if not, we can continue.
         */
        userRepository.findOneByProfileDisplayNameIgnoreCase(profileDTO.getDisplayName()).ifPresent(u -> {
            if (!u.getId().equals(userId)) {
                throw new DataIntegrityViolationException("DisplayName already in use");
            }
        });

        User user = getUserById(userId);

        // Set all the profile fields at once
        user.getProfile().setAllFields(profileDTO.getFirstName(), profileDTO.getLastName(), profileDTO.getDisplayName(),
                profileDTO.getBirthday(), profileDTO.getGender(), profileDTO.getAddress(), profileDTO.getZipcode(),
                profileDTO.getCity(), profileDTO.getPhoneNumber(), profileDTO.getNotes());
        return userRepository.saveAndFlush(user);

    }

    @Override
    public User changeProfile(Long userId, ProfileDTO profileDTO) {
        return addProfile(userId, profileDTO);
    }

    @Override
    public Profile resetProfile(Long userId) {
        Profile profile = getUserById(userId).getProfile();
        getUserById(userId).resetProfile();
        return profile;
    }

    @Override
    public void unlock(Long userId) {
        User user = getUserById(userId);
        user.setAccountNonLocked(true);
        userRepository.saveAndFlush(user);
    }

    @Override
    public void lock(Long userId) {
        User user = getUserById(userId);
        user.setAccountNonLocked(false);
        userRepository.saveAndFlush(user);
    }

    @Override
    public void verify(Long userId) {
        User user = getUserById(userId);
        user.setEnabled(true);
        userRepository.saveAndFlush(user);
    }

    @Override
    public void requestResetPassword(User user) {
        // Use the generated ID to create a passwordToken and link it to the user
        PasswordResetToken passwordResetToken = new PasswordResetToken(user);
        passwordResetTokenRepository.saveAndFlush(passwordResetToken);

        String passwordUrl = resetUrl + "?token=" + passwordResetToken.getToken();
        mailService.sendPasswordResetMail(user, passwordUrl);
    }

    @Override
    public void resetPassword(Long userId, String password) {
        User user = getUserById(userId);
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("New password should be at least %d characters long", MIN_PASSWORD_LENGTH));
        }
        // The token is being checked in the authentication, so just set the password here
        user.setPasswordHash(new BCryptPasswordEncoder().encode(password));
        userRepository.saveAndFlush(user);

        log.info("New password set for {}.", user.getEmail(), StructuredArguments.v("user_email", user.getEmail()));
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("New password should be at least %d characters long", MIN_PASSWORD_LENGTH));
        }
        User user = getUserById(userId);

        if (new BCryptPasswordEncoder().matches(oldPassword, user.getPassword())) {
            user.setPasswordHash(getPasswordHash(newPassword));
            userRepository.save(user);
        } else {
            throw new AccessDeniedException("Wrong password");
        }
    }

    @Override
    public Boolean checkEmailAvailable(String email) {
        return !userRepository.findOneByEmailIgnoreCase(email).isPresent();

    }

    @Override
    public Boolean alcoholCheck(Long userId) {
        User user = getUserById(userId);
        return user.getProfile().getBirthday().isBefore(LocalDate.now().minusYears(ALCOHOL_AGE));
    }

    @Override
    public void addRole(Long userId, RoleDTO input) {
        User user = getUserById(userId);
        user.addRole(input.getRole());
        userRepository.saveAndFlush(user);
    }

    @Override
    public void deleteRole(Long userId, RoleDTO input) {
        if (input.getRole() == Role.ROLE_USER) {
            throw new CannotRemoveUserRoleException(input.getRole());
        }

        User user = getUserById(userId);
        user.deleteRole(input.getRole());
        userRepository.saveAndFlush(user);
    }

    /**
     * Encrypt the password using the BCryptPasswordEncoder with default settings
     *
     * @param plainTextPassword The password to be encoded
     *
     * @return The hashed password
     */
    private String getPasswordHash(String plainTextPassword) {
        return new BCryptPasswordEncoder().encode(plainTextPassword);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findOneByEmailIgnoreCase(email).orElseThrow(() -> new UsernameNotFoundException(email));
    }
}
