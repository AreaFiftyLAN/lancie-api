package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.TeamDTO;
import ch.wisv.areafiftylan.dto.TeamInviteResponse;
import ch.wisv.areafiftylan.exception.TeamNotFoundException;
import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.exception.UserNotFoundException;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.security.token.TeamInviteToken;
import ch.wisv.areafiftylan.security.token.Token;
import ch.wisv.areafiftylan.service.repository.TeamRepository;
import ch.wisv.areafiftylan.service.repository.token.TeamInviteTokenRepository;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final UserService userService;
    private final MailService mailService;
    private final TeamInviteTokenRepository teamInviteTokenRepository;

    @Autowired
    public TeamServiceImpl(TeamRepository teamRepository, UserService userService, MailService mailService,
                           TeamInviteTokenRepository teamInviteTokenRepository) {
        this.teamRepository = teamRepository;
        this.userService = userService;
        this.mailService = mailService;
        this.teamInviteTokenRepository = teamInviteTokenRepository;
    }

    @Override
    public Team create(String username, String teamname) {
        User captain =
                userService.getUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        Team team = new Team(teamname, captain);

        return teamRepository.saveAndFlush(team);
    }

    @Override
    public Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId));
    }

    @Override
    public Team getTeamByTeamname(String teamname) {
        return teamRepository.findByTeamNameIgnoreCase(teamname)
                .orElseThrow(() -> new TeamNotFoundException("Cant find team with  name " + teamname));
    }

    @Override
    public boolean teamnameUsed(String teamname) {
        return teamRepository.findByTeamNameIgnoreCase(teamname).isPresent();
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
        return teamRepository.findAllByMembersUsernameIgnoreCase(username);
    }

    @Override
    public Team update(Long teamId, TeamDTO input) {
        Team current = getTeamById(teamId);

        // If the Teamname is set, change the Teamname
        if (!Strings.isNullOrEmpty(input.getTeamName())) {
            current.setTeamName(input.getTeamName());
        }

        // If the Captain username is set and different from the current captain, change the Captain
        String captainUsername = input.getCaptainUsername();
        if (!Strings.isNullOrEmpty(captainUsername) && !captainUsername.equals(current.getCaptain().getUsername())) {
            User captain = userService.getUserByUsername(captainUsername)
                    .orElseThrow(() -> new UserNotFoundException(captainUsername));
            current.setCaptain(captain);
        }

        return teamRepository.saveAndFlush(current);
    }

    @Override
    public Team delete(Long teamId) {
        Team team = teamRepository.getOne(teamId);
        teamRepository.delete(teamId);
        return team;
    }

    @Override
    public TeamInviteToken inviteMember(Long teamId, String username) {
        User user = userService.getUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        Team team = getTeamById(teamId);

        // Check if the member isn't already part of the team
        if (team.getMembers().contains(user)) {
            throw new IllegalArgumentException("User is already a member of this team");
        }

        if (isUserAlreadyInvited(username, team)) {
            TeamInviteToken inviteToken = new TeamInviteToken(user, team);
            teamInviteTokenRepository.save(inviteToken);

            try {
                mailService.sendTeamInviteMail(user, team.getTeamName(), team.getCaptain());
            } catch (MessagingException e) {
                // TODO: Fix mailservice exception handling
                e.printStackTrace();
            }
            return inviteToken;
        } else {
            throw new IllegalArgumentException("User already invited");
        }
    }

    private boolean isUserAlreadyInvited(String username, Team team) {
        // Check if the member isn't already invited
        return teamInviteTokenRepository.findByUserUsernameIgnoreCase(username).stream().
                filter(token -> token.getTeam().equals(team)).
                noneMatch(TeamInviteToken::isValid);
    }

    @Override
    public void revokeInvite(String token) {
        TeamInviteToken teamInviteToken =
                teamInviteTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        teamInviteToken.revoke();
        teamInviteTokenRepository.save(teamInviteToken);

    }

    @Override
    public List<TeamInviteResponse> findTeamInvitesByUsername(String username) {
        Collection<TeamInviteToken> inviteTokens = teamInviteTokenRepository.findByUserUsernameIgnoreCase(username);

        return teamInviteTokensToReponses(inviteTokens);
    }

    @Override
    public List<TeamInviteResponse> findTeamInvitesByTeamId(Long teamId) {
        Collection<TeamInviteToken> inviteTokens = teamInviteTokenRepository.findByTeamId(teamId);

        return teamInviteTokensToReponses(inviteTokens);
    }

    private List<TeamInviteResponse> teamInviteTokensToReponses(Collection<TeamInviteToken> inviteTokens) {
        // From all Tokens that exist in the database linked to the user, only display the valid ones. Change
        // them to TeamInviteResponses for display in the controller.
        return inviteTokens.stream().
                filter(Token::isValid).
                map(t -> new TeamInviteResponse(t.getTeam().getId(), t.getTeam().getTeamName(), t.getToken(),
                        t.getUser().getUsername())).
                collect(Collectors.toList());
    }

    @Override
    public void addMemberByInvite(String token) {
        TeamInviteToken teamInviteToken =
                teamInviteTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        addMember(teamInviteToken.getTeam().getId(), teamInviteToken.getUser().getUsername());
        teamInviteToken.use();
        teamInviteTokenRepository.save(teamInviteToken);
    }

    @Override
    public void addMember(Long teamId, String username) {
        Team team = teamRepository.getOne(teamId);
        User user = userService.getUserByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        if (team.addMember(user)) {
            teamRepository.saveAndFlush(team);
        } else {
            throw new IllegalArgumentException("Could not add User to Team");
        }
    }

    @Override
    public boolean removeMember(Long teamId, String username) {
        Team team = getTeamById(teamId);
        User user = userService.getUserByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        if (team.getCaptain().equals(user)) {
            return false;
        }
        boolean success = team.removeMember(user);
        if (success) {
            teamRepository.saveAndFlush(team);
        }
        return success;

    }
}
