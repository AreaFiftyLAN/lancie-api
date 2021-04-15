package ch.wisv.areafiftylan.utils.setup;

import ch.wisv.areafiftylan.security.token.SetupToken;
import ch.wisv.areafiftylan.users.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class SetupLog {

    public SetupLog(Integer year, String initiator) {
        this.year = year;
        this.initiator = initiator;
        this.setupDate = LocalDateTime.now();
    }

    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime setupDate;

    @Column(unique = true)
    private Integer year;

    private String initiator;

    @ManyToOne
    private SetupToken token;
}
