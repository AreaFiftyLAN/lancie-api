package ch.wisv.areafiftylan.dto;

/**
 * Created by beer on 7-1-16.
 */
public class TransferDTO {

    String ticketKey;

    String goalUsername;

    public String getTicketKey(){
        return ticketKey;
    }

    public void setTicketKey(String ticketKey){
        this.ticketKey = ticketKey;
    }

    public String getGoalUsername(){
        return goalUsername;
    }

    public void setGoalUsername(String goalUsername){
        this.goalUsername = goalUsername;
    }
}
