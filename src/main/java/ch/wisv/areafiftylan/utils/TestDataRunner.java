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

import ch.wisv.areafiftylan.seats.model.SeatGroupDTO;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.model.Gender;
import ch.wisv.areafiftylan.users.model.Role;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.seats.service.SeatService;
import ch.wisv.areafiftylan.teams.service.TeamRepository;
import ch.wisv.areafiftylan.products.service.TicketRepository;
import ch.wisv.areafiftylan.users.service.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Created by Sille Kamoen on 24-3-16.
 */
public class TestDataRunner implements CommandLineRunner {
    private final UserRepository accountRepository;
    private final TicketRepository ticketRepository;
    private final SeatService seatService;
    private final TeamRepository teamRepository;

    public TestDataRunner(UserRepository accountRepository, TicketRepository ticketRepository,
                          TeamRepository teamRepository, SeatService seatService) {
        this.accountRepository = accountRepository;
        this.ticketRepository = ticketRepository;
        this.seatService = seatService;
        this.teamRepository = teamRepository;
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
        User testUser3 = new User("user3", new BCryptPasswordEncoder().encode("password"), "katrien@ms.com");
        testUser3.getProfile()
                .setAllFields("Katrien", "Zwanenburg", "Admiral Cheesecake", Gender.FEMALE, "Ganzenlaan 5", "2826CD",
                        "Duckstad", "0906-0666", null);
        User testUser4 = new User("noticket", new BCryptPasswordEncoder().encode("password"), "user@yahoo.com");
        testUser4.getProfile()
                .setAllFields("Kees", "Jager", "l33tz0r", Gender.MALE, "Herenweg 2", "2826CD", "Delft", "0902-30283",
                        null);
        User testUser5 = new User("user5", new BCryptPasswordEncoder().encode("password"), "custom@myself.com");
        testUser5.getProfile()
                .setAllFields("Gert", "Gertson", "Whosyourdaddy", Gender.MALE, "Jansstraat", "8826CD", "Delft",
                        "0238-2309736", null);

        testUser1 = accountRepository.saveAndFlush(testUser1);
        testUser2 = accountRepository.saveAndFlush(testUser2);
        testUser3 = accountRepository.saveAndFlush(testUser3);
        testUser4 = accountRepository.saveAndFlush(testUser4);
        testUser5 = accountRepository.saveAndFlush(testUser5);

        Ticket ticket = new Ticket(testUser1, TicketType.EARLY_FULL, false, false);
        Ticket ticket2 = new Ticket(testUser2, TicketType.EARLY_FULL, false, false);
        Ticket ticket3 = new Ticket(testUser3, TicketType.EARLY_FULL, false, false);
        ticket.setValid(true);
        ticket2.setValid(true);
        ticketRepository.save(ticket);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);

        Team team = new Team("testTeam", testUser1);
        team.addMember(testUser2);
        team.addMember(testUser3);

        teamRepository.save(team);

        for (char s = 'A'; s <= 'J'; s++) {
            SeatGroupDTO seatGroup = new SeatGroupDTO();
            seatGroup.setNumberOfSeats(16);
            seatGroup.setSeatGroupName(String.valueOf(s));
            seatService.addSeats(seatGroup);
        }
        seatService.reserveSeatForTicket("A", 2, ticket.getId());
    }
}
