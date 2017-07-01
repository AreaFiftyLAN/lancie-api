/*
 * Copyright (c) 2016  W.I.S.V. 'Christiaan Huygens'
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

package ch.wisv.areafiftylan.security.token;

import ch.wisv.areafiftylan.users.model.User;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class UserToken extends Token{
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    @Getter
    private User user;

    UserToken(User user) {
        this(user, Token.DEFAULTEXPIRATION);
    }

    UserToken(User user, int expiration) {
        super(expiration);
        this.user = user;
    }
}
