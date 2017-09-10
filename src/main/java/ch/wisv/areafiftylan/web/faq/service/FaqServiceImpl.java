package ch.wisv.areafiftylan.web.faq.service;

import ch.wisv.areafiftylan.web.faq.model.FaqPair;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class FaqServiceImpl implements FaqService {

    private FaqRepository faqRepository;

    public FaqServiceImpl(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @Override
    public Collection<FaqPair> getFaq() {
        return faqRepository.findAll();
    }

    @Override
    public FaqPair addQuestion(FaqPair faqPair) {
        return faqRepository.save(faqPair);
    }

    @Override
    public void deleteQuestion(Long id) {
        faqRepository.delete(id);
    }

    @Override
    public void deleteFaq() {
        faqRepository.deleteAll();
    }
}
