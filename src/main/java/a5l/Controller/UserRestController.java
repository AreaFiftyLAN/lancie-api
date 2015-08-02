package a5l.Controller;

import a5l.DTO.UserDTO;
import a5l.Model.User;
import a5l.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserRestController {


    private UserService userService;

    @Autowired
    UserRestController(UserService userService) {
        this.userService = userService;
    }

    //////////// USER MAPPINGS //////////////////

    /**
     * This method accepts POST requests on /users. It will create a new user and an empty profile attached to it.
     *
     * @param input The user that has to be created. It consists of 3 fields. The username, the email and the
     *                  plain-text password. The password is saved hashed using the BCryptPasswordEncoder
     * @return The generated object, in JSON format.
     */
    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@Validated @RequestBody UserDTO input) {
        User save = userService.create(input);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(save.getId()).toUri());

        return new ResponseEntity<>(save, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    User getUserById(@PathVariable Long userId) {
        return this.userService.getUserById(userId).get();
    }

    @RequestMapping(method = RequestMethod.GET)
    Collection<User> readUsers() {
        return userService.getAllUsers();
    }
}
