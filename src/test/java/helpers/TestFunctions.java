package helpers;

import rx.functions.Func0;
import rx.functions.Func1;

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


}
