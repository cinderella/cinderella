package io.cinderella.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Shane Witbeck
 * @since 10/8/12
 */
@Controller
public class HomeController {

    @RequestMapping(value = "/", produces = "text/plain")
    @ResponseBody
    public String handleRoot() {
       return "OK!";
    }
}
