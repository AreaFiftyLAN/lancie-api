package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.service.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;

    @Autowired
    public TeamServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public Team getTeamById(Long id) {
        return teamRepository.findOne(id);
    }

    @Override
    public Team getTeamByTeamname(String teamname) {
        return teamRepository.findByTeamName(teamname);
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
}
