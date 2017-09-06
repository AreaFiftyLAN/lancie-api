package ch.wisv.areafiftylan.web.committee.service;

import ch.wisv.areafiftylan.web.committee.model.CommitteeMember;

import java.util.List;

public interface CommitteeService {

    List<CommitteeMember> getCommitteeMembers();

    CommitteeMember addCommitteeMember(CommitteeMember member);

    void removeCommitteeMember(Long id);

    void deleteCommittee();
}
