package ch.wisv.areafiftylan.web.faq.controller;

import ch.wisv.areafiftylan.web.faq.model.FaqPair;
import ch.wisv.areafiftylan.web.faq.service.FaqService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@Controller
@RequestMapping("/web/faq")
public class FaqController {

    private FaqService faqService;

    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping
    ResponseEntity<?> getFaq() {
        Collection<FaqPair> faq = faqService.getFaq();
        return createResponseEntity(HttpStatus.OK, "FAQ retrieved.", faq);
    }

    @PostMapping
    @PreAuthorize("hasRole('COMMITTEE')")
    ResponseEntity<?> addQuestion(@RequestBody FaqPair faqPair) {
        faqPair = faqService.addQuestion(faqPair);
        return createResponseEntity(HttpStatus.CREATED, "FaqPair added.", faqPair);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COMMITTEE')")
    ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        faqService.deleteQuestion(id);
        return createResponseEntity(HttpStatus.OK, "FaqPair deleted.");
    }

    @DeleteMapping
    @PreAuthorize("hasRole('COMMITTEE')")
    ResponseEntity<?> deleteFaq() {
        faqService.deleteFaq();
        return createResponseEntity(HttpStatus.OK, "Faq deleted.");
    }
}
