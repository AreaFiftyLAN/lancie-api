package ch.wisv.areafiftylan.web.properties.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/web/properties")
public class WebPropertiesController {

    @Value("${a5l.googleMapsAPIkey:API_KEY}")
    private String googleMapsKey;

    @GetMapping("/googlemapskey")
    public ResponseEntity<?> getGoogleMapsKey() {
        return createResponseEntity(HttpStatus.OK, this.googleMapsKey);
    }
}
