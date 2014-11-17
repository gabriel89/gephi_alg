package org.deltaalex.fractaldimension;

import java.util.LinkedList;

/**
 *
 * @author Alexandru Topirceanu
 */
public class ThreadPool extends ThreadGroup {

    private static int NumThreads = 8;
    private boolean isAlive;
    private LinkedList<Runnable> taskQueue;

    /**
     * Creates a new ThreadPool with a specific number of worker threads.
     */
    public ThreadPool(int nThreads) {
        super("ThreadPool");
        setDaemon(true);

        isAlive = true;

        if (nThreads >= 1 && nThreads <= 512) {
            NumThreads = nThreads;
        } else {
            throw new IllegalArgumentException("Unsupported thread amount : " + nThreads);
        }

        taskQueue = new LinkedList<Runnable>();
        for (int i = 0; i < NumThreads; i++) {
            PooledThread thread = new PooledThread(i);
            // thread.setPriority(10);
            thread.start();
        }
    }

    /**
     * Requests a new task to run. This method returns immediately, and the task
     * executes on the next available idle thread in this ThreadPool. <p> Tasks
     * start execution in the order they are received.
     *
     * @param task The task to run. If null, no action is taken.
     * @throws IllegalStateException if this ThreadPool is already closed.
     */
    public synchronized void runTask(Runnable task) {
        if (!isAlive) {
            return;
            // throw new IllegalStateException();
        }
        if (task != null) {
            taskQueue.add(task);
            notify();
        }

    }

    protected synchronized Runnable getTask() throws InterruptedException {
        while (taskQueue.size() == 0) {
            if (!isAlive) {
                return null;
            }
            wait();
        }
        return taskQueue.removeFirst();
    }

    /**
     * Closes this ThreadPool and returns immediately. All threads are stopped,
     * and any waiting tasks are not executed. Once a ThreadPool is closed, no
     * more tasks can be run on this ThreadPool.
     */
    public synchronized void close() {
        if (isAlive) {
            isAlive = false;
            taskQueue.clear();
            interrupt();
        }
    }

    /**
     * Closes this ThreadPool and waits for all running threads to finish. Any
     * waiting tasks are executed.
     */
    public void join() {
        // notify all waiting threads that this ThreadPool is no
        // longer alive
        synchronized (this) {
            isAlive = false;
            notifyAll();
        }

        // wait for all threads to finish
        Thread[] threads = new Thread[activeCount()];
        int count = enumerate(threads);
        for (int i = 0; i < count; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {/*nothing*/

            }
        }
    }

    /**
     * A PooledThread is a Thread in a ThreadPool group, designed to run tasks
     * (Runnables).
     */
    private class PooledThread extends Thread {

        public PooledThread(int id) {
            super(ThreadPool.this, "Thread-" + id);
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                // get a task from the queue to run
                Runnable task = null;
                try {
                    task = getTask();
                } catch (InterruptedException ex) {/*nothing*/

                }

                // if getTask() returned null or was interrupted, close this
                // thread by returning null
                if (task == null) {
                    return;
                }

                // run the task, and eat any exceptions it throws
                try {
                    task.run();
                } catch (Throwable t) {
                    uncaughtException(this, t);
                }
            }
        }
    }
}
