package ch.wisv.areafiftylan.util;

import com.jayway.restassured.response.Cookie;
import com.jayway.restassured.response.Header;

public class SessionData {
    Header token;
    Cookie cookie;

    public SessionData(String token, String cookie) {
        this.token = new Header("X-CSRF-TOKEN", token);
        this.cookie = new Cookie.Builder("JSESSIONID", cookie).build();
    }

    public Header getCsrfHeader() {
        return token;
    }

    public String getToken() {
        return token.getValue();
    }

    public Cookie getCookie() {
        return cookie;
    }

    public String getSessionId() {
        return cookie.getValue();
    }

}
