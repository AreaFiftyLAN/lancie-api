package ch.wisv.areafiftylan.security.token;

import ch.wisv.areafiftylan.model.User;
import org.springframework.cglib.core.Local;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
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

    private boolean expirable = true;
    private LocalDateTime expiryDate;

    private boolean used = false;
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

    public void revoke(){
        this.revoked = true;
    }

    public boolean isExpirable(){
        return expirable;
    }

    public boolean isValid() {
        // returns true only if the token is not used and not expired
        return !(this.used || isExpired() || isRevoked());
    }

    private boolean isExpired() {
        if(!this.isExpirable())
            return false;

        return LocalDateTime.now().compareTo(expiryDate) > 0;
    }

    private boolean isRevoked() {
        return revoked;
    }
}
