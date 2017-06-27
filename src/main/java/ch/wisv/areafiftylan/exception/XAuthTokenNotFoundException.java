package ch.wisv.areafiftylan.exception;

public class XAuthTokenNotFoundException extends AreaFiftyLANException {
    public XAuthTokenNotFoundException () {
        super("X-Auth-Token not found");
    }
}
