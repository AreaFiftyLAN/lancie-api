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

package ch.wisv.areafiftylan.web.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class Tournament extends Event {

    Sponsor sponsor;
    LinkedList<String> prizes;
    Format format;
    LocalDateTime startingDateTime;
    String rulePath;
    Collection<Platform> platform;

    public Tournament(String title, String subtitle, String headerTitle, String description, String backgroundImagePath,
                      Sponsor sponsor, LinkedList<String> prizes, Format format, LocalDateTime startingDateTime,
                      String rulePath, Platform... platforms) {
        super(title, subtitle, headerTitle, description, backgroundImagePath);
        this.sponsor = sponsor;
        this.prizes = prizes;
        this.format = format;
        this.startingDateTime = startingDateTime;
        this.rulePath = rulePath;
        this.platform = Arrays.asList(platforms);
    }

    public Collection<Platform> getPlatform() {
        return platform;
    }

    public void setPlatform(Collection<Platform> platform) {
        this.platform = platform;
    }

    public Sponsor getSponsor() {
        return sponsor;
    }

    public void setSponsor(Sponsor sponsor) {
        this.sponsor = sponsor;
    }

    public LinkedList<String> getPrizes() {
        return prizes;
    }

    public void setPrizes(LinkedList<String> prizes) {
        this.prizes = prizes;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public LocalDateTime getStartingDateTime() {
        return startingDateTime;
    }

    public void setStartingDateTime(LocalDateTime startingDateTime) {
        this.startingDateTime = startingDateTime;
    }

    public String getRulePath() {
        return rulePath;
    }

    public void setRulePath(String rulePath) {
        this.rulePath = rulePath;
    }
}
