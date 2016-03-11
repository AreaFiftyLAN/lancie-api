package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRespository extends JpaRepository<Seat, Long> {
    Optional<Seat> findByUserUsername(String username);

    List<Seat> findBySeatGroup(String seatGroup);

    Seat findBySeatGroupAndSeatNumber(String seatGroup, int seatNumber);
}
