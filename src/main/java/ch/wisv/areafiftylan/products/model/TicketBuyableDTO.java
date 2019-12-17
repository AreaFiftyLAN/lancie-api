package ch.wisv.areafiftylan.products.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class TicketBuyableDTO {

    @NotNull
    private boolean buyable;
}
