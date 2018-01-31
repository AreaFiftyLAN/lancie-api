/*
 * Copyright (c) 2016  W.I.S.V. 'Christiaan Huygens'
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.wisv.areafiftylan.exception;

import ch.wisv.areafiftylan.web.sponsor.model.Sponsor;
import ch.wisv.areafiftylan.web.tournament.model.Tournament;

import java.util.stream.Collectors;

public class SponsorStillUsedInTournamentException extends AreaFiftyLANException {
    public SponsorStillUsedInTournamentException(Sponsor sponsor) {
        super(String.format(
                "Sponsor %s is still used by tournaments: %s",
                sponsor.getName(),
                String.join(", ", sponsor.getTournaments()
                        .stream()
                        .map(Tournament::getHeaderTitle)
                        .collect(Collectors.toList()))
        ));
    }

    public SponsorStillUsedInTournamentException() {
        super("Sponsor is still being used by one or more tournaments");
    }
}
