package ch.wisv.areafiftylan.utils.mail.template.injections;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailTemplateInjectionsRepository extends JpaRepository<MailTemplateInjections, Long> {

    Optional<MailTemplateInjections> findOneByTemplateName(String templateName);
}
