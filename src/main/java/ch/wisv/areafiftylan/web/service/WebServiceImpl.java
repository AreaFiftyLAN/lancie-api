package ch.wisv.areafiftylan.web.service;

import ch.wisv.areafiftylan.web.model.CommitteeMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebServiceImpl implements WebService {


    private CommitteeMemberRepository committeeMemberRepository;

    @Autowired
    public WebServiceImpl(CommitteeMemberRepository committeeMemberRepository) {
        this.committeeMemberRepository = committeeMemberRepository;
    }

    @Override
    public List<CommitteeMember> getAllCommitteeMembers() {
        return committeeMemberRepository.findAll();
    }

    @Override
    public void setAllCommitteeMembers(List<CommitteeMember> committeeMembers) {
        committeeMemberRepository.save(committeeMembers);
    }

    @Override
    public void addCommitteeMember(CommitteeMember committeeMember) {
        committeeMemberRepository.save(committeeMember);
    }

}
