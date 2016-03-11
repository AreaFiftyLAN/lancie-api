package ch.wisv.areafiftylan.security;

import ch.wisv.areafiftylan.model.User;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

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

    private Date expiryDate;

    private boolean used = false;
    private boolean revoked = false;

    public Token() {
    }

    public Token(String token, User user) {
        this(token, user, EXPIRATION);
    }
    public Token(String token, User user, int expiration) {
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(expiration);
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean getIsExpireable(){
        return EXPIRATION != 0;
    }

    public void use() {
        this.used = true;
    }

    public void revoke(){
        this.revoked = true;
    }

    public boolean isValid() {
        // returns true only if the token is not used and not expired
        return !(this.used || isExpired() || isRevoked());
    }

    private boolean isExpired() {
        Calendar cal = Calendar.getInstance();
        return (this.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0;
    }

    private boolean isRevoked() {
        return revoked;
    }
}
