package ch.wisv.areafiftylan.util;

import ch.wisv.areafiftylan.dto.SeatGroupDTO;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.model.util.Role;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.SeatService;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Created by Sille Kamoen on 24-3-16.
 */
public class TestDataRunner implements CommandLineRunner {
    private final UserRepository accountRepository;
    private final TicketRepository ticketRepository;
    private final SeatService seatService;

    public TestDataRunner(UserRepository accountRepository, TicketRepository ticketRepository,
                          SeatService seatService) {
        this.accountRepository = accountRepository;
        this.ticketRepository = ticketRepository;
        this.seatService = seatService;
    }

    @Override
    public void run(String... evt) throws Exception {
        User testUser1 = new User("user", new BCryptPasswordEncoder().encode("password"), "user@mail.com");
        testUser1.addRole(Role.ROLE_ADMIN);
        testUser1.getProfile()
                .setAllFields("Jan", "de Groot", "MonsterKiller9001", Gender.MALE, "Mekelweg 4", "2826CD", "Delft",
                        "0906-0666", null);
        User testUser2 = new User("user2", new BCryptPasswordEncoder().encode("password"), "bert@mail.com");
        testUser2.getProfile()
                .setAllFields("Bert", "Kleijn", "ILoveZombies", Gender.OTHER, "Mekelweg 20", "2826CD", "Amsterdam",
                        "0611", null);
        User testUser3 = new User("user3", "passwordHash", "katrien@ms.com");
        testUser3.getProfile()
                .setAllFields("Katrien", "Zwanenburg", "Admiral Cheesecake", Gender.FEMALE, "Ganzenlaan 5",
                        "2826CD", "Duckstad", "0906-0666", null);
        User testUser4 = new User("user4", "passwordHash", "user@yahoo.com");
        testUser4.getProfile()
                .setAllFields("Kees", "Jager", "l33tz0r", Gender.MALE, "Herenweg 2", "2826CD", "Delft",
                        "0902-30283", null);
        User testUser5 = new User("user5", "passwordHash", "custom@myself.com");
        testUser5.getProfile()
                .setAllFields("Gert", "Gertson", "Whosyourdaddy", Gender.MALE, "Jansstraat", "8826CD", "Delft",
                        "0238-2309736", null);

        accountRepository.saveAndFlush(testUser1);
        accountRepository.saveAndFlush(testUser2);
        accountRepository.saveAndFlush(testUser3);
        accountRepository.saveAndFlush(testUser4);
        accountRepository.saveAndFlush(testUser5);

        Ticket ticket = new Ticket(testUser1, TicketType.EARLY_FULL, false, false);
        Ticket ticket2 = new Ticket(testUser2, TicketType.EARLY_FULL, false, false);
        Ticket ticket3 = new Ticket(testUser3, TicketType.EARLY_FULL, false, false);
        ticketRepository.save(ticket);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);

        for (char s = 'A'; s <= 'J'; s++) {
            SeatGroupDTO seatGroup = new SeatGroupDTO();
            seatGroup.setNumberOfSeats(16);
            seatGroup.setSeatGroupName(String.valueOf(s));
            seatService.addSeats(seatGroup);
        }
        seatService.reserveSeatForTicket("A", 2, ticket.getId());
    }
}
