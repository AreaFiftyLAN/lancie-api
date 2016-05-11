package ch.wisv.areafiftylan.service;

/**
 * Created by Sille Kamoen on 6-5-16.
 */
public interface AuthenticationService {

    public String createNewAuthToken(String username, String password);
}
