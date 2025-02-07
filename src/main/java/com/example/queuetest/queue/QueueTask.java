package com.example.queuetest.queue;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledFuture;

@Builder
@ToString
class QueueTask {

    Runnable task;
    TaskPriority taskPriority;
    BlockingQueue<TaskStatus> backChannel;
    ScheduledFuture<?> cancelTask;

}
