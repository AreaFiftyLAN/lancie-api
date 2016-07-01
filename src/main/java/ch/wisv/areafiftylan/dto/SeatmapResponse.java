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

package ch.wisv.areafiftylan.dto;

import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.view.View;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.List;
import java.util.Map;

/**
 * Created by sille on 10-1-16.
 */
public class SeatmapResponse {

    @JsonView(View.Public.class)
    public Map<String, List<Seat>> seatmap;

    public SeatmapResponse(Map<String, List<Seat>> seatmap) {
        this.seatmap = seatmap;
    }

    public Map<String, List<Seat>> getSeatmap() {
        return seatmap;
    }

    public void setSeatmap(Map<String, List<Seat>> seatmap) {
        this.seatmap = seatmap;
    }
}
