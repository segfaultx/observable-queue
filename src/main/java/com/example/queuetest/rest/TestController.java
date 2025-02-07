package com.example.queuetest.rest;

import com.example.queuetest.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TestController {

    private final TaskService taskService;

    @GetMapping("/prio")
    public void prioTask() {
        taskService.prioTask();
    }

    @GetMapping("/prio-blocking")
    public void prioBlockingTask() {
        taskService.prioTaskBlocking();
    }

    @GetMapping("/regular")
    public void regularTask() {
        taskService.regularTask();
    }

    @GetMapping("/stopWorker")
    public void stopWorker() {
        taskService.stopWorker();
    }

    @GetMapping("/startWorker")
    public void startWorker() {
        taskService.startWorker();
    }


}
