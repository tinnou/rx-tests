package com.tinnou.rx.cache

import rx.Observable
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import rx.schedulers.TestScheduler
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class RxCacheTest extends Specification {

    /*
     *  Problem statement:
     *      Implement an Observable that emits one or multiple items.
     *      Cache those exact items to be replayed to future subscriptions.
     *
     */

    def "No cache: Emit 5 events at a 1 second interval, with 2 subscriptions registered at different times."() {
        setup:
        TestSubscriber<UniqueItem> testSubscriber = new TestSubscriber<>()
        TestSubscriber<UniqueItem> testSubscriber2 = new TestSubscriber<>()

        TestScheduler testScheduler = Schedulers.test()
        def callable = Observable.create(new OnSubscribeCustomTimerPeriodically(1, 1, TimeUnit.SECONDS, testScheduler))
                .take(5)
                .subscribeOn(testScheduler)

        when:
        callable.subscribe(testSubscriber)
        testScheduler.advanceTimeBy(7, TimeUnit.SECONDS)
        then:
        List<UniqueItem> events = testSubscriber.getOnNextEvents()
        testSubscriber.assertCompleted()

        when: "Second subscription subscribes, it triggers the execution of the observable one more time."
        callable.subscribe(testSubscriber2)
        testScheduler.advanceTimeBy(7, TimeUnit.SECONDS)

        then:
        List<UniqueItem> events2 = testSubscriber2.getOnNextEvents()
        testSubscriber2.assertCompleted()

        then: "To prove it, we check the emitted events references for inequality"
        !events[0].is(events2[0])
        !events[1].is(events2[1])
        !events[2].is(events2[2])
        !events[3].is(events2[3])
        !events[4].is(events2[4])
    }

    def "With cache: Emit 5 events at a 1 second interval, with 2 subscriptions registered at different times."() {
        setup:
        TestSubscriber<UniqueItem> testSubscriber = new TestSubscriber<>()
        TestSubscriber<UniqueItem> testSubscriber2 = new TestSubscriber<>()

        TestScheduler testScheduler = Schedulers.test()

        def callable = Observable.create(new OnSubscribeCustomTimerPeriodically(1, 1, TimeUnit.SECONDS, testScheduler)) // emits an infinite sequence of numbers every second
                .take(5) // only take first 5
                .share() // .share() transforms Observable to a ConnectableObservable
                .subscribeOn(testScheduler) // let's control the ticks
                .replay(testScheduler) // .replay() operator makes sure that all our Subscribers,
                          // no matter when they have subscribed to our Observable will receive the same data
                          // that was produced from the initial execution of our interval method

        callable.connect() // connect once, there is no need to call .connect() again thanks to .replay()
        when:
        callable.subscribe(testSubscriber)
        testScheduler.advanceTimeBy(7, TimeUnit.SECONDS)
        then:
        List<UniqueItem> events = testSubscriber.getOnNextEvents()
        testSubscriber.assertCompleted()

        when: "Now the subscription should receive the same exact cached objects after subscribing."
        callable.subscribe(testSubscriber2)
        testScheduler.advanceTimeBy(7, TimeUnit.SECONDS)

        then:
        List<UniqueItem> events2 = testSubscriber2.getOnNextEvents()
        testSubscriber2.assertCompleted()

        then: "To make sure, we check the emitted events reference for equality"
        events[0].is(events2[0])
        events[1].is(events2[1])
        events[2].is(events2[2])
        events[3].is(events2[3])
        events[4].is(events2[4])
    }
}
