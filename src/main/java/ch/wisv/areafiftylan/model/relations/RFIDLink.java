package ch.wisv.areafiftylan.model.relations;

import ch.wisv.areafiftylan.exception.InvalidRFIDException;
import ch.wisv.areafiftylan.model.Ticket;

/**
 * Created by beer on 5-5-16.
 */
public class RFIDLink {
    public static final int RFID_CHAR_COUNT = 10;

    private String RFID;
    private Ticket ticket;

    public RFIDLink(){
        //JPA Only
    }

    public RFIDLink(String RFID, Ticket ticket) {
        if(RFID.length() != RFID_CHAR_COUNT){
            throw new InvalidRFIDException(RFID);
        }

        this.RFID = RFID;
        this.ticket = ticket;
    }

    public String getRFID(){
        return RFID;
    }

    public Ticket getTicket(){
        return ticket;
    }
}
