package io.github.vladimirmi.localradio.data.net;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;

/**
 * Created by Vladimir Mikhalev 09.05.2018.
 */
public class RxRetryTransformer<T> implements SingleTransformer<T, T> {

    private Throwable throwable;

    @Override
    public SingleSource<T> apply(Single<T> upstream) {
        return upstream.retryWhen(attempts -> attempts
                .zipWith(Flowable.range(1, Api.RETRY_COUNT), (throwable, attempt) -> {
                    this.throwable = throwable;
                    return attempt;
                }).flatMap(attempt -> {
                    if (attempt == Api.RETRY_COUNT) {
                        return Flowable.error(throwable);
                    } else {
                        return Flowable.timer(Api.RETRY_DELAY, TimeUnit.MILLISECONDS)
                                .map(aLong -> new Object());
                    }
                })
        );
    }
}
