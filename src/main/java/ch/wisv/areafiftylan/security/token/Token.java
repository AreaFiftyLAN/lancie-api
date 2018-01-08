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

package ch.wisv.areafiftylan.security.token;

import ch.wisv.areafiftylan.users.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Token {
    //Zero means not expirable
    private static final int EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean expirable = true;

    private LocalDateTime expiryDate;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean used = false;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean revoked = false;

    Token(User user) {
        this(user, EXPIRATION);
    }

    Token(User user, int expiration) {
        this.token = UUID.randomUUID().toString();
        this.user = user;
        this.expirable = expiration != 0;
        this.expiryDate = calculateExpiryDate(expiration);
    }

    private LocalDateTime calculateExpiryDate(int expiryTimeInMinutes) {
        LocalDateTime expiryDate = LocalDateTime.now();
        return expiryDate.plusMinutes(expiryTimeInMinutes);
    }

    public void use() {
        this.used = true;
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isValid() {
        // returns true only if the token is not used and not expired
        return !(isUsed() || isExpired() || isRevoked());
    }

    private boolean isExpired() {
        return this.isExpirable() && LocalDateTime.now().compareTo(expiryDate) > 0;

    }
}
