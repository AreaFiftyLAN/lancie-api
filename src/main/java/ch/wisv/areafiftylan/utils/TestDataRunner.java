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

import ch.wisv.areafiftylan.extras.rfid.model.RFIDLink;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLinkRepository;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.TicketOption;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.products.service.repository.TicketOptionRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketTypeRepository;
import ch.wisv.areafiftylan.seats.model.SeatGroupDTO;
import ch.wisv.areafiftylan.seats.service.SeatService;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.teams.service.TeamRepository;
import ch.wisv.areafiftylan.users.model.Gender;
import ch.wisv.areafiftylan.users.model.Role;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("dev")
public class TestDataRunner implements CommandLineRunner {
    private final UserRepository accountRepository;
    private final TicketRepository ticketRepository;
    private final SeatService seatService;
    private final TeamRepository teamRepository;
    private final TicketOptionRepository ticketOptionRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final RFIDLinkRepository rfidLinkRepository;

    @Autowired
    public TestDataRunner(UserRepository accountRepository, TicketRepository ticketRepository,
                          TeamRepository teamRepository, SeatService seatService,
                          TicketOptionRepository ticketOptionRepository, TicketTypeRepository ticketTypeRepository,
                          RFIDLinkRepository rfidLinkRepository) {
        this.accountRepository = accountRepository;
        this.ticketRepository = ticketRepository;
        this.seatService = seatService;
        this.teamRepository = teamRepository;
        this.ticketOptionRepository = ticketOptionRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.rfidLinkRepository = rfidLinkRepository;
    }

    @Override
    public void run(String... evt) throws Exception {
        LocalDate localDate = LocalDate.of(2000, 1, 2);

        User testUser1 = new User("user@mail.com", new BCryptPasswordEncoder().encode("password"));
        testUser1.addRole(Role.ROLE_ADMIN);
        testUser1.getProfile()
                .setAllFields("Jan", "de Groot", "MonsterKiller9001", LocalDate.of(1990, 2, 1), Gender.MALE, "Mekelweg 4", "2826CD",
                        "Delft", "0906-0666", null);
        User testUser2 = new User("bert@mail.com", new BCryptPasswordEncoder().encode("password"));
        testUser2.getProfile()
                .setAllFields("Bert", "Kleijn", "ILoveZombies", localDate, Gender.OTHER, "Mekelweg 20", "2826CD",
                        "Amsterdam", "0611", null);
        User testUser3 = new User("katrien@ms.com", new BCryptPasswordEncoder().encode("password"));
        testUser3.getProfile()
                .setAllFields("Katrien", "Zwanenburg", "Admiral Cheesecake", localDate, Gender.FEMALE, "Ganzenlaan 5",
                        "2826CD", "Duckstad", "0906-0666", null);
        User testUser4 = new User("user@yahoo.com", new BCryptPasswordEncoder().encode("password"));
        testUser4.getProfile()
                .setAllFields("Kees", "Jager", "l33tz0r", localDate, Gender.MALE, "Herenweg 2", "2826CD", "Delft",
                        "0902-30283", null);
        User testUser5 = new User("custom@myself.com", new BCryptPasswordEncoder().encode("password"));
        testUser5.getProfile()
                .setAllFields("Gert", "Gertson", "Whosyourdaddy", localDate, Gender.MALE, "Jansstraat", "8826CD",
                        "Delft", "0238-2309736", null);

        testUser1 = accountRepository.saveAndFlush(testUser1);
        testUser2 = accountRepository.saveAndFlush(testUser2);
        testUser3 = accountRepository.saveAndFlush(testUser3);
        testUser4 = accountRepository.saveAndFlush(testUser4);
        testUser5 = accountRepository.saveAndFlush(testUser5);

        TicketOption chMember = ticketOptionRepository.save(new TicketOption("chMember", -5F));
        TicketOption pickupService = ticketOptionRepository.save(new TicketOption("pickupService", 2.5F));
        TicketType early = new TicketType("Early", "Early Bird", 35F, 50, LocalDateTime.now().plusDays(7L), true);
        early.addPossibleOption(chMember);
        early.addPossibleOption(pickupService);
        early = ticketTypeRepository.save(early);

        Ticket ticket = new Ticket(testUser1, early);
        ticket.addOption(chMember);
        ticket.addOption(pickupService);
        Ticket ticket2 = new Ticket(testUser2, early);
        ticket2.addOption(pickupService);
        Ticket ticket3 = new Ticket(testUser3, early);
        ticket.setValid(true);
        ticket2.setValid(true);
        ticketRepository.save(ticket);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);

        RFIDLink rfidLink1 = new RFIDLink("0000000001", ticket);
        rfidLinkRepository.saveAndFlush(rfidLink1);
        RFIDLink rfidLink2 = new RFIDLink("0000000002", ticket2);
        rfidLinkRepository.saveAndFlush(rfidLink2);
        RFIDLink rfidLink3 = new RFIDLink("0000000003", ticket3);
        rfidLinkRepository.saveAndFlush(rfidLink3);

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
