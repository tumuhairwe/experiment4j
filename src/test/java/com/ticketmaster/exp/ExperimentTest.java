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

package com.ticketmaster.exp;

import com.ticketmaster.exp.publish.PrintStreamPublisher;
import com.ticketmaster.exp.util.Selectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.ticketmaster.exp.Science.science;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by dannwebster on 6/20/15.
 */
public class ExperimentTest {
  @Test
  @DisplayName("science() builder should create an experiment with the experiment-name")
  public void testExperiment() {
    // GIVEN
    science().experiment("person-name", () -> new Experiment<>("person-name"));

    // WHEN
    String name = science().experiments().get("person-name").getName();

    // THEN
    assertEquals("person-name", name);
  }

  @Test
  @DisplayName("should run a trial with control and candidate functions")
  public void testTrialType() {
        // GIVEN
        Map<Integer, String> controlMap = new HashMap<>();
        controlMap.put(1, "ten");
        controlMap.put(2, "twenty");
        controlMap.put(3, "thirty");
        controlMap.put(4, "forty");
        controlMap.put(5, "fifty");
        controlMap.put(6, "sixty");

        Map<Integer, String> candidateMap = new HashMap<>();
        candidateMap.put(1, "one hundred");
        candidateMap.put(2, "two hundred");
        candidateMap.put(3, "three hundred");
        candidateMap.put(4, "four hundred");
        candidateMap.put(5, "five hundred");
        candidateMap.put(6, "six hundred");

        Supplier<Experiment<Integer, String>> expSupplier = () -> new Experiment<Integer, String>("integer-to-string-experiment")
            .control(key -> controlMap.get(key))
            .candidate(candidateMap::get);

      Trial<Integer, String> trial = science()
              .experiment("trial-type-test", expSupplier)
              .publishedBy(new PrintStreamPublisher<>())
              .timedBy(Clock.system(ZoneId.systemDefault()))
              .doExperimentWhen(Selectors.always())
              .trial();

      // run trial
      for (int i = 1; i <= 5; i++){
          String result = trial.apply(i);
          assertEquals(controlMap.get(i), result);
      }
    }
}
