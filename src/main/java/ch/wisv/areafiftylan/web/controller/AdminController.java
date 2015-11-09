package ch.wisv.areafiftylan.web.controller;

import ch.wisv.areafiftylan.dto.UserDTO;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;

/**
 * Created by sille on 7-11-15.
 */
@Controller
@RequestMapping(value = "/admin")
public class AdminController {

    @Autowired
    UserService userService;

    @RequestMapping(method = RequestMethod.GET)
    public String getAdminOverview() {
        return "dashboard/pages/overview";
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String getUserPage(Model model) {
        Collection<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("UserDTO", new UserDTO());
        return "dashboard/pages/users";
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String getTestPage() {
        return "dashboard/material-layout";
    }
}