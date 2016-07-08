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

package ch.wisv.areafiftylan.web.service;

import ch.wisv.areafiftylan.web.model.Sponsor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class SponsorServiceImpl {

    public Collection<Sponsor> getAllSponsors() {
        return getDummySponspors();
    }

    private Collection<Sponsor> getDummySponspors() {
        Collection<Sponsor> sponspors = new ArrayList<>();
        sponspors.add(new Sponsor("Logitech", "path/to/image", "logitech.com"));
        sponspors.add(new Sponsor("Azerty", "path/to/image", "schralebazen.com"));
        sponspors.add(new Sponsor("Ordina", "path/to/image", "ordina.com"));
        sponspors.add(new Sponsor("Plantronics", "path/to/image", "koptelefoons.com"));
        sponspors.add(new Sponsor("OCZ", "path/to/image", "ocz.com"));
        return sponspors;
    }
}
