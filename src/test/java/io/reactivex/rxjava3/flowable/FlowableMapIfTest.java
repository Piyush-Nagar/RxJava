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

package io.reactivex.rxjava3.flowable;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscriber;

import com.google.common.collect.ImmutableList;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableMapIfTest extends RxJavaTest {

    private Subscriber<String> stringSubscriber;

    @Before
    public void before() {
        stringSubscriber = TestHelper.mockSubscriber();
    }

    @Test
    public void mapIfForAllTrue() {
        List<String> actualResult = Flowable.fromIterable(ImmutableList.of("First", "FirstClass"))
            .mapIf(item -> item.startsWith("First"), map -> map.concat("1st"), map -> map.concat("2st"))
            .toList()
            .blockingGet();
        List<String> expectedResult = ImmutableList.of("First1st", "FirstClass1st");
        Assert.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void mapIfForAllFalse() {
        List<String> actualResult = Flowable.fromIterable(ImmutableList.of("Second", "SecondClass"))
            .mapIf(item -> item.startsWith("First"), map -> map.concat("1st"), map -> map.concat("2nd"))
            .toList()
            .blockingGet();
        List<String> expectedResult = ImmutableList.of("Second2nd", "SecondClass2nd");
        Assert.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void mapIfForTrueFalse() {
        List<String> actualResult = Flowable.fromIterable(
            ImmutableList.of("FirstClass", "SecondClass"))
            .mapIf(item -> item.startsWith("First"), map -> map.concat("1st"), map -> map.concat("2nd"))
            .toList()
            .blockingGet();
        List<String> expectedResult = ImmutableList.of("FirstClass1st", "SecondClass2nd");
        Assert.assertEquals(expectedResult, actualResult);
    }


    @Test(expected = NullPointerException.class)
    public void mapIfForNullPredicate() {
            Flowable.fromIterable(ImmutableList.of("First", "FirstClass"))
                .mapIf(null, map -> map.concat("1st"), map -> map.concat("2nd"))
                .subscribe(stringSubscriber);
    }


    @Test(expected = NullPointerException.class)
    public void mapIfForNullIfMapper() {
            Flowable.fromIterable(ImmutableList.of("First", "FirstClass"))
                .mapIf(item -> item.startsWith("First"), null, map -> map.concat("2nd"))
                .subscribe(stringSubscriber);
    }

    @Test(expected = NullPointerException.class)
    public void mapIfForNullElseMapper() {
            Flowable.fromIterable(ImmutableList.of("First", "FirstClass"))
                .mapIf(item -> item.startsWith("First"), map -> map.concat("1st"), null)
                .subscribe(stringSubscriber);
    }
}
