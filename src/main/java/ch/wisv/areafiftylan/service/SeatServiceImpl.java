package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.SeatNotFoundException;
import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.SeatGroup;
import ch.wisv.areafiftylan.service.repository.SeatGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatServiceImpl implements SeatService {

    SeatGroupRepository seatGroupRepository;

    @Autowired
    public SeatServiceImpl(SeatGroupRepository seatGroupRepository) {
        this.seatGroupRepository = seatGroupRepository;
    }

    @Override
    public Seat getSeatByUser(User user) {
        SeatGroup seatGroup = this.seatGroupRepository.findBySeats_User_Username(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(user.getUsername()));

        for (Seat seat : seatGroup.getSeats().values()) {
            if (seat.getUser().equals(user)) {
                return seat;
            }
        }
        throw new UsernameNotFoundException(user.getUsername());
    }

    @Override
    public List<Seat> getAllSeats() {
        // Have fun deciphering this one
        return seatGroupRepository.findAll().stream().map(seatGroup -> seatGroup.getSeats().values())
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public List<SeatGroup> getAllSeatGroups() {
        return seatGroupRepository.findAll();
    }

    @Override
    public SeatGroup getSeatGroupByName(String groupname) {
        return seatGroupRepository.findByName(groupname).orElseThrow(() -> new SeatNotFoundException(groupname, 0));
    }

    @Override
    public boolean reserveSeat(String groupname, int seatnumber, User user) {
        SeatGroup seatGroup = seatGroupRepository.findByName(groupname)
                .orElseThrow(() -> new SeatNotFoundException(groupname, seatnumber));
        Seat seat = seatGroup.getSeats().get(seatnumber);
        if (!seat.isTaken()) {
            seat.setUser(user);
            seatGroupRepository.saveAndFlush(seatGroup);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Seat getSeatByGroupAndNumber(String groupName, int seatNumber) {
        SeatGroup seatGroup = seatGroupRepository.findByName(groupName)
                .orElseThrow(() -> new SeatNotFoundException(groupName, seatNumber));
        return seatGroup.getSeats().get(seatNumber);
    }

    @Override
    public void addSeats(String groupName, int seats) {
        SeatGroup seatGroup = new SeatGroup(groupName, seats);

        seatGroupRepository.save(seatGroup);
        seatGroupRepository.flush();
    }
}
