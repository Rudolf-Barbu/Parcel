package org.bsoftware.parcel.domain.components;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bsoftware.parcel.domain.callbacks.DataLoadingCallback;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.runnables.BruteForceRunnable;
import org.bsoftware.parcel.domain.runnables.DataLoadingRunnable;

import java.io.File;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * ThreadContainer is a class that contains thread-related methods
 *
 * @author Rudolf Barbu
 * @version 1.0.2
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThreadContainer
{
    /**
     * Defines container for data processing threads
     */
    private static final Map<DataType, Thread> DATA_LOADING_THREAD_MAP = new EnumMap<>(DataType.class);

    /**
     * Defines container for brute-force threads
     */
    private static final Thread[] BRUTE_FORCE_THREAD_ARRAY = new Thread[120];

    /**
     * Starts corresponding, loading thread
     *
     * @param file file to load
     * @param dataType type of data, file contain
     * @param dataLoadingCallback callback
     */
    public static void startLoadingThread(final File file, final DataType dataType, final DataLoadingCallback dataLoadingCallback)
    {
        final DataLoadingRunnable dataLoadingRunnable = new DataLoadingRunnable(file, dataType, dataLoadingCallback);
        final Thread thread = createDaemonThread(dataLoadingRunnable, String.format(WorkType.LOADING.getThreadNamePattern(), dataType.getDataTypeNameInPlural()));

        DATA_LOADING_THREAD_MAP.put(dataType, thread);
        thread.start();
    }

    /**
     * Starts bruteforce threads
     *
     * @param bruteForceRunnable runnable to be passed to thread
     */
    public static void startBruteForceThreads(final BruteForceRunnable bruteForceRunnable)
    {
        for (int index = 0; index < BRUTE_FORCE_THREAD_ARRAY.length; index++)
        {
            final Thread thread = createDaemonThread(bruteForceRunnable, String.format(WorkType.BRUTEFORCE.getThreadNamePattern(), index));

            BRUTE_FORCE_THREAD_ARRAY[index] = thread;
            thread.start();
        }
    }

    /**
     * Checks if work is not interrupted
     *
     * @param workTypes work-type, which may present
     * @return boolean depends on work status
     */
    public static boolean isWorkStillExecuting(final WorkType... workTypes)
    {
        if ((workTypes.length == 0) || (workTypes.length > 2))
        {
            throw new IllegalArgumentException("Work-types length is out on ranges");
        }
        else if (Arrays.stream(workTypes).distinct().count() < workTypes.length)
        {
            throw new IllegalArgumentException("You can't pass the same work-type several times");
        }

        for (final WorkType workType : workTypes)
        {
            final Stream<Thread> threadStream = (workType == WorkType.LOADING) ? DATA_LOADING_THREAD_MAP.values().stream() : Arrays.stream(BRUTE_FORCE_THREAD_ARRAY);

            if (threadStream.anyMatch(ThreadContainer::isThreadStillExecuting))
            {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    /**
     * Interrupts bruteforce threads
     */
    public static void interruptBruteforceThreads()
    {
        Arrays.stream(BRUTE_FORCE_THREAD_ARRAY).filter(ThreadContainer::isThreadStillExecuting).forEach(Thread::interrupt);
    }

    /**
     * Checks if only one running brute-force thread left
     *
     * @return true, in only one thread left
     */
    public static boolean isTheLastBruteForceThreadLeft()
    {
        return (Arrays.stream(BRUTE_FORCE_THREAD_ARRAY).filter(ThreadContainer::isThreadStillExecuting).count() == 1L);
    }

    /**
     * Checks, if thread still executing
     *
     * @param thread target thread
     * @return true, if thread is not interrupted
     */
    private static boolean isThreadStillExecuting(final Thread thread)
    {
        return ((thread != null) && thread.isAlive());
    }

    /**
     * Creates and returns new daemon thread, with custom name
     *
     * @param runnable separated runnable logic
     * @param threadName string, which will represent thread name
     * @return newly created, daemon thread
     */
    private static Thread createDaemonThread(final Runnable runnable, final String threadName)
    {
        final Thread thread = new Thread(runnable);

        thread.setDaemon(Boolean.TRUE);
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.setName(threadName);

        return thread;
    }

    /**
     * Enum with all supported wo
     */
    @Getter
    @RequiredArgsConstructor
    public enum WorkType
    {
        LOADING("Thread of %s loading"), BRUTEFORCE("Brute-force thread #%d");

        /**
         * Defines thread name pattern
         */
        private final String threadNamePattern;
    }
}