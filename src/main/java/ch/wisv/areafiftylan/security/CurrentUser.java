package ch.wisv.areafiftylan.security;

import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Role;

import java.util.Collection;

public class CurrentUser extends org.springframework.security.core.userdetails.User {

    private User user;

    public CurrentUser(User user) {
        super(user.getEmail(), user.getPasswordHash(), user.getRoles());
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Long getId() {
        return user.getId();
    }

    public Collection<Role> getRoles() {
        return user.getRoles();
    }

}
