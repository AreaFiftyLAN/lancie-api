package a5l;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/users/")
public class UserRestController {

    private UserRepository userRepository;

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@RequestBody User input) {

        User save = userRepository.save(new User(input.getUsername(), input.getPassword(), input.getEmail()));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(save.getId()).toUri());

        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    User readBookmark(@PathVariable Long userId) {
        return this.userRepository.findOne(userId);
    }

    @RequestMapping(method = RequestMethod.GET)
    List<User> readUsers() {
        return userRepository.findAll();
    }

    @Autowired
    UserRestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
