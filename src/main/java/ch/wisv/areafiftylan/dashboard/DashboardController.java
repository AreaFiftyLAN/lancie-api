package ch.wisv.areafiftylan.dashboard;

import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;

@Controller
@RequestMapping(value = "/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    UserService userService;

    @Autowired
    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String getIndexPage() {
        return "redirect:dashboard/overview";
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String getUsersPage(Model model) {
        Collection<User> allUsers = userService.getAllUsers();
        model.addAttribute("users", allUsers);
        return "admin/users";
    }

    @RequestMapping(value = "/overview", method = RequestMethod.GET)
    public String getOverviewPage() {
        return "admin/overview";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView loginRedirect(AccessDeniedException ex) {
        return new ModelAndView("admin/login");
    }
}
