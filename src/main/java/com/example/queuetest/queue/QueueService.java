package com.example.queuetest.queue;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
    public void setup() {
        log.info("Starting worker thread for queue processing");
        workerThread = Thread.ofVirtual().start(() -> {
            try {
                processQueue();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("Successfully started worker thread for queue processing");
    }

    public void stopWorkerThread() {
        workerThread.interrupt();
    }

    public void startWorkerThread() {
        setup();
    }


    public BlockingQueue<TaskStatus> addPriorityTaskToQueue(Runnable task) {
        var queueTask = createQueueTask(task, TaskPriority.PRIORITY, TimeUnit.SECONDS);
        queue.add(queueTask);

        return queueTask.getBackChannel();
    }

    public BlockingQueue<TaskStatus> addRegularTaskToQueue(Runnable task) {
        var queueTask = createQueueTask(task, TaskPriority.REGULAR, TimeUnit.MINUTES);
        queue.add(queueTask);

        return queueTask.getBackChannel();
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
        log.info("Cancelling task {}", task);
        if (queue.remove(task)) {
            task.getBackChannel().add(TaskStatus.TIMEOUT);
        }
    }

    private void processQueue() throws InterruptedException {
        while (!Thread.interrupted()) {
            var queueTask = queue.take();
            log.info("Processing queue task: {}", queueTask);

            if (queueTask.getTaskPriority() == TaskPriority.REGULAR && queueContainsPriorityTasks()) {
                log.info("Put regular task back in queue due to priority task taking precedence");
                queue.put(queueTask);
                continue;
            }

            queueTask.getCancelTask().cancel(true);

            try {
                queueTask.getTask().run();
            } catch (Exception e) {
                log.error("Error when running task", e);
                queueTask.getBackChannel().add(TaskStatus.ERROR);
                continue;
            }

            queueTask.getBackChannel().add(TaskStatus.OK);
        }
    }

    private boolean queueContainsPriorityTasks() {
        return queue.stream()
                .map(QueueTask::getTaskPriority)
                .anyMatch(taskPriority -> taskPriority == TaskPriority.PRIORITY);
    }
}
