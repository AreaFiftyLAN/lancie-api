package ch.wisv.areafiftylan.web.service;

import ch.wisv.areafiftylan.web.model.Format;
import ch.wisv.areafiftylan.web.model.Platform;
import ch.wisv.areafiftylan.web.model.Sponsor;
import ch.wisv.areafiftylan.web.model.Tournament;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

@Service
public class WebTournamentServiceImpl {

    public Collection<Tournament> getAllTournaments() {
        return getDummyTournaments();
    }

    private Collection<Tournament> getDummyTournaments() {
        Collection<Tournament> tournaments = new ArrayList<>();
        tournaments.add(new Tournament("CS:GO", "5v5", "Counter Strike: Global Offensive",
                "Counter-Strike: Global Offensive (CS:GO) is a tactical first-person shooter developed by " +
                        "Valve Corporation and Hidden Path Entertainment. It is the fourth game in the main " +
                        "Counter-Strike franchise. Like the previous games in the series, Global Offensive is" +
                        " an objective-based multiplayer first-person shooter. Each player joins either the " +
                        "Terrorist or Counter-Terrorist team and attempts to complete objectives or eliminate" +
                        " the enemy team. The game operates in short rounds that end when all players on one " +
                        "side are dead or a team's objective is completed", "/images/background/tournaments/lol.jpg",
                new Sponsor("Logitech", "path/to/image", "logitech.com"),
                new LinkedList<>(Arrays.asList("Mooie koptelegoof", "Mooie koptelegoof", "Mooie koptelegoof")),
                Format.FIVE_VS_FIVE, LocalDateTime.now(), "link/to/rules", Platform.MAC, Platform.PC));

        tournaments.add(new Tournament("CS:GO", "5v5", "Counter Strike: Global Offensive",
                "Counter-Strike: Global Offensive (CS:GO) is a tactical first-person shooter developed by " +
                        "Valve Corporation and Hidden Path Entertainment. It is the fourth game in the main " +
                        "Counter-Strike franchise. Like the previous games in the series, Global Offensive is" +
                        " an objective-based multiplayer first-person shooter. Each player joins either the " +
                        "Terrorist or Counter-Terrorist team and attempts to complete objectives or eliminate" +
                        " the enemy team. The game operates in short rounds that end when all players on one " +
                        "side are dead or a team's objective is completed", "/images/background/tournaments/lol.jpg",
                new Sponsor("Logitech", "path/to/image", "logitech.com"),
                new LinkedList<>(Arrays.asList("Mooie koptelegoof", "Mooie koptelegoof", "Mooie koptelegoof")),
                Format.FIVE_VS_FIVE, LocalDateTime.now(), "link/to/rules", Platform.OFFLINE));
        return tournaments;
    }
}
