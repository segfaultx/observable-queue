package com.example.queuetest.queue;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledFuture;

@Getter
@Setter
@Builder
class QueueTask {

    Runnable task;
    TaskPriority taskPriority;
    BlockingQueue<TaskStatus> backChannel;
    ScheduledFuture<?> cancelTask;

}
