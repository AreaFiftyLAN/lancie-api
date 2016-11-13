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

import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.products.service.TicketRepository;
import ch.wisv.areafiftylan.seats.model.SeatGroupDTO;
import ch.wisv.areafiftylan.seats.service.SeatService;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.teams.service.TeamRepository;
import ch.wisv.areafiftylan.users.model.Gender;
import ch.wisv.areafiftylan.users.model.Role;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

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
        Calendar calendar = new GregorianCalendar(2000, 0, 1, 0, 0, 0);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+1"));

        User testUser1 = new User("user@mail.com", new BCryptPasswordEncoder().encode("password"));
        testUser1.addRole(Role.ROLE_ADMIN);
        testUser1.getProfile()
                .setAllFields("Jan", "de Groot", "MonsterKiller9001", calendar, Gender.MALE, "Mekelweg 4", "2826CD", "Delft",
                        "0906-0666", null);
        User testUser2 = new User("bert@mail.com", new BCryptPasswordEncoder().encode("password"));
        testUser2.getProfile()
                .setAllFields("Bert", "Kleijn", "ILoveZombies", calendar, Gender.OTHER, "Mekelweg 20", "2826CD", "Amsterdam",
                        "0611", null);
        User testUser3 = new User("katrien@ms.com", new BCryptPasswordEncoder().encode("password"));
        testUser3.getProfile()
                .setAllFields("Katrien", "Zwanenburg", "Admiral Cheesecake", calendar, Gender.FEMALE, "Ganzenlaan 5", "2826CD",
                        "Duckstad", "0906-0666", null);
        User testUser4 = new User("user@yahoo.com", new BCryptPasswordEncoder().encode("password"));
        testUser4.getProfile()
                .setAllFields("Kees", "Jager", "l33tz0r", calendar, Gender.MALE, "Herenweg 2", "2826CD", "Delft", "0902-30283",
                        null);
        User testUser5 = new User("custom@myself.com", new BCryptPasswordEncoder().encode("password"));
        testUser5.getProfile()
                .setAllFields("Gert", "Gertson", "Whosyourdaddy", calendar, Gender.MALE, "Jansstraat", "8826CD", "Delft",
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
