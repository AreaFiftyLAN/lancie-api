package ch.wisv.areafiftylan.utils.setup;

import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.security.token.SetupToken;
import ch.wisv.areafiftylan.security.token.repository.SetupTokenRepository;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.utils.ResponseEntityBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/setup")
@PreAuthorize("hasRole('ADMIN')")
public class SetupController {
    private final SetupRepository setupRepository;
    private final SetupTokenRepository setupTokenRepository;
    private final SetupService setupService;

    public SetupController(SetupRepository setupRepository, SetupTokenRepository setupTokenRepository, SetupService setupService) {
        this.setupRepository = setupRepository;
        this.setupTokenRepository = setupTokenRepository;
        this.setupService = setupService;
    }

    @PostMapping("/{year}")
    public ResponseEntity<?> setupNewEvent(@AuthenticationPrincipal User user, @PathVariable int year) {
        SetupLog lastSetup = setupRepository.findAll(Sort.by("year")).get(0);

        if (lastSetup.getYear() >= year) {
            return ResponseEntityBuilder.createResponseEntity(HttpStatus.BAD_REQUEST, "Can't setup event for this year or before");
        }

        SetupToken token = new SetupToken(user, year);


        return ResponseEntityBuilder.createResponseEntity(HttpStatus.OK, "All good", token);
    }

    @PostMapping("/{year}/{token}")
    public ResponseEntity<?> confirmSetup(@AuthenticationPrincipal User user, @PathVariable int year, @PathVariable String token) {
        SetupToken setupToken = setupTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        if (setupToken.getUser().equals(user) &&
                setupToken.isValid() &&
                setupToken.getYear() == year) {
            // All good, delete all stuff from the previous event
            setupService.deleteAllCurrentEventData();

        }
        SetupLog newSetup = new SetupLog(year, user);
        setupRepository.save(newSetup);

        return ResponseEntityBuilder.createResponseEntity(HttpStatus.OK, "Setup complete");
    }


}
