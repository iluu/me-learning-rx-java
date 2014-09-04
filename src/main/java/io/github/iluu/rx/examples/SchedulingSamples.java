package io.github.iluu.rx.examples;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SchedulingSamples {

    private static Func1<Integer, Integer> multiplyFunc = new Func1<Integer, Integer>() {
        @Override
        public Integer call(Integer integer) {
            return integer * 2;
        }
    };

    private static final DebugAction generated = new DebugAction("Generated: ");
    private static final DebugAction multiplied = new DebugAction("Multiplied:");
    private static final DebugAction received = new DebugAction("Received:  ");

    public static void main(String[] args) {
        mainThreadSample();
        subscribeOnSample();
        observeOnSample();
    }

    /**
     * All in main thread
     */
    private static void mainThreadSample() {
        Observable.range(1, 10).doOnNext(generated)
                .map(multiplyFunc).doOnNext(multiplied)
                .subscribe(received);
    }

    /**
     * All executed in computation thread
     */
    private static void subscribeOnSample() {
        Observable.range(1, 10).doOnNext(generated)
                .subscribeOn(Schedulers.computation())
                .map(multiplyFunc).doOnNext(multiplied)
                .subscribe(received);
    }

    /**
     * Generation in main thread, computation on computation thread
     */
    private static void observeOnSample() {
        Observable.range(1, 10).doOnNext(generated)
                .observeOn(Schedulers.computation())
                .map(multiplyFunc).doOnNext(multiplied)
                .subscribe(received);
    }

    private static class DebugAction implements Action1<Integer> {
        private final String tag;

        public DebugAction(String tag) {
            this.tag = tag;
        }

        @Override
        public void call(Integer integer) {
            System.out.println(Thread.currentThread() + " " + tag + " " + integer);
        }
    }
}
