package com.example.queuetest.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task")
public class TestController {

    @GetMapping("/prio")
    public void prioTask() {

    }



    @GetMapping("/regular")
    public void regularTask() {

    }

    @GetMapping("/stopWorker")
    public void stopWorker() {

    }

    @GetMapping("/startWorker")
    public void startWorker() {

    }



}
