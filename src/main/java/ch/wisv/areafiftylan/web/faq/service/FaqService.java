package ch.wisv.areafiftylan.web.faq.service;

import ch.wisv.areafiftylan.web.faq.model.FaqPair;

import java.util.Collection;

public interface FaqService {

    Collection<FaqPair> getFaq();

    FaqPair addQuestion(FaqPair faqPair);

    void deleteQuestion(Long id);

    void deleteFaq();
}
