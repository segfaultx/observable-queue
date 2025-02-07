package com.example.queuetest.queue;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledFuture;

@Getter
@Setter
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
class QueueTask {

    Runnable task;
    TaskPriority taskPriority;
    BlockingQueue<TaskStatus> backChannel;
    ScheduledFuture<?> cancelTask;

}
