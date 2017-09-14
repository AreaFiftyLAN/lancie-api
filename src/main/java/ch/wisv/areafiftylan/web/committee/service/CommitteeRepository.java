package ch.wisv.areafiftylan.web.committee.service;

import ch.wisv.areafiftylan.web.committee.model.CommitteeMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitteeRepository extends JpaRepository<CommitteeMember, Long> {
}
