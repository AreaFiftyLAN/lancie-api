package ch.wisv.areafiftylan.web.service;

import ch.wisv.areafiftylan.web.model.Sponsor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class SponsorServiceImpl {

    public Collection<Sponsor> getAllSponsors() {
        return getDummySponspors();
    }

    private Collection<Sponsor> getDummySponspors() {
        Collection<Sponsor> sponspors = new ArrayList<>();
        sponspors.add(new Sponsor("Logitech", "path/to/image", "logitech.com"));
        sponspors.add(new Sponsor("Azerty", "path/to/image", "schralebazen.com"));
        sponspors.add(new Sponsor("Ordina", "path/to/image", "ordina.com"));
        sponspors.add(new Sponsor("Plantronics", "path/to/image", "koptelefoons.com"));
        sponspors.add(new Sponsor("OCZ", "path/to/image", "ocz.com"));
        return sponspors;
    }
}
