package ch.wisv.areafiftylan;


import ch.wisv.areafiftylan.service.SeatService;
import ch.wisv.areafiftylan.service.repository.TeamRepository;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import ch.wisv.areafiftylan.util.TestDataRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * This function inserts some testdata into the database for development testing.
     */
    @Bean
    @Profile("dev")
    CommandLineRunner init(UserRepository accountRepository, TicketRepository ticketRepository, SeatService seatService,
                           TeamRepository teamRepository) {

        return new TestDataRunner(accountRepository, ticketRepository, teamRepository, seatService);

    }


}