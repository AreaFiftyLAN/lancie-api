package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Coordinate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Coordinate>{
    Seat findByCoordinate(Coordinate coordinate);
    Seat findByUser(User user);

    Optional<Seat> findByGroupnameAndSeatnumber(String groupname, int seatnumber);
}
