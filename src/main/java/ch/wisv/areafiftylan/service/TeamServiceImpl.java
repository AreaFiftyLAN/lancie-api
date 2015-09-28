package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.TeamDTO;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final UserService userService;

    @Autowired
    public TeamServiceImpl(TeamRepository teamRepository, UserService userService) {
        this.teamRepository = teamRepository;
        this.userService = userService;
    }

    @Override
    public Team create(TeamDTO teamDTO) {
        User captain = userService.getUserById(teamDTO.getCaptainID()).get();
        Team team = new Team(teamDTO.getTeamName(), captain);

        return teamRepository.saveAndFlush(team);
    }

    @Override
    public Team save(Team team) {
        return teamRepository.saveAndFlush(team);
    }

    @Override
    public Optional<Team> getTeamById(Long teamId) {
        return teamRepository.findById(teamId);
    }

    @Override
    public Optional<Team> getTeamByTeamname(String teamname) {
        return teamRepository.findByTeamName(teamname);
    }

    @Override
    public Collection<Team> getTeamByCaptainId(Long userId) {
        return teamRepository.findByCaptainId(userId);
    }

    @Override
    public Collection<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @Override
    public Collection<Team> getTeamsByUsername(String username) {
//        return teamRepository.findByMembersUsername(username);
        return null;
    }

    @Override
    public Team update(Long teamId, TeamDTO input) {
        Team current = this.getTeamById(teamId).get();
        current.setTeamName(input.getTeamName());

        User captain = userService.getUserById(input.getCaptainID()).get();
        current.setCaptain(captain);

        return teamRepository.saveAndFlush(current);
    }

    @Override
    public void delete(Long teamId) {
        teamRepository.delete(teamId);
    }
}
