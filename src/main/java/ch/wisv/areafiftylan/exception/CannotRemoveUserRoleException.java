package ch.wisv.areafiftylan.exception;

import ch.wisv.areafiftylan.users.model.Role;

public class CannotRemoveUserRoleException extends AreaFiftyLANException {
    public CannotRemoveUserRoleException(Role role) {
        super("Role " + role.name() + " cannot be removed");
    }
}
