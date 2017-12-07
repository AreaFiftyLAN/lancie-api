package ch.wisv.areafiftylan.web.banner.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Date;

@Entity
@Data
@NoArgsConstructor
public class Banner {

    @Id
    @GeneratedValue
    private Long id;

    private String text;

    private Date startDate;

    private Date endDate;
}
