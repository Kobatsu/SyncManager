package com.cargo.sbpd.bus;

import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

/**
 * Created by leb on 31/07/2017.
 */

// Middleman object
public class RxBus {

    private static RxBus rxBus;

    public static RxBus getRxBusSingleton() {
        if (rxBus == null) {
            rxBus = new RxBus();
        }
        return rxBus;
    }

    private final Relay<Object> _bus = PublishRelay.create().toSerialized();

    public void send(RxMessage o) {
        _bus.accept(o);
    }

    public Flowable<Object> asFlowable() {
        return _bus.toFlowable(BackpressureStrategy.BUFFER);
    }

    public boolean hasObservers() {
        return _bus.hasObservers();
    }
}

