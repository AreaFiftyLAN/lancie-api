package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Coordinate;
import ch.wisv.areafiftylan.service.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatServiceImpl implements SeatService {

    SeatRepository seatRepository;

    @Autowired
    public SeatServiceImpl(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Override
    public Seat getSeatByUser(User user) {
        return this.seatRepository.findByUser(user);
    }

    @Override
    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    @Override
    public Seat getSeatByCoordinate(Coordinate coordinate) {
        return seatRepository.findByCoordinate(coordinate);
    }

    @Override
    public Seat reserveSeat(Coordinate coordinate, User user) {
        Seat seat = seatRepository.findByCoordinate(coordinate);
        seat.setUser(user);
        return seatRepository.saveAndFlush(seat);
    }
}
