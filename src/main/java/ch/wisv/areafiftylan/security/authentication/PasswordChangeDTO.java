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

package ch.wisv.areafiftylan.security.authentication;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import ch.wisv.areafiftylan.users.service.UserServiceImpl;

import javax.validation.constraints.NotEmpty;

public class PasswordChangeDTO {

    @Getter
    @Setter
    @NotEmpty
    String oldPassword = "";
    @Getter
    @Setter
    @Length(min = UserServiceImpl.MIN_PASSWORD_LENGTH)
    String newPassword = "";

}
