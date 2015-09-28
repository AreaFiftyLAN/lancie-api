package ch.wisv.areafiftylan.model.util;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER, COMMITTEE, ADMIN;

    @Override
    public String getAuthority() {
        return this.toString();
    }

}
