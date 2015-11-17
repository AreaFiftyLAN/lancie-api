package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.SeatNotFoundException;
import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Coordinate;
import ch.wisv.areafiftylan.service.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Override
    public Seat getSeatByGroupAndNumber(String groupName, int seatNumber) {
        return seatRepository.findByGroupnameAndSeatnumber(groupName, seatNumber)
                .orElseThrow(() -> new SeatNotFoundException(groupName, seatNumber));
    }

    @Override
    public void addSeats(String groupName, int seats) {
        List<Seat> seatsList = new ArrayList<>(seats);

        for (int i = 1; i <= seats; i++) {
            Seat seat = new Seat(groupName, i);
            seatsList.add(seat);
        }

        seatRepository.save(seatsList);
        seatRepository.flush();
    }
}
