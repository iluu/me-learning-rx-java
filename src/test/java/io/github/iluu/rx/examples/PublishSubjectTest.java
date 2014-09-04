package io.github.iluu.rx.examples;

import org.junit.Test;
import rx.Observer;
import rx.subjects.PublishSubject;

import static org.mockito.Mockito.*;

public class PublishSubjectTest {

    @Test
    @SuppressWarnings("unchecked")
    public void emitsOnlyNewItemsAfterObserverSubscribes() {
        Observer<Integer> observer = mock(Observer.class);
        PublishSubject<Integer> subject = PublishSubject.create();

        subject.onNext(1);

        subject.subscribe(observer);
        subject.onNext(2);
        subject.onCompleted();

        verify(observer).onNext(2);
        verify(observer).onCompleted();
        verifyNoMoreInteractions(observer);
    }

}
