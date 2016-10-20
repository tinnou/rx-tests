package com.tinnou.rx.cache;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action0;

import java.util.concurrent.TimeUnit;

public final class OnSubscribeCustomTimerPeriodically implements Observable.OnSubscribe<UniqueItem> {
        final long initialDelay;
        final long period;
        final TimeUnit unit;
        final Scheduler scheduler;

        public OnSubscribeCustomTimerPeriodically(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {
            this.initialDelay = initialDelay;
            this.period = period;
            this.unit = unit;
            this.scheduler = scheduler;
        }

        @Override
        public void call(final Subscriber<? super UniqueItem> child) {
            final Scheduler.Worker worker = scheduler.createWorker();
            child.add(worker);
            worker.schedulePeriodically(new Action0() {
                long counter;
                @Override
                public void call() {
                    try {
                        child.onNext(new UniqueItem(counter++));
                    } catch (Throwable e) {
                        try {
                            worker.unsubscribe();
                        } finally {
                            Exceptions.throwOrReport(e, child);
                        }
                    }
                }

            }, initialDelay, period, unit);
        }
    }