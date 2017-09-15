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

import ch.wisv.areafiftylan.extras.consumption.model.Consumption;
import ch.wisv.areafiftylan.extras.consumption.model.PossibleConsumptionsRepository;
import ch.wisv.areafiftylan.extras.consumption.service.ConsumptionService;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLink;
import ch.wisv.areafiftylan.extras.rfid.service.RFIDLinkRepository;
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
import ch.wisv.areafiftylan.web.committee.model.CommitteeMember;
import ch.wisv.areafiftylan.web.committee.service.CommitteeRepository;
import ch.wisv.areafiftylan.web.faq.model.FaqPair;
import ch.wisv.areafiftylan.web.faq.service.FaqRepository;
import ch.wisv.areafiftylan.web.sponsor.model.Sponsor;
import ch.wisv.areafiftylan.web.sponsor.model.SponsorType;
import ch.wisv.areafiftylan.web.sponsor.service.SponsorRepository;
import ch.wisv.areafiftylan.web.tournament.model.Tournament;
import ch.wisv.areafiftylan.web.tournament.model.TournamentType;
import ch.wisv.areafiftylan.web.tournament.service.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private final PossibleConsumptionsRepository consumptionsRepository;
    private final ConsumptionService consumptionService;

    private final CommitteeRepository committeeRepository;
    private final FaqRepository faqRepository;
    private final SponsorRepository sponsorRepository;
    private final TournamentRepository tournamentRepository;

    @Autowired
    public TestDataRunner(UserRepository accountRepository, TicketRepository ticketRepository,
                          TeamRepository teamRepository, SeatService seatService,
                          TicketOptionRepository ticketOptionRepository, TicketTypeRepository ticketTypeRepository,
                          RFIDLinkRepository rfidLinkRepository, PossibleConsumptionsRepository consumptionsRepository,
                          ConsumptionService consumptionService, CommitteeRepository committeeRepository,
                          FaqRepository faqRepository, SponsorRepository sponsorRepository,
                          TournamentRepository tournamentRepository) {
        this.accountRepository = accountRepository;
        this.ticketRepository = ticketRepository;
        this.seatService = seatService;
        this.teamRepository = teamRepository;
        this.ticketOptionRepository = ticketOptionRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.rfidLinkRepository = rfidLinkRepository;
        this.consumptionsRepository = consumptionsRepository;
        this.consumptionService = consumptionService;
        this.committeeRepository = committeeRepository;
        this.faqRepository = faqRepository;
        this.sponsorRepository = sponsorRepository;
        this.tournamentRepository = tournamentRepository;
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

        Consumption consumption1 = new Consumption("Bier");
        consumption1 = consumptionsRepository.saveAndFlush(consumption1);
        Consumption consumption2 = new Consumption("Wijn");
        consumption2 = consumptionsRepository.saveAndFlush(consumption2);

        consumptionService.consume(ticket.getId(), consumption1.getId());
        consumptionService.consume(ticket.getId(), consumption2.getId());
        consumptionService.consume(ticket2.getId(), consumption1.getId());

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
        seatService.setAllSeatsLock(false);
        seatService.reserveSeat("A", 2, ticket.getId(), false);

        // Web data
        committeeMember(1L, "Chairman", "Lotte Bryan", "group");
        committeeMember(2L, "Secretary", "Sterre Noorthoek", "male");
        committeeMember(3L, "Treasurer", "Francis Behnen", "money");
        committeeMember(4L, "Commissioner of Promo", "Hilco van der Wilk", "bullhorn");
        committeeMember(5L, "Commissioner of Logistics", "Millen van Osch", "truck");
        committeeMember(6L, "Commissioner of Systems", "Matthijs Kok", "cogs");
        committeeMember(7L, "Qualitate Qua", "Beer van der Drift", "heart");

        faqpair("What the fox say?", "Ring-ding-ding-ding-dingeringeding!");
        faqpair("What do you get if you multiply six by nine?", "42");
        faqpair("What is your favorite colour?", "Blue.");
        faqpair("What is the capital of Assyria?", "Well I don't know!");
        faqpair("What is your favorite colour?", "Blue! no, Yellow!");
        faqpair("What is the airspeed velocity of an unladen swallow?", "What do you mean? African or European swallow?");

        sponsor("Christiaan Huygens", SponsorType.PRESENTER, "https://ch.tudelft.nl", "images-optimized/lancie/logo_CH.png");
        sponsor("TU Delft", SponsorType.PRESENTER, "https://www.tudelft.nl", "images-optimized/logos/logo_SC.png");
        Sponsor sogeti = sponsor("Sogeti", SponsorType.PREMIUM, "https://www.sogeti.nl/", "images-optimized/logos/sogeti.png");
        sponsor("Nutanix", SponsorType.PREMIUM, "https://www.nutanix.com", "images-optimized/logos/nutanix.png");
        sponsor("OGD", SponsorType.PREMIUM, "https://ogd.nl/", "images-optimized/logos/ogd.png");
        Sponsor coolerMaster = sponsor("CoolerMaster", SponsorType.NORMAL, "http://www.coolermaster.com/", "images-optimized/logos/Cooler_Master_Logo.png");
        sponsor("Spam", SponsorType.NORMAL, "http://www.spam-energydrink.com/", "images-optimized/logos/spam-logo.jpg");
        sponsor("Jigsaw", SponsorType.NORMAL, "http://www.jigsaw.nl/", "images-optimized/logos/jigsaw_cleaner_logo.png");
        sponsor("TransIP", SponsorType.NORMAL, "https://www.transip.nl/", "images-optimized/logos/transip_logo.png");

        tournament(TournamentType.OFFICIAL, "RL", "images-optimized/activities/rl.jpg", "3 V 3", "Rocket League",
                "Rocket League is a fun game.", Arrays.asList("Huis", "Koelkast", "Broodrooster"), sogeti);
        tournament(TournamentType.OFFICIAL, "HS", "images-optimized/activities/hs.jpg", "1 V 1", "Hearthstone",
                "Hearthstone is not a fun game.", Arrays.asList("Keyboard", "Muis", "50 Packs"), coolerMaster);
        tournament(TournamentType.UNOFFICIAL, "Achtung", "images-optimized/unofficial/achtung.jpg", "1 V 1",
                "Achtung Die Kurve", "Achtung!", Arrays.asList("Kratje Hertog Jan", "Kratje Heineken", "Kratje Amstel"), null);
        tournament(TournamentType.UNOFFICIAL, "JD", "images-optimized/unofficial/justdance.jpg", "2 V 2", "Just Dance",
                "Just Dance is about dancing.", Collections.singletonList("Nothing."), sogeti);
    }

    private CommitteeMember committeeMember(Long position, String function, String name, String icon) {
        CommitteeMember committeeMember = new CommitteeMember();
        committeeMember.setPosition(position);
        committeeMember.setFunction(function);
        committeeMember.setName(name);
        committeeMember.setIcon(icon);
        return committeeRepository.save(committeeMember);
    }

    private FaqPair faqpair(String question, String answer) {
        FaqPair faqPair = new FaqPair();
        faqPair.setQuestion(question);
        faqPair.setAnswer(answer);
        return faqRepository.save(faqPair);
    }

    private Sponsor sponsor(String name, SponsorType type, String website, String imageName) {
        Sponsor sponsor = new Sponsor();
        sponsor.setName(name);
        sponsor.setType(type);
        sponsor.setWebsite(website);
        sponsor.setImageName(imageName);
        return sponsorRepository.save(sponsor);
    }

    private Tournament tournament(TournamentType type, String buttonTitle, String buttonImagePath, String format,
                                  String headerTitle, String description, List<String> prizes, Sponsor sponsor) {
        Tournament tournament = new Tournament();
        tournament.setType(type);
        tournament.setButtonTitle(buttonTitle);
        tournament.setButtonImagePath(buttonImagePath);
        tournament.setFormat(format);
        tournament.setHeaderTitle(headerTitle);
        tournament.setDescription(description);
        tournament.setPrizes(prizes);
        tournament.setSponsor(sponsor);
        return tournamentRepository.save(tournament);
    }
}
