package ch.wisv.areafiftylan.model.relations;

import ch.wisv.areafiftylan.exception.InvalidRFIDException;
import ch.wisv.areafiftylan.model.Ticket;

import javax.persistence.*;

/**
 * Created by beer on 5-5-16.
 */
@Entity
public class RFIDLink {
    @Id
    @GeneratedValue
    private Long id;

    private String rfid;

    @OneToOne(cascade = CascadeType.MERGE)
    private Ticket ticket;

    public RFIDLink(){
        //JPA Only
    }

    public RFIDLink(String rfid, Ticket ticket) {
        if(!isValidRFID(rfid)){
            throw new InvalidRFIDException(rfid);
        }

        this.rfid = rfid;
        this.ticket = ticket;
    }

    public String getRFID(){
        return rfid;
    }

    public Ticket getTicket(){
        return ticket;
    }

    //Static Content
    public static final int RFID_CHAR_COUNT = 10;

    public static boolean isValidRFID(String rfid){
        return rfid.length() == RFID_CHAR_COUNT;
    }
}
