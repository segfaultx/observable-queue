package com.example.queuetest.queue;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Slf4j
@Service
@Scope("singleton")
@RequiredArgsConstructor
public class QueueService {

    private final BlockingQueue<QueueTask> queue = new ArrayBlockingQueue<>(50);
    private final ScheduledExecutorService timeoutTaskPool = Executors.newScheduledThreadPool(50);
    private Thread workerThread;

    @PostConstruct
    @SneakyThrows
    public void setup() {
        log.info("Starting worker thread for queue processing");
        workerThread = Thread.ofVirtual().start(this::processQueue);
        log.info("Successfully started worker thread for queue processing");
    }

    public void stopWorkerThread() {
        workerThread.interrupt();
    }

    public void startWorkerThread() {
        workerThread.start();
    }


    public BlockingQueue<TaskStatus> addPriorityTaskToQueue(Runnable task) {
        var queueTask = createQueueTask(task, TaskPriority.PRIORITY, TimeUnit.SECONDS);
        queue.add(queueTask);

        return queueTask.backChannel;
    }

    public BlockingQueue<TaskStatus> addRegularTaskToQueue(Runnable task) {
        var queueTask = createQueueTask(task, TaskPriority.REGULAR, TimeUnit.MINUTES);
        queue.add(queueTask);

        return queueTask.backChannel;
    }

    private QueueTask createQueueTask(Runnable task, TaskPriority priority, TimeUnit timeoutUnit) {
        var queueTask = QueueTask.builder()
                .task(task)
                .taskPriority(priority)
                .backChannel(new ArrayBlockingQueue<>(1))
                .build();

        var cancelTask = timeoutTaskPool.schedule(() -> cancelTask(queueTask), 20, timeoutUnit);
        queueTask.setCancelTask(cancelTask);

        return queueTask;
    }

    private void cancelTask(QueueTask task) {
        if (queue.remove(task)) {
            task.backChannel.add(TaskStatus.TIMEOUT);
        }
    }

    private void processQueue() throws InterruptedException {
        while (!Thread.interrupted()) {
            var queueTask = queue.take();

            if (queueTask.getTaskPriority() == TaskPriority.REGULAR && queueContainsPriorityTasks()) {
                queue.put(queueTask);
                continue;
            }

            queueTask.cancelTask.cancel(true);

            try {
                queueTask.task.run();
            } catch (Exception e) {
                log.error("Error when running task", e);
                queueTask.backChannel.add(TaskStatus.ERROR);
            }

            queueTask.backChannel.add(TaskStatus.OK);
        }
    }

    private boolean queueContainsPriorityTasks() {
        return queue.stream()
                .map(QueueTask::getTaskPriority)
                .anyMatch(taskPriority -> taskPriority == TaskPriority.PRIORITY);
    }
}
