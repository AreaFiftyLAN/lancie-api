package ch.wisv.areafiftylan.web.committee.service;

import ch.wisv.areafiftylan.web.committee.model.CommitteeMember;

import java.util.Collection;

public interface CommitteeService {

    /**
     * Add a CommitteeMember to the Repository.
     * @param committeeMember the CommitteeMember to add.
     */
    void addCommitteeMember(CommitteeMember committeeMember);

    /**
     * Gets a List of all CommitteeMembers from the Repository.
     * @return a List<CommitteeMember>.
     */
    Collection<CommitteeMember> getCommittee();

    /**
     * Update the CommitteeMember with the given ID, with the given values.
     * @param id The given id.
     * @param committeeMember The new values of the CommitteeMember.
     */
    void updateCommitteeMember(Long id, CommitteeMember committeeMember);

    /**
     * Delete a CommitteeMember from the Repository if it exists.
     * @param id The ID of the CommitteeMember.
     */
    void deleteCommitteeMember(Long id);
}
