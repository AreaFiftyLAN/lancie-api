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


import ch.wisv.areafiftylan.users.model.Profile;
import ch.wisv.areafiftylan.users.model.ProfileDTO;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.model.UserDTO;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collection;

public interface UserService {
    User getUserById(long id);

    User getUserByEmail(String email);

    Collection<User> getAllUsers();

    User create(UserDTO userDTO) throws DataIntegrityViolationException;

    User replace(Long userId, UserDTO userDTO);

    void delete(Long userId);

    User edit(Long userId, UserDTO userDTO);

    User addProfile(Long userId, ProfileDTO profileDTO);

    User changeProfile(Long userId, ProfileDTO profileDTO);

    Profile resetProfile(Long userId);

    void lock(Long userId);

    void unlock(Long userId);

    void verify(Long userId);

    void requestResetPassword(User user);

    void resetPassword(Long userId, String password);

    void changePassword(Long userId, String oldPassword, String newPassword);

    Boolean checkEmailAvailable(String email);

    /**
     * This method checks wether a user is older than the alcohol age provided by properties.
     * @param userId The ID of the user to check.
     * @return A boolean indicating wether a user is old enough.
     */
    Boolean alcoholCheck(Long userId);
}
