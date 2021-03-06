package io.github.iluu.rx.examples;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import rx.Observer;
import rx.subjects.ReplaySubject;

import java.util.Arrays;

import static io.github.iluu.rx.examples.helpers.TestFunctions.verifyNotificationSequence;

@RunWith(MockitoJUnitRunner.class)
public class ReplaySubjectTest {

    @Mock
    private Observer<Integer> testObserver;

    @Test
    public void emitsEveryItemEverEmittedToSubscriber() {
        ReplaySubject<Integer> subject = ReplaySubject.create();

        subject.onNext(1);
        subject.subscribe(testObserver);
        subject.onNext(2);
        subject.onCompleted();

        verifyNotificationSequence(testObserver, Arrays.asList(1, 2));
    }

    @Test
    public void emitsOnlyCertainNumberOfCachetItems() {
        ReplaySubject<Integer> subject = ReplaySubject.createWithSize(2);

        subject.onNext(1);
        subject.onNext(2);
        subject.onNext(3);

        subject.subscribe(testObserver);
        subject.onCompleted();

        verifyNotificationSequence(testObserver, Arrays.asList(2, 3));
    }
}
