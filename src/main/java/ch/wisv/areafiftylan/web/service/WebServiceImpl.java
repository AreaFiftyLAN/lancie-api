package ch.wisv.areafiftylan.web.service;

import ch.wisv.areafiftylan.web.model.CommitteeMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class WebServiceImpl implements WebService {


    private CommitteeMemberRepository committeeMemberRepository;

    @Autowired
    public WebServiceImpl(CommitteeMemberRepository committeeMemberRepository) {
        this.committeeMemberRepository = committeeMemberRepository;
    }

    @Override
    public void setCommittee(Collection<CommitteeMember> committeeMembers) {
        committeeMemberRepository.deleteAll();
        committeeMemberRepository.save(committeeMembers);
    }

    @Override
    public List<CommitteeMember> getCommittee() {
        return committeeMemberRepository.findAll();
    }

    @Override
    public void deleteCommittee() {
        committeeMemberRepository.deleteAll();
    }

    @Override
    public void addCommitteeMember(Long id, CommitteeMember committeeMember) {
        committeeMember.setId(id);
        committeeMemberRepository.delete(id);
        committeeMemberRepository.save(committeeMember);
    }

    @Override
    public CommitteeMember getCommitteeMember(Long id) {
        return committeeMemberRepository.findOne(id);
    }

    @Override
    public void deleteCommitteeMember(Long id) {
        committeeMemberRepository.delete(id);
    }

}
