package ch.wisv.areafiftylan.web.service;

import ch.wisv.areafiftylan.web.model.CommitteeMember;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by martijn on 3-1-16.
 */
@Service
public class CommitteeMemberServiceImpl {
    public Collection<CommitteeMember> getAllCommitteeMembers() {
        return getDummyCommitteeMembers();
    }

    public Collection<CommitteeMember> getDummyCommitteeMembers() {
        Collection<CommitteeMember> committeeMembers = new ArrayList<>();
        committeeMembers.add(new CommitteeMember("Sille Kamoen", "Chairman", "people"));
        committeeMembers.add(new CommitteeMember("Rebecca Glans", "Secretary", "women"));
        committeeMembers.add(new CommitteeMember("Sven Popping", "Treasurer", "money"));
        return committeeMembers;
    }
}
