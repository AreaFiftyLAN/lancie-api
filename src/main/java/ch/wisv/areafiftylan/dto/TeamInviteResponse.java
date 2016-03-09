package ch.wisv.areafiftylan.dto;

import lombok.Getter;

import java.util.Date;

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
    Date expiryDate;

    public TeamInviteResponse(Long teamId, String teamName, String token, Date expiryDate) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.token = token;
        this.expiryDate = expiryDate;
    }
}
