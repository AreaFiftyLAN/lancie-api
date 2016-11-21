package ch.wisv.areafiftylan.web.service;

import ch.wisv.areafiftylan.web.model.CommitteeMember;

import java.util.Collection;

public interface WebService {

    /**
     * Gets a List of all CommitteeMembers from the Repository.
     * @return a List<CommitteeMember>
     */
    Collection<CommitteeMember> getAllCommitteeMembers();

    /**
     * Deletes all existing CommitteeMembers from the Repository,
     * and adds the List of CommitteeMembers to the Repository.
     * @param committeeMembers the List<CommitteeMember> to add.
     */
    void setAllCommitteeMembers(Collection<CommitteeMember> committeeMembers);

    /**
     * Add a committeemember to the existing Repository.
     * @param committeeMember the CommitteeMember to add.
     */
    void addCommitteeMember(CommitteeMember committeeMember);
}
