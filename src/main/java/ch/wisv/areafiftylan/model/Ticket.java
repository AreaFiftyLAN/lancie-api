package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.exception.TicketNotTransferrableException;
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
    @JsonView(View.Public.class)
    User owner;

    @ManyToOne(cascade = CascadeType.MERGE)
    User transferGoalOwner;

    @Enumerated(EnumType.STRING)
    @JsonView(View.OrderOverview.class)
    TicketType type;

    @JsonView(View.OrderOverview.class)
    String text;

    @JsonView(View.OrderOverview.class)
    boolean pickupService;

    @JsonView(View.OrderOverview.class)
    boolean chMember;

    boolean lockedForTransfer;

    @JsonView(View.OrderOverview.class)
    boolean valid;

    @JsonView(View.OrderOverview.class)
    float price;

    public Ticket(User owner, TicketType type, Boolean pickupService, Boolean chMember) {
        this.owner = owner;
        this.transferGoalOwner = null;
        this.type = type;
        this.text = type.getText();
        this.pickupService = pickupService;
        this.chMember = chMember;
        lockedForTransfer = true;
        this.valid = false;
        key = UUID.randomUUID().toString();

        price = getPrice();
    }

    public Ticket() {
        //JPA Only
    }

    public Long getId() {
        return id;
    }

    public void setUpForTransfer(User u){
        this.setLockedForTransfer(true);
        this.setTransferGoalOwner(u);
    }

    public void finalizeTransfer(){
        if(!this.isLockedForTransfer()) throw new TicketNotTransferrableException(this.key);

        this.setLockedForTransfer(false);

        User newOwner = this.getTransferGoalOwner();
        this.setOwner(newOwner);
        this.setTransferGoalOwner(null);
        this.setNewKey();
    }

    public boolean isLockedForTransfer() {
        return lockedForTransfer;
    }

    private void setLockedForTransfer(boolean lockedForTransfer) {
        this.lockedForTransfer = lockedForTransfer;
    }

    public boolean isPickupService() {
        return pickupService;
    }

    public void setPickupService(boolean pickupService) {
        this.pickupService = pickupService;
        price = getPrice();
    }

    public boolean isChMember() {
        return chMember;
    }

    public User getTransferGoalOwner() {
        return transferGoalOwner;
    }

    private void setTransferGoalOwner(User transferGoalOwner) {
        this.transferGoalOwner = transferGoalOwner;
    }

    public User getOwner() {
        return owner;
    }

    private void setOwner(User owner) {
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

    private void setNewKey(){
        this.key = UUID.randomUUID().toString();
    }

    public String getKey(){
        return this.key;
    }
}
