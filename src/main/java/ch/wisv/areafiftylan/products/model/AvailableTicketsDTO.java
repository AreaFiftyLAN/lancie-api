package ch.wisv.areafiftylan.products.model;

import ch.wisv.areafiftylan.utils.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@AllArgsConstructor
public class AvailableTicketsDTO {

    @JsonView(View.Public.class)
    @NotNull
    private final Collection<TicketInformationResponse> ticketTypes;

    @JsonView(View.Public.class)
    @NotNull
    private final Integer ticketLimit;
}
