package ch.wisv.areafiftylan.web.committee.service;

import ch.wisv.areafiftylan.web.committee.model.CommitteeMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommitteeServiceImpl implements CommitteeService {

    private final CommitteeRepository committeeRepository;

    @Autowired
    public CommitteeServiceImpl(CommitteeRepository committeeRepository) {
        this.committeeRepository = committeeRepository;
    }

    @Override
    public List<CommitteeMember> getCommitteeMembers() {
        return committeeRepository.findAll();
    }

    @Override
    public CommitteeMember addCommitteeMember(CommitteeMember member) {
        return committeeRepository.save(member);
    }

    @Override
    public void removeCommitteeMember(Long id) {
        committeeRepository.delete(id);
    }

    @Override
    public void deleteCommittee() {
        committeeRepository.deleteAll();
    }
}
