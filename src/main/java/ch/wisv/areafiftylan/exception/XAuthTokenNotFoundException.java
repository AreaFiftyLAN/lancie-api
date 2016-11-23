package ch.wisv.areafiftylan.exception;

public class XAuthTokenNotFoundException extends RuntimeException {
    public XAuthTokenNotFoundException () {
        super("X-Auth-Token not found");
    }
}
