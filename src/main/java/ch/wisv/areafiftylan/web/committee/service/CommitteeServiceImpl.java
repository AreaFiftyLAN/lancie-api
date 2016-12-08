package ch.wisv.areafiftylan.web.committee.service;

import ch.wisv.areafiftylan.web.committee.model.CommitteeMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommitteeServiceImpl implements CommitteeService {


    private CommitteeMemberRepository committeeMemberRepository;

    @Autowired
    public CommitteeServiceImpl(CommitteeMemberRepository committeeMemberRepository) {
        this.committeeMemberRepository = committeeMemberRepository;
    }

    @Override
    public void addCommitteeMember(CommitteeMember committeeMember) {
        committeeMemberRepository.saveAndFlush(committeeMember);
    }

    @Override
    public List<CommitteeMember> getCommittee() {
        return committeeMemberRepository.findAll();
    }

    @Override
    public void updateCommitteeMember(Long id, CommitteeMember committeeMember) {
        CommitteeMember oldCommitteeMember = committeeMemberRepository.getOne(id);
        oldCommitteeMember.setOrder(committeeMember.getOrder());
        oldCommitteeMember.setName(committeeMember.getName());
        oldCommitteeMember.setFunction(committeeMember.getFunction());
        oldCommitteeMember.setIcon(committeeMember.getIcon());
        committeeMemberRepository.saveAndFlush(committeeMember);
    }

    @Override
    public void deleteCommitteeMember(Long id) {
        committeeMemberRepository.delete(id);
    }
}
