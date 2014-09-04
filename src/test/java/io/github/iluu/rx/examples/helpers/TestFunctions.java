package io.github.iluu.rx.examples.helpers;

import rx.Observer;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class TestFunctions {

    /**
     * Function that returns value passed as argument
     */
    public static <T, R> Func1<T, R> just1(final R value) {
        return new Func1<T, R>() {

            @Override
            public R call(T ignored) {
                return value;
            }
        };
    }

    /**
     * Function returns value passed as argument
     */
    public static <R> Func0<R> just0(final R value) {
        return new Func0<R>() {
            @Override
            public R call() {
                return value;
            }
        };
    }

    /**
     * Verifies that mock observer gets notifications for each value inside expected values
     * and finally an onCompleted notification.
     */
    public static <R> void verifyNotificationSequence(Observer<R> mockObserver, List<R> expectedValues) {
        for (R item : expectedValues) {
            verify(mockObserver).onNext(item);
        }
        verify(mockObserver).onCompleted();
        verifyNoMoreInteractions(mockObserver);
    }

    /**
     * Emits all values and completes the subject in a new thread.
     */
    public static void runInNewThread(final PublishSubject<Integer> subject, final List<Integer> values) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                threadSleep();
                for (Integer value : values) {
                    subject.onNext(value);
                }
                subject.onCompleted();
            }
        }).start();
    }

    /**
     * Emits all values and completes the subject in a new thread.
     */
    public static void runInNewThreadEmitNull(final PublishSubject<Integer> subject) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                threadSleep();
                subject.onNext(null);
                subject.onCompleted();
            }
        }).start();
    }


    private static void threadSleep() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {
        }
    }


}
