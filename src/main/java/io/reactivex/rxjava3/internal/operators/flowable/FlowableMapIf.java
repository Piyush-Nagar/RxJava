/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava3.internal.operators.flowable;

import java.util.Objects;

import org.reactivestreams.Subscriber;

import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.internal.fuseable.ConditionalSubscriber;
import io.reactivex.rxjava3.internal.subscribers.BasicFuseableConditionalSubscriber;
import io.reactivex.rxjava3.internal.subscribers.BasicFuseableSubscriber;

public final class FlowableMapIf<T, U> extends AbstractFlowableWithUpstream<T, U> {

    private final Function<? super T, ? extends U> ifMapper;
    private final Function<? super T, ? extends U> elseMapper;
    private final Predicate<? super T> predicate;

    public FlowableMapIf(Flowable<T> source, Function<? super T, ? extends U> ifMapper,
        Function<? super T, ? extends U> elseMapper, Predicate<? super T> predicate) {
        super(source);
        this.ifMapper = ifMapper;
        this.elseMapper = elseMapper;
        this.predicate = predicate;
    }

    @Override
    protected void subscribeActual(Subscriber<? super U> s) {
        if (s instanceof ConditionalSubscriber) {
            source.subscribe(new MapConditionalSubscriber<T, U>((ConditionalSubscriber<? super U>)s,
                ifMapper, elseMapper, predicate));
        } else {
            source.subscribe(new MapSubscriber<T, U>(s, ifMapper, elseMapper, predicate));
        }
    }

    static final class MapSubscriber<T, U> extends BasicFuseableSubscriber<T, U> {

        final Function<? super T, ? extends U> ifMapper;
        final Function<? super T, ? extends U> elseMapper;
        final Predicate<? super T> predicate;

        MapSubscriber(Subscriber<? super U> actual,
            Function<? super T, ? extends U> ifMapper,
            Function<? super T, ? extends U> elseMapper,
            Predicate<? super T> predicate) {
            super(actual);
            this.ifMapper = ifMapper;
            this.elseMapper = elseMapper;
            this.predicate = predicate;
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }

            if (sourceMode != NONE) {
                downstream.onNext(null);
                return;
            }

            U v;

            try {
                v = applyConditionally(t);

            } catch (Throwable ex) {
                fail(ex);
                return;
            }
            downstream.onNext(v);
        }

        @Override
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Nullable
        @Override
        public U poll() throws Throwable {
            T t = qs.poll();
            return t != null ? applyConditionally(t) : null;
        }

        private U applyConditionally(T t) throws Throwable {
            return predicate.test(t) ?
              Objects.requireNonNull(ifMapper.apply(t), "The ifMapper function returned a null value.") :
              Objects.requireNonNull(elseMapper.apply(t), "The elseMapper function returned a null value.");
        }
    }

    static final class MapConditionalSubscriber<T, U> extends
        BasicFuseableConditionalSubscriber<T, U> {
      final Function<? super T, ? extends U> ifMapper;
      final Function<? super T, ? extends U> elseMapper;
      final Predicate<? super T> predicate;

        MapConditionalSubscriber(ConditionalSubscriber<? super U> actual,
            Function<? super T, ? extends U> ifMapper,
            Function<? super T, ? extends U> elseMapper,
            Predicate<? super T> predicate) {
            super(actual);
          this.ifMapper = ifMapper;
          this.elseMapper = elseMapper;
          this.predicate = predicate;        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }

            if (sourceMode != NONE) {
                downstream.onNext(null);
                return;
            }

            U v;

            try {
              v = applyConditionally(t);
            } catch (Throwable ex) {
                fail(ex);
                return;
            }
            downstream.onNext(v);
        }

        @Override
        public boolean tryOnNext(T t) {
            if (done) {
                return true;
            }

            if (sourceMode != NONE) {
                downstream.tryOnNext(null);
                return true;
            }

            U v;

            try {
              v = applyConditionally(t);
            } catch (Throwable ex) {
                fail(ex);
                return true;
            }
            return downstream.tryOnNext(v);
        }

        @Override
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Nullable
        @Override
        public U poll() throws Throwable {
            T t = qs.poll();
            return t != null ? applyConditionally(t) : null;
        }

        private U applyConditionally(T t) throws Throwable {
          return predicate.test(t) ?
            Objects.requireNonNull(ifMapper.apply(t), "The ifMapper function returned a null value.") :
            Objects.requireNonNull(elseMapper.apply(t), "The elseMapper function returned a null value.");
        }
    }

}
