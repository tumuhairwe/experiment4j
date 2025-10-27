/*
 * Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ticketmaster.exp.publish;

import com.ticketmaster.exp.MatchType;
import com.ticketmaster.exp.Publisher;
import com.ticketmaster.exp.Result;
import com.ticketmaster.exp.publish.DurationNamer.DurationType;
import com.ticketmaster.exp.util.Assert;

import java.time.Duration;
import java.util.function.BiPredicate;

public class MeasurerPublisher<K> implements Publisher<K> {
  public static final MeasurerPublisher DEFAULT = MeasurerPublisher.<String>builder()
      .durationNamer(PatternDurationNamer.DEFAULT)
      .matchCountNamer(PatternMatchCountNamer.DEFAULT)
      .measurer(PrintStreamMeasurer.DEFAULT)
      .build();

  private final Measurer measurer;
  private final MatchCountNamer matchCountNamer;
  private final DurationNamer durationNamer;

  @Override
  public void accept(MatchType matchType, Result<K> payload) {
    publish(matchType, payload);
  }

  public static class Builder<K> {
    private Measurer measurer = PrintStreamMeasurer.DEFAULT;
    private MatchCountNamer<K> matchCountNamer;
    private DurationNamer<K> durationNamer;

    private Builder() {
    }

    public MeasurerPublisher<K> build() {
      return new MeasurerPublisher<K>(measurer, matchCountNamer, durationNamer);
    }

    public Builder<K> measurer(Measurer measurer) {
      this.measurer = measurer;
      BiPredicate<String, Integer> predicate = (s, i) -> i % 1000 == 0;
      return this;
    }

    public Builder<K> matchCountNamer(MatchCountNamer matchCountNamer) {
      this.matchCountNamer = matchCountNamer;
      return this;
    }

    public Builder<K> durationNamer(DurationNamer durationNamer) {
      this.durationNamer = durationNamer;
      return this;
    }

  }

  public static <K> Builder<K> builder() {
    return new Builder<>();
  }

  private MeasurerPublisher(Measurer<K> measurer,
                            MatchCountNamer<K> matchCountNamer,
                            DurationNamer<K> durationNamer) {

    Assert.notNull(measurer, "measurer must be non-null");
    Assert.notNull(matchCountNamer, "matchCountNamer must be non-null");
    Assert.notNull(durationNamer, "durationNamer must be non-null");
    this.measurer = measurer;
    this.matchCountNamer = matchCountNamer;
    this.durationNamer = durationNamer;
  }

  @Override
  public void publish(MatchType matchType, Result<K> payload) {
    String name = payload.getName();
    measurer.measureCount(matchCountNamer.name(name, matchType), 1);
    Duration controlDuration = payload.getControlResult().getDuration();
    Duration candidateDuration = payload.getCandidateResult().getDuration();
    Duration difference = controlDuration.minus(candidateDuration);
    measurer.measureDuration(durationNamer.name(name, DurationType.CONTROL), controlDuration);
    measurer.measureDuration(durationNamer.name(name, DurationType.CANDIDATE), candidateDuration);
    measurer.measureDuration(durationNamer.name(name, DurationType.IMPROVEMENT), difference);
  }
}
