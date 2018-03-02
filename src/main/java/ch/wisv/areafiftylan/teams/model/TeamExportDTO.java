package ch.wisv.areafiftylan.teams.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TeamExportDTO {
    private Long teamId;
    private String teamname;
    private Long captainId;
    private List<Long> members;
}
