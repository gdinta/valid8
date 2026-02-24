package com.valid8.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the HTML pages via Thymeleaf.
 *
 * / → login portal selector
 * /student → student parking dashboard (lot cards + map)
 * /rupd → RUPD enforcement dashboard (violations + unregistered)
 */
@Controller
public class PageController {

    /** Landing page — portal selector (Student / RUPD login) */
    @GetMapping("/")
    public String login() {
        return "login";
    }

    /** Student dashboard: lot cards + availability key + map */
    @GetMapping("/student")
    public String student() {
        return "student";
    }

    /** RUPD dashboard: violations + unregistered combined */
    @GetMapping("/rupd")
    public String rupd() {
        return "rupd";
    }

    // ── Legacy routes ──────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/violations")
    public String violations() {
        return "violations";
    }

    @GetMapping("/unregistered")
    public String unregistered() {
        return "unregistered";
    }

    @GetMapping("/map")
    public String map() {
        return "map";
    }
}
