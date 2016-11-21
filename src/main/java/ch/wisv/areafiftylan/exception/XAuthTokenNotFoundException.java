package ch.wisv.areafiftylan.exception;

public class XAuthTokenNotFoundException extends TokenNotFoundException {
    public XAuthTokenNotFoundException () {
        super("X-Auth-Token not found");
    }
}
