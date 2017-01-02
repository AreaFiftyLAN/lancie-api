package ch.wisv.areafiftylan.products.service;

import ch.wisv.areafiftylan.exception.*;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLink;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.TicketOption;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import ch.wisv.areafiftylan.users.model.User;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;


public class TicketServiceTest extends ServiceTest {

    @Test
    public void getTicketById() {
        Long id = persistTicket().getId();
        Ticket ticket = ticketService.getTicketById(id);
        assertEquals(id, ticket.getId());
    }

    @Test
    public void getTicketByIdNotFound() {
        Long id = 9999L;
        thrown.expect(TicketNotFoundException.class);
        thrown.expectMessage("Ticket not found");
        ticketService.getTicketById(id);
    }

    @Test
    public void getTicketByIdNull() {
        thrown.expect(TicketNotFoundException.class);
        thrown.expectMessage("Ticket not found");
        ticketService.getTicketById(null);
    }

    @Test
    public void removeTicket() {
        Long id = persistTicket().getId();
        Ticket ticket = ticketService.removeTicket(id);
        assertEquals(id, ticket.getId());
        assertNull(ticketRepository.findOne(id));
    }

    @Test
    public void removeTicketNotFound() {
        Long id = 9999L;
        thrown.expect(TicketNotFoundException.class);
        thrown.expectMessage("Ticket not found");
        ticketService.removeTicket(id);
    }

    @Test
    public void removeTicketNull() {
        thrown.expect(TicketNotFoundException.class);
        thrown.expectMessage("Ticket not found");
        ticketService.removeTicket(null);
    }

    @Test
    public void getNumberSoldOfTypeZero() {
        Ticket ticket1 = persistTicket();
        TicketType ticketType = ticket1.getType();
        ticketService.removeTicket(ticket1.getId());
        assertEquals(Integer.valueOf(0), ticketService.getNumberSoldOfType(ticketType));
    }

    @Test
    public void getNumberSoldOfTypeOne() {
        Ticket ticket1 = persistTicket();
        TicketType ticketType = ticket1.getType();
        assertEquals(Integer.valueOf(1), ticketService.getNumberSoldOfType(ticketType));
    }

    @Test
    public void getNumberSoldOfTypeAdd() {
        Ticket ticket1 = persistTicket();
        TicketType ticketType = ticket1.getType();
        assertEquals(Integer.valueOf(1), ticketService.getNumberSoldOfType(ticketType));
        persistTicket();
        assertEquals(Integer.valueOf(2), ticketService.getNumberSoldOfType(ticketType));
    }

    @Test
    public void getNumberSoldOfTypeRemove() {
        Ticket ticket1 = persistTicket();
        TicketType ticketType = ticket1.getType();
        persistTicket();
        assertEquals(Integer.valueOf(2), ticketService.getNumberSoldOfType(ticketType));
        ticketService.removeTicket(ticket1.getId());
        assertEquals(Integer.valueOf(1), ticketService.getNumberSoldOfType(ticketType));
    }

    @Test
    public void getNumberSoldOfTypeTypeNull() {
        assertEquals(Integer.valueOf(0), ticketService.getNumberSoldOfType(null));
    }

    @Test
    public void findValidTicketsByOwnerUsername() {
        User user = persistUser();

        Ticket ticket = persistTicketForUser(user);
        Ticket ticket2 = persistTicketForUser(user);
        ticket2.setValid(false);
        testEntityManager.persist(ticket2);

        Collection<Ticket> validTicketsByOwnerUsername =
                ticketService.findValidTicketsByOwnerUsername(user.getUsername());

        assertThat(validTicketsByOwnerUsername).containsExactly(ticket);
    }

    @Test
    public void getAllTicketsZero() {
        Collection<Ticket> tickets = ticketService.getAllTickets();
        assertEquals(0, tickets.size());
    }

    @Test
    public void getAllTicketsOne() {
        persistTicket();
        Collection<Ticket> tickets = ticketService.getAllTickets();
        assertEquals(1, tickets.size());
    }

    @Test
    public void getAllTicketsTwo() {
        persistTicket();
        persistTicket();
        Collection<Ticket> tickets = ticketService.getAllTickets();
        assertEquals(2, tickets.size());
    }

    @Test
    public void validateTicketFromInvalid() {
        Ticket ticket = persistTicket();
        ticket.setValid(false);
        ticket = testEntityManager.persist(ticket);
        assertEquals(false, ticket.isValid());

        ticketService.validateTicket(ticket.getId());

        assertEquals(true, testEntityManager.find(Ticket.class, ticket.getId()).isValid());
    }

    @Test
    public void validateTicketAlreadyValid() {
        Ticket ticket = persistTicket();
        ticket.setValid(true);
        ticket = testEntityManager.persist(ticket);
        assertEquals(true, ticket.isValid());
        ticketService.validateTicket(ticket.getId());
        assertEquals(true, testEntityManager.find(Ticket.class, ticket.getId()).isValid());
    }

    @Test
    public void validateTicketNotFound() {
        Long id = 9999L;
        thrown.expect(TicketNotFoundException.class);
        thrown.expectMessage("Ticket not found");
        ticketService.validateTicket(id);
    }

    @Test
    public void validateTicketNull() {
        thrown.expect(TicketNotFoundException.class);
        thrown.expectMessage("Ticket not found");
        ticketService.validateTicket(null);
    }

    @Test
    public void requestTicketOfTypeStrings() {
        User user = persistUser();
        TicketType type = ticketTypeRepository.findByName(TEST_TICKET)
                .orElseThrow(() -> new TicketTypeNotFoundException(TEST_TICKET));
        TicketOption pickupOption = ticketOptionRepository.findByName(PICKUP_SERVICE_OPTION)
                .orElseThrow(TicketOptionNotFoundException::new);
        TicketOption chMemberOption =
                ticketOptionRepository.findByName(CH_MEMBER_OPTION).orElseThrow(TicketOptionNotFoundException::new);

        List<String> optionsList = Arrays.asList(CH_MEMBER_OPTION, PICKUP_SERVICE_OPTION);

        Ticket ticket = ticketService.requestTicketOfType(user, TEST_TICKET, optionsList);

        assertEquals(user, ticket.getOwner());
        assertThat(ticket.getEnabledOptions()).containsAll(Arrays.asList(pickupOption, chMemberOption));
        assertEquals(type, ticket.getType());
    }

    @Test
    public void requestTicketOfTypeStringsTypeNotAvailable() {
        User user = persistUser();
        String typeString = "unavailable_ticket";
        List<String> optionsString = Arrays.asList(CH_MEMBER_OPTION, PICKUP_SERVICE_OPTION);

        thrown.expect(TicketTypeNotFoundException.class);
        thrown.expectMessage("TicketType ");

        ticketService.requestTicketOfType(user, typeString, optionsString);
    }

    @Test
    public void requestTicketOfTypeStringsTypeNull() {
        User user = persistUser();
        List<String> optionsString = Arrays.asList(CH_MEMBER_OPTION, PICKUP_SERVICE_OPTION);

        thrown.expect(TicketTypeNotFoundException.class);
        thrown.expectMessage("TicketType ");

        ticketService.requestTicketOfType(user, null, optionsString);
    }

    @Test
    public void requestTicketOfTypeStringsOptionsNotFound() {
        User user = persistUser();
        List<String> optionsString = Arrays.asList("not found", "nope.avi");

        thrown.expect(TicketOptionNotFoundException.class);
        thrown.expectMessage("Ticket Option not found!");

        ticketService.requestTicketOfType(user, TEST_TICKET, optionsString);
    }

    @Test
    public void requestTicketOfTypeStringsOptionsNull() {
        User user = persistUser();

        Ticket ticket = ticketService.requestTicketOfType(user, TEST_TICKET, null);

        assertEquals(user, ticket.getOwner());
        assertThat(ticket.getEnabledOptions()).isEmpty();
        assertEquals(TEST_TICKET, ticket.getType().getName());
    }

    @Test
    public void requestTicketOfTypeObjects() {
        User user = persistUser();
        TicketType type = ticketTypeRepository.findByName(TEST_TICKET)
                .orElseThrow(() -> new TicketTypeNotFoundException(TEST_TICKET));
        TicketOption pickupOption = ticketOptionRepository.findByName(PICKUP_SERVICE_OPTION)
                .orElseThrow(TicketOptionNotFoundException::new);
        TicketOption chMemberOption =
                ticketOptionRepository.findByName(CH_MEMBER_OPTION).orElseThrow(TicketOptionNotFoundException::new);

        List<TicketOption> optionsList = Arrays.asList(chMemberOption, pickupOption);

        Ticket ticket = ticketService.requestTicketOfType(user, type, optionsList);

        assertEquals(user, ticket.getOwner());
        assertThat(ticket.getEnabledOptions()).containsAll(Arrays.asList(pickupOption, chMemberOption));
        assertEquals(type, ticket.getType());
    }

    @Test
    public void requestTicketDeadlinePassed() {
        User user = persistUser();
        TicketType type =
                new TicketType("unavailable", "Unavailable TicketType", 5F, 10, LocalDateTime.now().minusDays(1), true);
        testEntityManager.persist(type);

        thrown.expect(TicketUnavailableException.class);
        thrown.expectMessage("Ticket is no longer available.");

        ticketService.requestTicketOfType(user, type, Collections.emptyList());
    }

    @Test
    public void requestTicketOfTypeObjectsTypeNull() {
        User user = persistUser();

        thrown.expect(TicketUnavailableException.class);
        thrown.expectMessage("Ticket is no longer available.");

        ticketService.requestTicketOfType(user, null, new ArrayList<TicketOption>());
    }

    @Test
    public void requestTicketOfTypeObjectsOptionsNotFound() {
        User user = persistUser();
        TicketType type = ticketTypeRepository.findByName(TEST_TICKET)
                .orElseThrow(() -> new TicketTypeNotFoundException(TEST_TICKET));
        TicketOption unavailableOption = new TicketOption("unavailable", 5F);
        List<TicketOption> options = Collections.singletonList(unavailableOption);

        thrown.expect(TicketOptionNotSupportedException.class);
        thrown.expectMessage("Ticket option ");

        ticketService.requestTicketOfType(user, type, options);
    }

    @Test
    public void requestTicketOfTypeObjectsOptionsNull() {
        User user = persistUser();
        TicketType type = ticketTypeRepository.findByName(TEST_TICKET)
                .orElseThrow(() -> new TicketTypeNotFoundException(TEST_TICKET));
        Ticket ticketToPersist = new Ticket(user, type);
        testEntityManager.persist(ticketToPersist);

        Ticket ticket = ticketService.requestTicketOfType(user, type, null);

        assertEquals(user, ticket.getOwner());
        assertThat(ticket.getEnabledOptions()).isEmpty();
        assertEquals(type, ticket.getType());
    }

    @Test
    public void setupForTransfer() {
        User goalUser = persistUser();
        String goalUsername = goalUser.getUsername();
        Ticket ticket = persistTicket();
        Long ticketId = ticket.getId();

        TicketTransferToken ttt = ticketService.setupForTransfer(ticketId, goalUsername);

        assertEquals(ticket, ttt.getTicket());
        assertEquals(goalUser, ttt.getUser());
        assertEquals(true, ttt.isValid());
    }

    @Test
    public void setupForTransferDuplicate() {
        User goalUser = persistUser();
        String goalUsername = goalUser.getUsername();
        Ticket ticket = persistTicket();
        Long ticketId = ticket.getId();

        thrown.expect(DuplicateTicketTransferTokenException.class);
        thrown.expectMessage(" is already set up for transfer!");

        ticketService.setupForTransfer(ticketId, goalUsername);
        ticketService.setupForTransfer(ticketId, goalUsername);
    }

    @Test
    public void setupForTransferAlreadyLinked() {
        User goalUser = persistUser();
        String goalUsername = goalUser.getUsername();
        Ticket ticket = persistTicket();
        Long ticketId = ticket.getId();
        testEntityManager.persist(new RFIDLink("1234567890", ticket));

        thrown.expect(TicketAlreadyLinkedException.class);
        thrown.expectMessage("Ticket has already been linked to a RFID");

        ticketService.setupForTransfer(ticketId, goalUsername);
    }

    @Test
    public void transferTicket() {
        User goalUser = persistUser();
        String goalUsername = goalUser.getUsername();
        Ticket ticket = persistTicket();
        Long ticketId = ticket.getId();
        TicketTransferToken ttt = ticketService.setupForTransfer(ticketId, goalUsername);
        String token = ttt.getToken();

        ticketService.transferTicket(token);

        assertEquals(goalUser, ticket.getOwner());
        assertFalse(ttt.isUnused());
    }

    @Test
    public void transferTicketTokenNotFound() {
        thrown.expect(TokenNotFoundException.class);
        thrown.expectMessage("Could not find token ");

        ticketService.transferTicket("invalid_token");
    }

    @Test
    public void transferTicketTokenNull() {
        thrown.expect(TokenNotFoundException.class);
        thrown.expectMessage("Could not find token ");

        ticketService.transferTicket(null);
    }

    @Test
    public void transferTicketTokenInvalid() {
        User goalUser = persistUser();
        String goalUsername = goalUser.getUsername();
        Ticket ticket = persistTicket();
        Long ticketId = ticket.getId();
        TicketTransferToken ttt = ticketService.setupForTransfer(ticketId, goalUsername);
        String token = ttt.getToken();

        ttt.use();

        thrown.expect(InvalidTokenException.class);
        thrown.expectMessage("Token is expired, has been already used or has been revoked.");

        ticketService.transferTicket(token);
    }

    @Test
    public void transferTicketAlreadyLinked() {
        User goalUser = persistUser();
        String goalUsername = goalUser.getUsername();
        Ticket ticket = persistTicket();
        Long ticketId = ticket.getId();
        TicketTransferToken ttt = ticketService.setupForTransfer(ticketId, goalUsername);
        String token = ttt.getToken();

        // Creating the RFIDLink needs to happen after setupForTransfer.
        testEntityManager.persist(new RFIDLink("1234567890", ticket));

        thrown.expect(TicketAlreadyLinkedException.class);
        thrown.expectMessage("Ticket has already been linked to a RFID");

        ticketService.transferTicket(token);
    }

    @Test
    public void cancelTicketTransfer() {
        User goalUser = persistUser();
        String goalUsername = goalUser.getUsername();
        Ticket ticket = persistTicket();
        Long ticketId = ticket.getId();
        TicketTransferToken ttt = ticketService.setupForTransfer(ticketId, goalUsername);
        String token = ttt.getToken();

        ticketService.cancelTicketTransfer(token);

        assertFalse(ttt.isValid());
        assertTrue(ttt.isUnused());
        assertNotEquals(goalUser, ticket.getOwner());
    }

    @Test
    public void cancelTicketTransferTokenNotFound() {
        thrown.expect(TokenNotFoundException.class);
        thrown.expectMessage("Could not find token ");

        ticketService.cancelTicketTransfer("invalid_token");
    }

    @Test
    public void cancelTicketTransferTokenNull() {
        thrown.expect(TokenNotFoundException.class);
        thrown.expectMessage("Could not find token ");

        ticketService.cancelTicketTransfer(null);
    }

    @Test
    public void cancelTicketTransferTokenInvalid() {
        User goalUser = persistUser();
        String goalUsername = goalUser.getUsername();
        Ticket ticket = persistTicket();
        Long ticketId = ticket.getId();
        TicketTransferToken ttt = ticketService.setupForTransfer(ticketId, goalUsername);
        String token = ttt.getToken();

        ttt.use();

        thrown.expect(InvalidTokenException.class);
        thrown.expectMessage("Token is expired, has been already used or has been revoked.");

        ticketService.cancelTicketTransfer(token);
    }

    @Test
    public void getOwnedTicketsAndFromTeamMembersFull() {
        User captain = persistUser();
        Ticket captainTicket = persistTicketForUser(captain);
        User member1Team1 = persistUser();
        Ticket member1Team1Ticket = persistTicketForUser(member1Team1);
        User member2Team1 = persistUser();
        Ticket member2Team1Ticket = persistTicketForUser(member2Team1);
        User member1Team2 = persistUser();
        Ticket member1Team2Ticket = persistTicketForUser(member1Team2);
        User member2Team2 = persistUser();
        Ticket member2Team2Ticket = persistTicketForUser(member2Team2);
        persistTeamWithCaptainAndMembers("Team1", captain, Arrays.asList(member1Team1, member2Team1));
        persistTeamWithCaptainAndMembers("Team2", captain, Arrays.asList(member1Team2, member2Team2));

        Collection<Ticket> tickets = ticketService.getOwnedTicketsAndFromTeamMembers(captain);

        assertThat(tickets).containsAll(Arrays.asList(captainTicket, member1Team1Ticket, member2Team1Ticket, member1Team2Ticket,
                member2Team2Ticket));
    }

    @Test
    public void getOwnedTicketsAndFromTeamMembersOnlyCaptainInTeams() {
        User captain = persistUser();
        Ticket captainTicket = persistTicketForUser(captain);
        persistTeamWithCaptain("Team1", captain);
        persistTeamWithCaptain("Team2", captain);

        Collection<Ticket> tickets = ticketService.getOwnedTicketsAndFromTeamMembers(captain);

        assertThat(tickets).containsExactly(captainTicket);
    }

    @Test
    public void getOwnedTicketsAndFromTeamMembersOnlyCaptainNoTeams() {
        User captain = persistUser();
        Ticket captainTicket = persistTicketForUser(captain);

        Collection<Ticket> tickets = ticketService.getOwnedTicketsAndFromTeamMembers(captain);

        assertThat(tickets).containsExactly(captainTicket);
    }

    @Test
    public void getOwnedTicketsAndFromTeamMembersNull() {
        thrown.expect(IllegalArgumentException.class);

        ticketService.getOwnedTicketsAndFromTeamMembers(null);
    }

    @Test
    public void getValidTicketTransferTokensByUser() {
        User owner = persistUser();
        User goalUser = persistUser();
        Ticket ticket1 = persistTicketForUser(owner);
        Ticket ticket2 = persistTicketForUser(owner);
        Ticket ticket3 = persistTicketForUser(goalUser);
        TicketTransferToken ttt1 = ticketService.setupForTransfer(ticket1.getId(), goalUser.getUsername());
        TicketTransferToken ttt2 = ticketService.setupForTransfer(ticket2.getId(), goalUser.getUsername());
        ticketService.setupForTransfer(ticket3.getId(), owner.getUsername());
        ttt1.use();
        testEntityManager.persist(ttt1);

        Collection<TicketTransferToken> tttc = ticketService.getValidTicketTransferTokensByUser(owner.getUsername());

        assertEquals(tttc, Collections.singletonList(ttt2));
    }

    @Test
    public void getValidTicketTransferTokensByUserNotFound() {
        Collection<TicketTransferToken> tttc = ticketService.getValidTicketTransferTokensByUser("doesnt_exist");
        assertTrue(tttc.isEmpty());
    }

    @Test
    public void getValidTicketTransferTokensByUserNull() {
        Collection<TicketTransferToken> tttc = ticketService.getValidTicketTransferTokensByUser(null);
        assertTrue(tttc.isEmpty());
    }

    @Test
    public void getAllTicketsWithTransport() {
        Ticket ticketWithPickup = persistTicket();
        Ticket ticketWithPickup2 = persistTicket();
        Ticket ticketWithoutPickup =
                ticketService.requestTicketOfType(TEST_TICKET, Collections.singletonList(CH_MEMBER_OPTION));
        ticketWithoutPickup.setValid(true);
        testEntityManager.persist(ticketWithoutPickup);

        Collection<Ticket> tickets = ticketService.getAllTicketsWithTransport();

        assertEquals(tickets, Arrays.asList(ticketWithPickup, ticketWithPickup2));
    }

    @Test
    public void getAllTicketsWithTransportZero() {
        Ticket ticketWithoutPickup =
                ticketService.requestTicketOfType(TEST_TICKET, Collections.singletonList(CH_MEMBER_OPTION));
        ticketWithoutPickup.setValid(true);
        testEntityManager.persist(ticketWithoutPickup);

        Collection<Ticket> tickets = ticketService.getAllTicketsWithTransport();

        assertThat(tickets).isEmpty();
    }

    @Test
    public void assignTicketToUserAnonymous() {
        User user = persistUser();
        Ticket ticket = persistTicket();
        Ticket result = ticketService.assignTicketToUser(ticket.getId(), user.getUsername());
        assertEquals(user, result.getOwner());
    }

    @Test
    public void assignTicketToUserFromOtherUser() {
        User user = persistUser();
        User owner = persistUser();
        Ticket ticket = persistTicketForUser(owner);
        assertEquals(owner, ticket.getOwner());
        Ticket result = ticketService.assignTicketToUser(ticket.getId(), user.getUsername());
        assertEquals(user, result.getOwner());
    }

    @Test
    public void addTicketType() {
        long countBefore = ticketTypeRepository.count();
        TicketType ticketType = new TicketType("type1", "text", 5F, 0, LocalDateTime.now(), true);
        ticketService.addTicketType(ticketType);
        assertEquals(countBefore + 1, ticketTypeRepository.count());
    }

    @Test
    public void addTicketTypeTwo() {
        long countBefore = ticketTypeRepository.count();
        TicketType ticketType1 = new TicketType("type1", "text", 5F, 0, LocalDateTime.now(), true);
        ticketService.addTicketType(ticketType1);
        TicketType ticketType2 = new TicketType("type2", "text", 6F, 0, LocalDateTime.now(), true);
        ticketService.addTicketType(ticketType2);
        assertEquals(countBefore + 2, ticketTypeRepository.count());
    }

    @Test
    public void getAllTicketTypes() {
        long countBefore = ticketTypeRepository.count();
        Collection<TicketType> types = ticketService.getAllTicketTypes();
        assertEquals(countBefore, types.size());
    }

    @Test
    public void getAllTicketTypesOneExtra() {
        long countBefore = ticketTypeRepository.count();
        TicketType ticketType1 = new TicketType("type1", "text", 5F, 0, LocalDateTime.now(), true);
        ticketService.addTicketType(ticketType1);
        Collection<TicketType> types = ticketService.getAllTicketTypes();
        assertEquals(countBefore + 1, types.size());
    }

    @Test
    public void getAllTicketTypesTwoExtra() {
        long countBefore = ticketTypeRepository.count();
        TicketType ticketType1 = new TicketType("type1", "text", 5F, 0, LocalDateTime.now(), true);
        ticketService.addTicketType(ticketType1);
        TicketType ticketType2 = new TicketType("type2", "text", 6F, 0, LocalDateTime.now(), true);
        ticketService.addTicketType(ticketType2);
        Collection<TicketType> types = ticketService.getAllTicketTypes();
        assertEquals(countBefore + 2, types.size());
    }

    @Test
    public void addTicketOption() {
        long countBefore = ticketOptionRepository.count();
        TicketOption option = new TicketOption("option1", 2.5F);
        ticketService.addTicketOption(option);
        assertEquals(countBefore + 1, ticketOptionRepository.count());
    }

    @Test
    public void addTicketOptionTwo() {
        long countBefore = ticketOptionRepository.count();
        TicketOption option1 = new TicketOption("option1", 2.5F);
        ticketService.addTicketOption(option1);
        TicketOption option2 = new TicketOption("option1", 2.5F);
        ticketService.addTicketOption(option2);
        assertEquals(countBefore + 2, ticketOptionRepository.count());
    }
}