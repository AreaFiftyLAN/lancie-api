package ch.wisv.areafiftylan.utils.mail.template;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("mail/templates")
public class MailTemplateController {

    private final MailTemplateService templateService;

    public MailTemplateController(MailTemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    ResponseEntity<?> addMailTemplate(@RequestBody MailTemplateDTO mailTemplateDTO) {
        templateService.addMailTemplate(mailTemplateDTO.getTemplateName(),
                mailTemplateDTO.getSubject(), mailTemplateDTO.getMessage());
        return createResponseEntity(HttpStatus.OK, "MailTemplate successfully added.");
    }

    @PutMapping("/{id}")
    ResponseEntity<?> updateMailTemplate(@PathVariable Long id, @RequestBody MailTemplateDTO mailTemplateDTO) {
        MailTemplate template = templateService.updateMailTemplate(id, mailTemplateDTO);
        return createResponseEntity(HttpStatus.OK, "MailTemplate successfully updated.", template);
    }

    @GetMapping("/{id}")
    ResponseEntity<?> getMailTemplate(@PathVariable Long id) {
        MailTemplate template = templateService.getMailTemplateById(id);
        return createResponseEntity(HttpStatus.OK, "MailTemplate successfully retrieved.", template);
    }

    @GetMapping
    ResponseEntity<?> getAllMailTemplates() {
        Collection<MailTemplate> templates = templateService.getAllMailTemplates();
        return createResponseEntity(HttpStatus.OK, "MailTemplates successfully retrieved.", templates);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteMailTemplate(@PathVariable Long id) {
        templateService.deleteMailTemplateById(id);
        return createResponseEntity(HttpStatus.OK, "MailTemplate successfully deleted.");
    }
}
