package ch.wisv.areafiftylan;


import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Coordinate;
import ch.wisv.areafiftylan.service.repository.SeatRepository;
import ch.wisv.areafiftylan.service.repository.TeamRepository;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    @Bean
    CommandLineRunner init(UserRepository accountRepository, TeamRepository teamRepository, SeatRepository seatRepository) {
        User testUser = new User("testUser", "lolhash", "test@a5l.com");
        testUser = accountRepository.save(testUser);

        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 5; j++) {
                Seat seat = new Seat(new Coordinate(i, j));
                if(seat.getCoordinate().equals(new Coordinate(1,1)))
                    seat.setUser(testUser);
                seatRepository.save(seat);
            }

        }

        return (evt) -> Arrays.asList(
                "jhoeller,dsyer,pwebb,ogierke,rwinch,mfisher,mpollack,jlong".split(","))
                .forEach(
                        a -> {
                            User account = accountRepository.save(new User(a,
                                    "password", a + "@mail.com"));
                            User captain = accountRepository.findByUsername(a).get();
                            Team team = new Team(a + "team", captain);
                            teamRepository.save(team);


                        });

    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}