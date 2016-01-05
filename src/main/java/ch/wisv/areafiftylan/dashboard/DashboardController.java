package ch.wisv.areafiftylan.dashboard;

import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.OrderService;
import ch.wisv.areafiftylan.service.TeamService;
import ch.wisv.areafiftylan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    OrderService orderService;

    TeamService teamService;

    @Autowired
    public DashboardController(UserService userService, OrderService orderService, TeamService teamService) {
        this.userService = userService;
        this.orderService = orderService;
        this.teamService = teamService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String getIndexPage() {
        return "redirect:dashboard/overview";
    }

    @RequestMapping(value = "/overview", method = RequestMethod.GET)
    public String getOverviewPage(Model model, Authentication auth) {
        model.addAttribute("usercount", userService.getAllUsers().size());
        model.addAttribute("ordercount", orderService.getAllOrders().size());
        model.addAttribute("teamcount", teamService.getAllTeams().size());

        return "admin/overview";
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String getUsersPage(Model model) {
        Collection<User> allUsers = userService.getAllUsers();
        model.addAttribute("users", allUsers);
        return "admin/users";
    }

    @RequestMapping(value = "/teams", method = RequestMethod.GET)
    public String getTeamsPage(Model model) {
        Collection<Team> allTeams = teamService.getAllTeams();
        model.addAttribute("teams", allTeams);
        return "admin/teams";
    }

    @RequestMapping(value = "/orders", method = RequestMethod.GET)
    public String getOrdersPage(Model model){
        model.addAttribute("orders", orderService.getAllOrders());

        return "admin/orders";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView loginRedirect(AccessDeniedException ex) {
        return new ModelAndView("admin/login");
    }
}
