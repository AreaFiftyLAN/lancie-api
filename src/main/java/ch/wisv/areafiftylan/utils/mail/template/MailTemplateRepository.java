package ch.wisv.areafiftylan.utils.mail.template;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailTemplateRepository extends JpaRepository<MailTemplate, Long>{

    Optional<MailTemplate> findOneByTemplateName(String templateName);
}
