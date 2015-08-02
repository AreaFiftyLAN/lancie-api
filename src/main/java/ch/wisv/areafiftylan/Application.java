package ch.wisv.areafiftylan;


import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.TeamRepository;
import ch.wisv.areafiftylan.service.UserRepository;
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
    CommandLineRunner init(UserRepository accountRepository, TeamRepository teamRepository) {
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