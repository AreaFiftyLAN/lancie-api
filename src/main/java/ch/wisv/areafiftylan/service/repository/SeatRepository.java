package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByTicketOwnerUsernameIgnoreCase(String username);

    List<Seat> findBySeatGroup(String seatGroup);

    Seat findBySeatGroupAndSeatNumber(String seatGroup, int seatNumber);

    Optional<Seat> findByTicketId(Long ticketId);
}
