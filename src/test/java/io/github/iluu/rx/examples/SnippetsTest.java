package io.github.iluu.rx.examples;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.functions.Func2;

import java.util.Arrays;

import static io.github.iluu.rx.examples.helpers.TestFunctions.verifyNotificationSequence;

@RunWith(MockitoJUnitRunner.class)
public class SnippetsTest {

    @Mock private Observer<Integer> observer;

    private final Func1<String, Integer> MAP_EACH_OCCURRENCE_TO_ONE = new Func1<String, Integer>() {
        @Override
        public Integer call(String nextValue) {
            return 1;
        }
    };
    private final Func2<Integer, Integer, Integer> SUM_OF_OCCURRENCES = new Func2<Integer, Integer, Integer>() {

        @Override
        public Integer call(Integer accumulator, Integer value) {
            return accumulator + value;
        }
    };

    @Test
    public void mapAndScanAndLastToCountNumberOfEvents() {
        Observable<String> streamOfSomeEvents = Observable.from(Arrays.asList("a", "b", "c", "d"));
        streamOfSomeEvents
                .map(MAP_EACH_OCCURRENCE_TO_ONE)
                .scan(SUM_OF_OCCURRENCES)
                .last()
                .subscribe(observer);

        verifyNotificationSequence(observer, Arrays.asList(4));
    }
}
