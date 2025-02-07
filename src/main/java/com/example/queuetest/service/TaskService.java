package com.example.queuetest.service;

import com.example.queuetest.queue.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {

    private final QueueService queueService;

    public void prioTask() {
        queueService.addPriorityTaskToQueue(this::task);
    }

    public void prioTaskBlocking() {
        var channel = queueService.addPriorityTaskToQueue(this::task);

        try {
            var status = channel.take();
            log.info("Task completed: {}", status);
        } catch (InterruptedException e) {
            log.error("Error when trying to wait for data from channel", e);
        }
    }

    public void regularTask() {
        queueService.addRegularTaskToQueue(this::task);
    }

    public void stopWorker() {
        queueService.stopWorkerThread();
    }

    public void startWorker() {
        queueService.startWorkerThread();
    }


    private void task() {
        try {
            log.info("Task started");
            Thread.sleep(TimeUnit.MILLISECONDS.convert(Duration.ofSeconds(3L)));
            log.info("Task completed");
        } catch (InterruptedException e) {
            log.error("task interrupted", e);
        }
    }
}
