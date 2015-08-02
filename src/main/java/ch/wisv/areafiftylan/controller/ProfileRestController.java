package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.ProfileDTO;
import ch.wisv.areafiftylan.model.Profile;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/users/{userId}/profile")
public class ProfileRestController {

    private UserService userService;


    @Autowired
    ProfileRestController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> addProfile(@PathVariable Long userId, @Validated @RequestBody ProfileDTO input) {
        User user = userService.getUserById(userId).get();
        Profile profile = user.getProfile();

        profile.setDisplayName(input.getDisplayName());
        profile.setFirstName(input.getFirstName());
        profile.setLastName(input.getLastName());

        userService.save(user);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(user.getId()).toUri());

        return new ResponseEntity<>(user, httpHeaders, HttpStatus.CREATED);
    }
}
