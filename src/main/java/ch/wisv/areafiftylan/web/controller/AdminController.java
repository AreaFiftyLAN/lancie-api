package ch.wisv.areafiftylan.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by sille on 7-11-15.
 */
@Controller
@RequestMapping(value = "/admin")
public class AdminController {

    @RequestMapping(method = RequestMethod.GET)
    public String getAdminOverview() {
        return "dashboard/pages/overview";
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String getUserPage() {
        return "dashboard/pages/users";
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String getTestPage() {
//        ModelAndView r = new ModelAndView("mailTemplate");
//        return r;
        return "dashboard/material-layout";
    }
}