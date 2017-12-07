package ch.wisv.areafiftylan.exception;

public class BannerNotFoundException extends AreaFiftyLANException {

    public BannerNotFoundException() {
        super("Could not find a banner that is scheduled for now.");
    }

}
