package ch.wisv.areafiftylan.web.faq.service;

import ch.wisv.areafiftylan.web.faq.model.FaqPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqRepository extends JpaRepository<FaqPair, Long> {
}
