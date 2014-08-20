import static helpers.TestFunctions.verifyNotificationSequence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import rx.Observer;
import rx.subjects.BehaviorSubject;

import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class BehaviourSubjectTest {

    @Mock
    private Observer<Integer> testObserver;

    @Test
    public void emitsDefaultItemWhenNoItemWasYetEmitted() {
        BehaviorSubject<Integer> subject = BehaviorSubject.create(2);

        subject.subscribe(testObserver);
        subject.onCompleted();

        verifyNotificationSequence(testObserver, Arrays.asList(2));
    }

    @Test
    public void emitsLastEmittedItemAndAllTheFollowing() {
        BehaviorSubject<Integer> subject = BehaviorSubject.create();

        subject.onNext(1);
        subject.onNext(2);
        subject.subscribe(testObserver);
        subject.onNext(3);
        subject.onCompleted();

        verifyNotificationSequence(testObserver, Arrays.asList(2, 3));
    }


}
