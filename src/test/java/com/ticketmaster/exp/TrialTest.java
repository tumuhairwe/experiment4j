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

import com.ticketmaster.exp.config.entry.EntryGuard;
import com.ticketmaster.exp.util.ReturnChoices;
import com.ticketmaster.exp.util.Selectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TrialTest {
  public static final String ARGS = "foo";

  Function<String, String> candidate = mock(Function.class);
  Function<String, String> control = mock(Function.class);
  Publisher<String> publisher = mock(Publisher.class);
  Clock clock = mock(Clock.class);

  @BeforeEach
  public void setUp() throws Exception {
    when(clock.instant()).thenAnswer((inv) -> Instant.EPOCH);
    when(candidate.apply(any())).thenReturn("candidate");
    when(control.apply(any())).thenReturn("control");
  }

  @Test
  @DisplayName("Only control is executed when doExperimentWhen() is never()")
  public void testOnlyControlWhenDoExperimentReturnsFalse() throws Exception {
    // GIVEN
    Trial<String, String> subject = new Trial(
        "trial",
        control,
        candidate,
        Executors.newSingleThreadExecutor(),
        ReturnChoices.alwaysCandidate(),
        Selectors.never(),
        Objects::equals,
        Objects::equals,
        publisher,
        clock
        );


    // WHEN
    String output = subject.apply("input");

    // THEN
    assertEquals("trial", subject.getName());
    assertEquals("control", output);
    verify(control, times(1)).apply("input");
    verify(candidate, never()).apply(anyString());

  }

  @Test
  @DisplayName("Only control is executed when doExperimentWhen() = always() and ReturnChoice = alwaysCandidateValue()")
  public void testOnlyControlWhenDoExperimentReturnsTrueAndAlwaysCandidateValue() {
    // GIVEN
    Trial<String, String> subject = new Trial(
        "trial",
        control,
        candidate,
        Executors.newSingleThreadExecutor(),
        ReturnChoices.alwaysCandidate(),
        Selectors.always(),
        Objects::equals,
        Objects::equals,
        publisher,
        clock
    );


    // WHEN
    String output = subject.apply("input");

    // THEN
    assertEquals("trial", subject.getName());
    assertEquals("candidate", output);
    verify(control, times(1)).apply("input");
    verify(candidate, times(1)).apply("input");
    verify(publisher, times(1)).publish(eq(MatchType.MISMATCH), any(Result.class));
  }

  @Test
  @DisplayName("Only control is executed when doExperimentWhen returns true and alwaysControlValue")
  public void testOnlyControlWhenDoExperimentReturnsTrueAndAlwaysControlValue() throws Exception {
    // GIVEN
    Trial<String, String> subject = new Trial(
        "trial",
        control,
        candidate,
        Executors.newSingleThreadExecutor(),
        ReturnChoices.alwaysControl(),
        Selectors.always(),
        Objects::equals,
        Objects::equals,
        publisher,
        clock
    );


    // WHEN
    String output = subject.apply("input");

    // THEN
    assertEquals("trial", subject.getName());
    assertEquals("control", output);
    verify(control, times(1)).apply("input");
    verify(candidate, times(1)).apply("input");
    verify(publisher, times(1)).publish(eq(MatchType.MISMATCH), any(Result.class));
  }

  @Test
  @DisplayName("Control is returned when candidate throws an exception")
  public void testTestOutputWhen() throws Exception {
    when(candidate.apply(anyString())).thenThrow(new IllegalArgumentException());
    // GIVEN
    Trial<String, String> subject = new Trial(
        "trial",
        control,
        candidate,
        Executors.newSingleThreadExecutor(),
        ReturnChoices.alwaysControl(),
        Selectors.always(),
        Objects::equals,
        Objects::equals,
        publisher,
        clock
    );


    // WHEN
    String output = subject.apply("input");

    // THEN
    assertEquals("trial", subject.getName());
    assertEquals("control", output);
    verify(control, times(1)).apply("input");
    verify(candidate, times(1)).apply("input");
    verify(publisher, times(1)).publish(eq(MatchType.CANDIDATE_EXCEPTION), any(Result.class));
  }

  @Test
  @DisplayName("Trial never runs when EntryGuard is NEVER")
  public void testApply_neverRun(){
    String trialName = "trial-that-never-runs";
    Trial<String, String> subject = new Trial(
        trialName,
        control,
        candidate,
        Executors.newSingleThreadExecutor(),
        ReturnChoices.alwaysControl(),
        Selectors.always(),
        Objects::equals,
        Objects::equals,
        publisher,
        clock
    )
            .withEntryGuard(EntryGuard.NEVER);

    String output = subject.apply(ARGS);

    assertNull(output);
    assertEquals(trialName, subject.getName());
    verify(control, never()).apply(anyString());
    verify(candidate, never()).apply(anyString());
    verify(publisher, never()).publish(any(), any());
  }
  @Test
  @DisplayName("Trial runs when EntryGuard is ALWAYS")
  public void testApply_alwaysRun(){
    String trialName = "trial-that-always-runs";
    Trial<String, String> subject = new Trial(
            trialName,
            control,
            candidate,
            Executors.newSingleThreadExecutor(),
            ReturnChoices.alwaysControl(),
            Selectors.always(),
            Objects::equals,
            Objects::equals,
            publisher,
            clock
    )
            .withEntryGuard(EntryGuard.ALWAYS);

    String output = subject.apply(ARGS);

    assertNotNull(output);
    assertEquals(trialName, subject.getName());
    verify(control, times(1)).apply(eq(ARGS));
    verify(candidate, times(1)).apply(eq(ARGS));
    verify(publisher, times(1)).publish(eq(MatchType.MISMATCH), any(Result.class)); // setUp() to have candidate -> "candidate" && control -> "control"
  }
}
