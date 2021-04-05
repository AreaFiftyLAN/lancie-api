package ch.wisv.areafiftylan.utils.setup;

import ch.wisv.areafiftylan.extras.consumption.model.ConsumptionMapsRepository;
import ch.wisv.areafiftylan.extras.consumption.model.PossibleConsumptionsRepository;
import ch.wisv.areafiftylan.extras.rfid.service.RFIDLinkRepository;
import ch.wisv.areafiftylan.products.service.repository.*;
import ch.wisv.areafiftylan.seats.service.SeatRepository;
import ch.wisv.areafiftylan.security.token.Token;
import ch.wisv.areafiftylan.security.token.repository.AuthenticationTokenRepository;
import ch.wisv.areafiftylan.security.token.repository.TokenRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetupService {
    private final List<TokenRepository<? extends Token>> tokenRepositories;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final RFIDLinkRepository rfidLinkRepository;
    private final ExpiredOrderRepository expiredOrderRepository;

    private final TicketOptionRepository ticketOptionRepository;
    private final TicketTypeRepository ticketTypeRepository;

    private final ConsumptionMapsRepository consumptionMapsRepository;
    private final PossibleConsumptionsRepository possibleConsumptionsRepository;

    private final SeatRepository seatRepository;

    public SetupService(List<TokenRepository<?>> tokenRepositories, OrderRepository orderRepository, TicketRepository ticketRepository, RFIDLinkRepository rfidLinkRepository, ExpiredOrderRepository expiredOrderRepository, TicketOptionRepository ticketOptionRepository, TicketTypeRepository ticketTypeRepository, ConsumptionMapsRepository consumptionMapsRepository, PossibleConsumptionsRepository possibleConsumptionsRepository, SeatRepository seatRepository) {
        this.tokenRepositories = tokenRepositories;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.rfidLinkRepository = rfidLinkRepository;
        this.expiredOrderRepository = expiredOrderRepository;
        this.ticketOptionRepository = ticketOptionRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.consumptionMapsRepository = consumptionMapsRepository;
        this.possibleConsumptionsRepository = possibleConsumptionsRepository;
        this.seatRepository = seatRepository;
    }

    /**
     * This method deletes all data related to a single event. Only Users, their Profiles, Teams, UpdateSubscriptions
     * and web-based data remains. This action is irreversible and final!
     */
    @Transactional
    public void deleteAllCurrentEventData() {
        // Delete all tokens
        for (TokenRepository<? extends Token> tokenRepository : tokenRepositories) {
            if (tokenRepository instanceof AuthenticationTokenRepository) {
                List<? extends Token> invalidTokens = tokenRepository.findAll().stream().filter(t -> !t.isValid()).collect(Collectors.toList());
                for (Token invalidToken : invalidTokens) {
                    tokenRepository.deleteById(invalidToken.getId());
                }
            }
            tokenRepository.deleteAll();
        }
        ticketRepository.deleteAll();

        rfidLinkRepository.deleteAll();

        consumptionMapsRepository.deleteAll();
        possibleConsumptionsRepository.deleteAll();

        seatRepository.deleteAll();

        orderRepository.deleteAll();
        expiredOrderRepository.deleteAll();

        ticketTypeRepository.deleteAll();
        ticketOptionRepository.deleteAll();
    }

    public void checkDeletion() {
        System.out.println("hi");
    }
}
