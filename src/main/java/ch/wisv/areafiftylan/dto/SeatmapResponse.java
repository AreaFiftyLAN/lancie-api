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
