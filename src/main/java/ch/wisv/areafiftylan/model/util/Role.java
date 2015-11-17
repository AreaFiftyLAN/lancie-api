package ch.wisv.areafiftylan.model.util;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_USER, ROLE_COMMITTEE, ROLE_ADMIN;

    @Override
    public String getAuthority() {
        return this.toString();
    }

}
