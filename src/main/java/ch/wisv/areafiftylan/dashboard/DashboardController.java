package ch.wisv.areafiftylan.dashboard;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

@Controller
@RequestMapping(value = "/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    @RequestMapping(method = RequestMethod.GET)
    public String getIndexPage(){
        return "redirect:dashboard/overview";
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String getUsersPage(){
        return "admin/users";
    }
    @RequestMapping(value = "/overview", method = RequestMethod.GET)
    public String getOverviewPage(){
        return "admin/overview";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView loginRedirect(AccessDeniedException ex) {
        return new ModelAndView("admin/login");
    }
}
