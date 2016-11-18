package ch.wisv.areafiftylan.web.service;

import ch.wisv.areafiftylan.web.model.CommitteeMember;

import java.util.List;

public interface WebService {

    List<CommitteeMember> getAllCommitteeMembers();

    void setAllCommitteeMembers(List<CommitteeMember> committeeMembers);

    void addCommitteeMember(CommitteeMember committeeMember);
}
