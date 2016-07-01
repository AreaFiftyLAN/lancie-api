/*
 * Copyright (c) 2016  W.I.S.V. 'Christiaan Huygens'
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.wisv.areafiftylan.utils;

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
