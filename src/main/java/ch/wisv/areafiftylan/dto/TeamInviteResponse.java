package ch.wisv.areafiftylan.dto;

import lombok.Getter;

/**
 * Created by Sille Kamoen on 9-3-16.
 */
public class TeamInviteResponse {
    @Getter
    private Long teamId;
    @Getter
    String teamName;
    @Getter
    String token;
    @Getter
    String username;

    public TeamInviteResponse(Long teamId, String teamName, String token, String username) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.token = token;
        this.username = username;
    }
}
