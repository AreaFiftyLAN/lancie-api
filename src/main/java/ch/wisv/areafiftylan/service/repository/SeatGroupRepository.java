package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Coordinate;
import ch.wisv.areafiftylan.model.util.SeatGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatGroupRepository extends JpaRepository<SeatGroup, String>{
    Optional<SeatGroup> findByName(String groupname);

    Optional<SeatGroup> findBySeats_User_Username(String username);
}
