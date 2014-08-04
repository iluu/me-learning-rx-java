import helpers.MockitoTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import rx.Notification;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.BlockingObservable;
import rx.subjects.PublishSubject;

import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;

import static com.pivotallabs.greatexpectations.Expect.expect;
import static helpers.TestFunctions.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(MockitoTestRunner.class)
public class ObservableTest {

    @Mock
    Observer<Integer> testObserver;

    @Test
    @SuppressWarnings("unchecked")
    public void allReturnsTrueWhenAllItemsSatisfyPredicate() {
        Observable<Integer> observable = Observable.from(2, 4, 6, 8);
        Observer<Boolean> observer = mock(Observer.class);

        observable.all(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return integer % 2 == 0;
            }
        }).subscribe(observer);

        verify(observer).onNext(true);
        verify(observer).onCompleted();
        verifyNoMoreInteractions(observer);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void allReturnsFalseWhenOneItemDoesNotSatisfyPredicate() {
        Observable<Integer> observable = Observable.from(2, 4, 5, 8);
        Observer<Boolean> observer = mock(Observer.class);

        observable.all(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return integer % 2 == 0;
            }
        }).subscribe(observer);

        verify(observer).onNext(false);
        verify(observer).onCompleted();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void filterReturnsOnlyValuesThatSatisfyGivenCondition() {
        Observable<Integer> observable = Observable.from(1, 2, 3, 4);

        observable.filter(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return integer % 2 == 0;
            }
        }).subscribe(testObserver);

        verifyNotificationSequence(testObserver, Arrays.asList(2, 4));
    }

    @Test
    public void lastReturnsLastElementEmitted() {
        Observable<Integer> observable = Observable.from(1, 2, 3);

        observable.last().subscribe(testObserver);

        verifyNotificationSequence(testObserver, Arrays.asList(3));
    }

    @Test
    public void lastThrowsIndexOutOfBoundsWhenObservableDoesNotEmit() {
        PublishSubject<Integer> observable = PublishSubject.create();

        observable.last().subscribe(testObserver);
        observable.onCompleted();

        verify(testObserver).onError(any(IndexOutOfBoundsException.class));
    }

    @Test
    public void mapTransformsEachEmittedItem() {
        Observable<Integer> observable = Observable.from(1, 2, 3);

        observable.map(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                return integer * 2;
            }
        }).subscribe(testObserver);

        verifyNotificationSequence(testObserver, Arrays.asList(2, 4, 6));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void materializeWrapsSequenceEventsWithRxNotifications() {
        Observable<Integer> observable = Observable.just(1);
        Observer<Notification<Integer>> testObserver = mock(Observer.class);
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        observable.materialize().subscribe(testObserver);
        verify(testObserver, times(2)).onNext(captor.capture());
        verify(testObserver).onCompleted();
        verifyNoMoreInteractions(testObserver);

        Notification<Integer> onNextNotification = captor.getAllValues().get(0);
        expect(onNextNotification.isOnNext()).toBeTrue();
        expect(onNextNotification.getValue()).toBe(1);

        Notification<Integer> onCompleted = captor.getValue();
        expect(onCompleted.isOnCompleted()).toBeTrue();
    }

    @Test
    public void mergeMapCombinesNewObservableWithEachNextSourceValue() {
        Observable<Integer> observable = Observable.from(1, 3);

        observable.mergeMap(new Func1<Integer, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(Integer integer) {
                return Observable.from(integer, integer * 2);
            }
        }).subscribe(testObserver);

        verifyNotificationSequence(testObserver, Arrays.asList(1, 2, 3, 6));
    }

    @Test
    public void mergeMapEmitsNewObservableForEachSourceNotification() {
        PublishSubject<Integer> observable = PublishSubject.create();
        observable.mergeMap(
                just1(Observable.from(1)),
                just1(Observable.from(-1)),
                just0(Observable.from(0)))
                .subscribe(testObserver);

        observable.onNext(100);
        observable.onCompleted();

        verifyNotificationSequence(testObserver, Arrays.asList(1, 0));
    }

    @Test
    public void mergeMapEmitsNewObservableFoOnErrorNotification() {
        PublishSubject<Integer> observable = PublishSubject.create();
        observable.mergeMap(
                just1(Observable.from(1)),
                just1(Observable.from(-1)),
                just0(Observable.from(0)))
                .subscribe(testObserver);

        observable.onError(new Throwable());

        verify(testObserver).onNext(-1);
        verify(testObserver).onCompleted();
        verifyNoMoreInteractions(testObserver);
    }

    @Test
    public void observableCreateDoesNotNeedToCallOnNext() {
        Observable<Integer> observable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onCompleted();
            }
        });

        observable.subscribe(testObserver);
        verify(testObserver, never()).onNext(anyInt());
        verify(testObserver).onCompleted();

        verifyNoMoreInteractions(testObserver);
    }

    @Test
    public void observableJustConvertsSingleObjectToObservable() {
        Observable<Integer> observable = Observable.just(1);

        observable.subscribe(testObserver);
        verify(testObserver).onNext(1);
        verify(testObserver).onCompleted();

        verifyNoMoreInteractions(testObserver);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void observableJustEmitsListAsASingleObject() {
        Observer<int[]> observer = mock(Observer.class);
        Observable<int[]> observable = Observable.just(new int[]{1, 2});

        observable.subscribe(observer);
        verify(observer).onNext(new int[]{1, 2});
        verify(observer).onCompleted();

        verifyNoMoreInteractions(observer);
    }

    @Test
    public void observableJustEmitsSingleObjectEventIfItsNull() {
        Observable<Integer> observable = Observable.just(null);

        observable.subscribe(testObserver);
        verify(testObserver).onNext(null);
        verify(testObserver).onCompleted();

        verifyNoMoreInteractions(testObserver);
    }

    @Test
    public void blockingObservableSingleReturnsTheOnlyElementEmitted() {
        PublishSubject<Integer> observable = PublishSubject.create();
        BlockingObservable<Integer> result = observable.toBlocking();

        runInNewThread(observable, Arrays.asList(2));
        assertThat(result.single(), is(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void blockingObservableSingleThrowsExceptionWhenMoreThenOneValueGetsEmitted() {
        PublishSubject<Integer> observable = PublishSubject.create();
        BlockingObservable<Integer> result = observable.toBlocking();

        runInNewThread(observable, Arrays.asList(1, 2));
        result.single();
    }

    @Test
    public void blockingObservableSingleReturnsSingleElementThatFulfillsPredicate() {
        PublishSubject<Integer> observable = PublishSubject.create();
        BlockingObservable<Integer> result = observable.toBlocking();

        runInNewThread(observable, Arrays.asList(1, 2));
        assertThat(result.single(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return integer % 2 == 0;
            }
        }), is(2));
    }

    @Test(expected = NoSuchElementException.class)
    public void blockingObservableSingleThrowsExceptionWhenNoValueFulfillsGivenPredicate() {
        PublishSubject<Integer> observable = PublishSubject.create();
        BlockingObservable<Integer> result = observable.toBlocking();

        runInNewThread(observable, Arrays.asList(1, 2));
        result.single(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return integer > 5;
            }
        });
    }

    @Test
    public void blockingObservableSingleOrDefaultReturnsNullAsAValidValue() {
        PublishSubject<Integer> observable = PublishSubject.create();
        BlockingObservable<Integer> result = observable.toBlocking();

        runInNewThreadEmitNull(observable);
        assertThat(result.singleOrDefault(2), is(nullValue()));
    }

    @Test
    public void blockingObservableSingleOrDefaultReturnsDefaultWhenNoValueWasEmitted() {
        PublishSubject<Integer> observable = PublishSubject.create();
        BlockingObservable<Integer> result = observable.toBlocking();

        runInNewThread(observable, Collections.<Integer>emptyList());
        assertThat(result.singleOrDefault(2), is(2));
    }

    @Test
    public void onErrorResumeNextEmitsAllSourceValuesWhenNoError() {
        Observable<Integer> observable = Observable.from(1, 2);
        Observable<Integer> runOnError = Observable.from(3, 4);

        observable
                .onErrorResumeNext(runOnError)
                .subscribe(testObserver);

        verifyNotificationSequence(testObserver, Arrays.asList(1, 2));
    }

    @Test
    public void onErrorResumeNextEmitsValuesFromNewObservableAfterError() {
        PublishSubject<Integer> observable = PublishSubject.create();
        Observable<Integer> runOnError = Observable.from(3, 4);

        observable
                .onErrorResumeNext(runOnError)
                .subscribe(testObserver);

        observable.onNext(1);
        observable.onError(new Throwable());

        verifyNotificationSequence(testObserver, Arrays.asList(1, 3, 4));
        verify(testObserver, never()).onError(any(Throwable.class));
    }

    @Test
    public void scanWorksAsAccumulatorFunction() {
        Observable<Integer> observable = Observable.from(1, 2, 3);
        observable.scan(new Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer accumulator, Integer currentValue) {
                return accumulator + currentValue;
            }
        }).subscribe(testObserver);

        verifyNotificationSequence(testObserver, Arrays.asList(1, 3, 6));
    }

    @Test
    public void scanWorksAsAccumulatorFunctionWithInitialValue() {
        Observable<Integer> observable = Observable.from(1, 2, 3);
        observable.scan(10, new Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer accumulator, Integer currentValue) {
                return accumulator + currentValue;
            }
        }).subscribe(testObserver);

        verifyNotificationSequence(testObserver, Arrays.asList(10, 11, 13, 16));
    }

    @Test
    public void takeLastEmitsOnlyGivenNumberOfLastElements() {
        Observable<Integer> observable = Observable.from(1, 2, 3, 4, 5, 6, 7, 8);
        observable.takeLast(2).subscribe(testObserver);

        verifyNotificationSequence(testObserver, Arrays.asList(7, 8));
    }

    @Test
    public void takeLastFromEmptyObserverDoesNotEmitAnyValue() {
        Observable<Integer> observable = Observable.empty();
        observable.takeLast(2).subscribe(testObserver);

        verify(testObserver, never()).onNext(anyInt());
        verify(testObserver).onCompleted();
    }
}

