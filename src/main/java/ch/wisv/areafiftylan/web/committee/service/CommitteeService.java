package ch.wisv.areafiftylan.web.committee.service;

import ch.wisv.areafiftylan.web.committee.model.CommitteeMember;

import java.util.Collection;

public interface CommitteeService {

    Collection<CommitteeMember> getCommitteeMembers();

    CommitteeMember addCommitteeMember(CommitteeMember member);

    void removeCommitteeMember(Long id);

    void deleteCommittee();
}
