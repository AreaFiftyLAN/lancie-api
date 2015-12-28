package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.model.util.TicketOptions;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.model.view.View;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class Ticket {

    @Id
    @GeneratedValue
    @JsonView(View.OrderOverview.class)
    Long id;

    String key;

    @ManyToOne(cascade = CascadeType.MERGE)
    User owner;

    @ManyToOne(cascade = CascadeType.MERGE)
    User previousOwner;

    @Enumerated(EnumType.STRING)
    @JsonView(View.OrderOverview.class)
    TicketType type;

    @JsonView(View.OrderOverview.class)
    boolean pickupService;

    @JsonView(View.OrderOverview.class)
    boolean chMember;

    boolean lockedForTransfer;

    @JsonView(View.OrderOverview.class)
    boolean valid;

    public Ticket(User owner, TicketType type, Boolean pickupService, Boolean chMember) {
        this.owner = owner;
        this.previousOwner = null;
        this.type = type;
        this.pickupService = pickupService;
        this.chMember = chMember;
        lockedForTransfer = true;
        this.valid = false;
        key = UUID.randomUUID().toString();
    }

    public Ticket() {
        //JPA Only
    }

    public boolean isLockedForTransfer() {
        return lockedForTransfer;
    }

    public void setLockedForTransfer(boolean lockedForTransfer) {
        this.lockedForTransfer = lockedForTransfer;
    }

    public boolean isPickupService() {
        return pickupService;
    }

    public void setPickupService(boolean pickupService) {
        this.pickupService = pickupService;
    }

    public User getPreviousOwner() {
        return previousOwner;
    }

    public void setPreviousOwner(User previousOwner) {
        this.previousOwner = previousOwner;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public TicketType getType() {
        return type;
    }

    public float getPrice() {
        float finalPrice = type.getPrice();

        finalPrice += pickupService ? TicketOptions.PICKUPSERVICE.getPrice() : 0;

        finalPrice += chMember ? TicketOptions.CHMEMBER.getPrice() : 0;

        return finalPrice;
    }

    public boolean hasPickupService() {
        return pickupService;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
