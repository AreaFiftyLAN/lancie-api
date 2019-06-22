package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.products.model.TicketOption;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.products.service.repository.TicketOptionRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketTypeRepository;
import ch.wisv.areafiftylan.seats.model.Seat;
import ch.wisv.areafiftylan.seats.service.SeatRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("test")
public class TestRunner implements CommandLineRunner {


    private final TicketOptionRepository ticketOptionRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final SeatRepository seatRepository;

    public TestRunner(TicketOptionRepository ticketOptionRepository, TicketTypeRepository ticketTypeRepository,
                      SeatRepository seatRepository) {
        this.ticketOptionRepository = ticketOptionRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    public void run(String... evt) throws Exception {
        TicketOption chMember = ticketOptionRepository.save(new TicketOption("chMember", -5F));
        TicketOption pickupService = ticketOptionRepository.save(new TicketOption("pickupService", 2.5F));
        TicketOption extraOption = ticketOptionRepository.save(new TicketOption("extraOption", 10F));

        TicketType ticketType =
                new TicketType("test", "Testing Ticket", 30F, 0, LocalDateTime.now().plusDays(1), true);
        ticketType.addPossibleOption(chMember);
        ticketType.addPossibleOption(pickupService);
        ticketTypeRepository.save(ticketType);

        for (int i = 1; i <= 5; i++) {
            Seat seat = new Seat("A", i);
            seat.setLocked(false);
            seatRepository.save(seat);
        }
    }
}
