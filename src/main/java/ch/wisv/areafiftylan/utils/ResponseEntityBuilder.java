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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResponseEntityBuilder {

    /**
     * Create a standard response for all requests to the API
     *
     * @param httpStatus  The HTTP Status of the response
     * @param httpHeaders Optional Http Headers for the response.
     * @param message     The message in human readable String format
     * @param object      Optional object related to the request (like a created User)
     *
     * @return The ResponseEntity in standard Area FiftyLAN format.
     */
    public static ResponseEntity<?> createResponseEntity(HttpStatus httpStatus, HttpHeaders httpHeaders, String message,
                                                         Object object) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("status", httpStatus.toString());
        responseBody.put("timestamp", LocalDateTime.now().toString());
        responseBody.put("message", message);
        responseBody.put("object", object);

        if (httpHeaders == null) {
            httpHeaders = new HttpHeaders();
        }
        return new ResponseEntity<>(responseBody, httpHeaders, httpStatus);
    }

    public static ResponseEntity<?> createResponseEntity(HttpStatus httpStatus, String message, Object object) {
        return createResponseEntity(httpStatus, null, message, object);
    }

    public static ResponseEntity<?> createResponseEntity(HttpStatus httpStatus, String message) {
        return createResponseEntity(httpStatus, null, message, null);
    }

    public static ResponseEntity<?> createResponseEntity(HttpStatus httpStatus, HttpHeaders httpHeaders,
                                                         String message) {
        return createResponseEntity(httpStatus, httpHeaders, message, null);
    }
}

