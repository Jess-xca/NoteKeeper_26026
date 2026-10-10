package com.notekeeper.notekeeper.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProtectedResourceProbe {

    @GetMapping("/api/probe")
    public String probe() {
        return "ok";
    }
}
