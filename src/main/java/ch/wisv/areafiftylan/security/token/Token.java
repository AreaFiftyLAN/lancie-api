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

import ch.wisv.areafiftylan.model.User;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Token {
    //Zero means not expirable
    private static final int EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private User user;


    @Column(nullable=false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean expirable = true;

    @Type(type = "ch.wisv.areafiftylan.util.LocalDateTimeUserType")
    private LocalDateTime expiryDate;


    @Column(nullable=false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean used = false;

    @Column(nullable=false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean revoked = false;

    public Token() {
    }

    public Token(User user) {
        this(user, EXPIRATION);
    }

    public Token(User user, int expiration) {
        this.token = UUID.randomUUID().toString();
        this.user = user;
        this.expirable = expiration != 0;
        this.expiryDate = calculateExpiryDate(expiration);
    }

    private LocalDateTime calculateExpiryDate(int expiryTimeInMinutes) {
        LocalDateTime expiryDate = LocalDateTime.now();
        return expiryDate.plusMinutes(expiryTimeInMinutes);
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void use() {
        this.used = true;
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isExpirable() {
        return expirable;
    }

    public boolean isValid() {
        // returns true only if the token is not used and not expired
        return !(this.used || isExpired() || isRevoked());
    }

    private boolean isExpired() {
        if (!this.isExpirable()) {
            return false;
        }

        return LocalDateTime.now().compareTo(expiryDate) > 0;
    }

    private boolean isRevoked() {
        return revoked;
    }

    public boolean isUnused() {
        return !used;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Token token1 = (Token) o;

        if (!id.equals(token1.id)) {
            return false;
        }
        if (!token.equals(token1.token)) {
            return false;
        }
        return user != null ? user.equals(token1.user) : token1.user == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + token.hashCode();
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}
