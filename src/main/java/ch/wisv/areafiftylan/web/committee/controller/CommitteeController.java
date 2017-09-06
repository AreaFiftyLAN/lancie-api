package ch.wisv.areafiftylan.web.committee.controller;

import ch.wisv.areafiftylan.web.committee.model.CommitteeMember;
import ch.wisv.areafiftylan.web.committee.service.CommitteeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@Controller
@RequestMapping("/web/committee")
public class CommitteeController {

    private final CommitteeService committeeService;

    public CommitteeController(CommitteeService committeeService) {
        this.committeeService = committeeService;
    }

    @GetMapping
    ResponseEntity<?> getCommitteeMembers() {
        List<CommitteeMember> members = committeeService.getCommitteeMembers();
        return createResponseEntity(HttpStatus.OK, "Successfully added committee member.", members);
    }

    @PostMapping
    @PreAuthorize("hasRole('COMMITTEE')")
    ResponseEntity<?> addCommitteeMember(CommitteeMember committeeMember) {
        committeeMember = committeeService.addCommitteeMember(committeeMember);
        return createResponseEntity(HttpStatus.OK, "Successfully added committee member.", committeeMember);
    }

    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasRole('COMMITTEE')")
    ResponseEntity<?> deleteCommitteeMember(@PathVariable Long memberId) {
        committeeService.removeCommitteeMember(memberId);
        return createResponseEntity(HttpStatus.OK, "Successfully removed committee member.");
    }

    @DeleteMapping
    @PreAuthorize("hasRole('COMMITTEE')")
    ResponseEntity<?> deleteCommittee() {
        committeeService.deleteCommittee();
        return createResponseEntity(HttpStatus.OK, "Successfully removed committee.");
    }
}
