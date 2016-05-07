package ch.wisv.areafiftylan.model.relations;

import ch.wisv.areafiftylan.exception.InvalidRFIDException;
import ch.wisv.areafiftylan.model.Ticket;

import javax.persistence.*;

/**
 * Created by beer on 5-5-16.
 */
@Entity
public class RFIDLink {
    public static final int RFID_CHAR_COUNT = 10;

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
        if(rfid.length() != RFID_CHAR_COUNT){
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
}
