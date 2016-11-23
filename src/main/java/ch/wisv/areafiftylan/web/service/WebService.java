package ch.wisv.areafiftylan.web.service;

import ch.wisv.areafiftylan.web.model.CommitteeMember;

import java.util.Collection;

public interface WebService {

    /**
     * Deletes all existing CommitteeMembers from the Repository,
     * and adds the List of CommitteeMembers to the Repository.
     * @param committeeMembers the List<CommitteeMember> to add.
     */
    void setCommittee(Collection<CommitteeMember> committeeMembers);

    /**
     * Gets a List of all CommitteeMembers from the Repository.
     * @return a List<CommitteeMember>
     */
    Collection<CommitteeMember> getCommittee();

    /**
     * Deletes all existing CommitteeMembers from the Repository.
     */
    void deleteCommittee();

    /**
     * Add a CommitteeMember to the Repository.
     * @param committeeMember the CommitteeMember to add.
     */
    void addCommitteeMember(Long id, CommitteeMember committeeMember);

    /**
     * Get a CommitteeMember from the Repository.
     * @param id The ID of the CommitteeMember.
     * @return The CommitteeMember.
     */
    CommitteeMember getCommitteeMember(Long id);

    /**
     * Delete a CommitteeMember from the Repository if it exists.
     * @param id The ID of the CommitteeMember.
     */
    void deleteCommitteeMember(Long id);

}
