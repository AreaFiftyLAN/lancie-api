package ch.wisv.areafiftylan.products.service;

import ch.wisv.areafiftylan.exception.*;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.TicketOption;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.users.model.User;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;

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
        //TODO I can't get this method to do my bidding...
        /*
        User user = persistUser();
        Ticket ticket = ticketService.requestTicketOfType(user, TEST_TICKET, Arrays.asList(CH_MEMBER_OPTION, PICKUP_SERVICE_OPTION));
        testEntityManager.persist(ticket);
        Collection<Ticket> tickets = ticketService.findValidTicketsByOwnerUsername(user.getUsername());
        assertEquals(Collections.singleton(ticket), tickets);
        */
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
        testEntityManager.persist(ticket);
        assertEquals(false, ticket.isValid());
        ticketService.validateTicket(ticket.getId());
        assertEquals(true, ticket.isValid());
    }

    @Test
    public void validateTicketAlreadyValid() {
        Ticket ticket = persistTicket();
        ticket.setValid(true);
        testEntityManager.persist(ticket);
        assertEquals(true, ticket.isValid());
        ticketService.validateTicket(ticket.getId());
        assertEquals(true, ticket.isValid());
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
        TicketType type = ticketTypeRepository.findByName(TEST_TICKET).get();
        TicketOption pickupOption = ticketOptionRepository.findByName(PICKUP_SERVICE_OPTION).get();
        TicketOption chMemberOption = ticketOptionRepository.findByName(CH_MEMBER_OPTION).get();
        List<TicketOption> options = Arrays.asList(pickupOption, chMemberOption);
        Set<TicketOption> optionSet = new HashSet<>(options);
        String typeString = TEST_TICKET;
        List<String> optionsString = Arrays.asList(CH_MEMBER_OPTION, PICKUP_SERVICE_OPTION);

        Ticket ticket = ticketService.requestTicketOfType(user, typeString, optionsString);

        assertEquals(user, ticket.getOwner());
        assertEquals(optionSet, ticket.getEnabledOptions());
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
        String typeString = null;
        List<String> optionsString = Arrays.asList(CH_MEMBER_OPTION, PICKUP_SERVICE_OPTION);

        thrown.expect(TicketTypeNotFoundException.class);
        thrown.expectMessage("TicketType ");

        ticketService.requestTicketOfType(user, typeString, optionsString);
    }

    @Test
    public void requestTicketOfTypeStringsOptionsNotFound() {
        User user = persistUser();
        String typeString = TEST_TICKET;
        List<String> optionsString = Arrays.asList("not found", "nope.avi");

        thrown.expect(TicketOptionNotFoundException.class);
        thrown.expectMessage("Ticket Option not found!");

        ticketService.requestTicketOfType(user, typeString, optionsString);
    }

    @Test
    public void requestTicketOfTypeStringsOptionsNull() {
        User user = persistUser();
        TicketType type = ticketTypeRepository.findByName(TEST_TICKET).get();
        Set<TicketOption> optionSet = new HashSet<>();
        String typeString = TEST_TICKET;
        List<String> optionsString = null;

        Ticket ticket = ticketService.requestTicketOfType(user, typeString, optionsString);

        assertEquals(user, ticket.getOwner());
        assertEquals(optionSet, ticket.getEnabledOptions());
        assertEquals(type, ticket.getType());
    }

    @Test
    public void requestTicketOfTypeObjects() {
        User user = persistUser();
        TicketType type = ticketTypeRepository.findByName(TEST_TICKET).get();
        Ticket ticketToPersist = new Ticket(user, type);
        TicketOption pickupOption = ticketOptionRepository.findByName(PICKUP_SERVICE_OPTION).get();
        ticketToPersist.addOption(pickupOption);
        TicketOption chMemberOption = ticketOptionRepository.findByName(CH_MEMBER_OPTION).get();
        ticketToPersist.addOption(chMemberOption);
        List<TicketOption> options = Arrays.asList(pickupOption, chMemberOption);
        Set<TicketOption> optionSet = new HashSet<>(options);
        testEntityManager.persist(ticketToPersist);

        Ticket ticket = ticketService.requestTicketOfType(user, type, options);

        assertEquals(user, ticket.getOwner());
        assertEquals(optionSet, ticket.getEnabledOptions());
        assertEquals(type, ticket.getType());
    }

    @Test
    public void requestTicketOfTypeObjectsTypeNotAvailable() {
        User user = persistUser();
        TicketOption pickupOption = ticketOptionRepository.findByName(PICKUP_SERVICE_OPTION).get();
        TicketOption chMemberOption = ticketOptionRepository.findByName(CH_MEMBER_OPTION).get();
        List<TicketOption> options = Arrays.asList(pickupOption, chMemberOption);
        TicketType type = new TicketType("bla", "bla", 5F, 10, LocalDateTime.now().minusDays(1), true);
        type.addPossibleOption(pickupOption);
        type.addPossibleOption(chMemberOption);
        testEntityManager.persist(type);

        thrown.expect(TicketUnavailableException.class);
        thrown.expectMessage("Ticket is no longer available.");

        ticketService.requestTicketOfType(user, type, options);
    }

    @Test
    public void requestTicketOfTypeObjectsTypeNull() {
        User user = persistUser();
        TicketType type = null;
        TicketOption pickupOption = ticketOptionRepository.findByName(PICKUP_SERVICE_OPTION).get();
        TicketOption chMemberOption = ticketOptionRepository.findByName(CH_MEMBER_OPTION).get();
        List<TicketOption> options = Arrays.asList(pickupOption, chMemberOption);

        thrown.expect(TicketUnavailableException.class);
        thrown.expectMessage("Ticket is no longer available.");

        ticketService.requestTicketOfType(user, type, options);
    }

    @Test
    public void requestTicketOfTypeObjectsOptionsNotFound() {
        User user = persistUser();
        TicketType type = ticketTypeRepository.findByName(TEST_TICKET).get();
        TicketOption unavailableOption = new TicketOption("unavailable", 5F);
        List<TicketOption> options = Collections.singletonList(unavailableOption);

        thrown.expect(TicketOptionNotSupportedException.class);
        thrown.expectMessage("Ticket option ");

        ticketService.requestTicketOfType(user, type, options);
    }

    @Test
    public void requestTicketOfTypeObjectsOptionsNull() {
        User user = persistUser();
        TicketType type = ticketTypeRepository.findByName(TEST_TICKET).get();
        Ticket ticketToPersist = new Ticket(user, type);
        List<TicketOption> options = null;
        Set<TicketOption> optionSet = new HashSet<>();
        testEntityManager.persist(ticketToPersist);

        Ticket ticket = ticketService.requestTicketOfType(user, type, options);

        assertEquals(user, ticket.getOwner());
        assertEquals(optionSet, ticket.getEnabledOptions());
        assertEquals(type, ticket.getType());
    }

    @Test
    public void setupForTransfer() {

    }

    @Test
    public void transferTicket() {

    }

    @Test
    public void cancelTicketTransfer() {

    }

    @Test
    public void getOwnedTicketsAndFromTeamMembers() {

    }

    @Test
    public void getValidTicketTransferTokensByUser() {

    }

    @Test
    public void getAllTicketsWithTransport() {

    }

    @Test
    public void assignTicketToUser() {

    }

    @Test
    public void addTicketType() {

    }

    @Test
    public void getAllTicketTypes() {

    }

    @Test
    public void addTicketOption() {

    }

}