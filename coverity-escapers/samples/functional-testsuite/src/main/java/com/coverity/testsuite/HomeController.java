package com.coverity.testsuite;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController {

    // By default we go to the EL tests.
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        return "test-el";
    }

    @RequestMapping(value = "/el", method = RequestMethod.GET)
    public String testEL(Model model) {
        return "test-el";
    }
    
    @RequestMapping(value = "/scriptlet", method = RequestMethod.GET)
    public String testJSP(Model model) {
        return "test-jsp";
    }
}
 