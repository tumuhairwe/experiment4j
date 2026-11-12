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
import com.ticketmaster.exp.config.entry.ExactMismatchGuard;
import com.ticketmaster.exp.publish.StringConsumerPublisher;
import com.ticketmaster.exp.util.ReturnChoices;
import com.ticketmaster.exp.util.Selectors;
import com.ticketmaster.exp.util.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
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
  @DisplayName("Only control runs, and its result is returned, when EntryGuard is NEVER")
  public void testApply_guardDenies_runsOnlyControl(){
    String trialName = "trial-that-never-enters";
    Trial<String, String> subject = guardedTrial(trialName, ReturnChoices.alwaysControl(), EntryGuard.NEVER);

    String output = subject.apply(ARGS);

    assertEquals("control", output);
    assertEquals(trialName, subject.getName());
    verify(control, times(1)).apply(eq(ARGS));
    verify(candidate, never()).apply(anyString());
    verify(publisher, never()).publish(any(), any());
    verify(publisher, never()).accept(any(), any());
  }

  @Test
  @DisplayName("Control result is returned when EntryGuard is NEVER, even if returnChoice prefers the candidate")
  public void testApply_guardDenies_ignoresCandidateReturnChoice(){
    Trial<String, String> subject = guardedTrial("trial-candidate-choice", ReturnChoices.alwaysCandidate(), EntryGuard.NEVER);

    String output = subject.apply(ARGS);

    assertEquals("control", output);
    verify(candidate, never()).apply(anyString());
  }

  @Test
  @DisplayName("Control exception propagates to the caller when EntryGuard is NEVER")
  public void testApply_guardDenies_rethrowsControlException(){
    when(control.apply(anyString())).thenThrow(new IllegalStateException("control failed"));
    Trial<String, String> subject = guardedTrial("trial-control-throws", ReturnChoices.alwaysControl(), EntryGuard.NEVER);

    IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> subject.apply(ARGS));

    assertEquals("control failed", thrown.getMessage());
    verify(candidate, never()).apply(anyString());
  }

  private Trial<String, String> guardedTrial(String name,
                                             Function<Result<String>, Try<String>> returnChoice,
                                             EntryGuard entryGuard) {
    return new Trial<>(
        name,
        control,
        candidate,
        Executors.newSingleThreadExecutor(),
        returnChoice,
        Selectors.always(),
        Objects::equals,
        Objects::equals,
        publisher,
        clock
    ).withEntryGuard(entryGuard);
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

  @Test
  @DisplayName("EntryGuard receives the experiment's running totals, shared across trials")
  public void testApply_guardReceivesRunningTotals() {
    List<int[]> seen = new ArrayList<>();
    EntryGuard recordingGuard = (totalRuns, mismatches, dateTime) -> {
      seen.add(new int[] {totalRuns, mismatches});
      return true;
    };
    Experiment<String, String> experiment = Experiment.<String, String>named("counted")
        .control(control)
        .candidate(candidate)
        .publishedBy(publisher)
        .timedBy(clock)
        .doExperimentWhen(Selectors.always());

    // each call creates a new Trial, as callers do; totals must survive across them
    experiment.trial().withEntryGuard(recordingGuard).apply(ARGS);  // control != candidate -> mismatch
    when(candidate.apply(any())).thenReturn("control");
    experiment.trial().withEntryGuard(recordingGuard).apply(ARGS);  // match
    experiment.trial().withEntryGuard(recordingGuard).apply(ARGS);

    assertArrayEquals(new int[] {0, 0}, seen.get(0));
    assertArrayEquals(new int[] {1, 1}, seen.get(1));
    assertArrayEquals(new int[] {2, 1}, seen.get(2));
    assertEquals(3, experiment.getStats().getTotalRuns());
    assertEquals(1, experiment.getStats().getMismatches());
  }

  @Test
  @DisplayName("A mismatch-count guard stops the candidate once the limit is exceeded")
  public void testApply_mismatchGuardStopsCandidateAfterMismatch() {
    Experiment<String, String> experiment = Experiment.<String, String>named("mismatch-limited")
        .control(control)
        .candidate(candidate)
        .publishedBy(publisher)
        .timedBy(clock)
        .doExperimentWhen(Selectors.always());
    EntryGuard noMismatchesAllowed = new ExactMismatchGuard(0);

    experiment.trial().withEntryGuard(noMismatchesAllowed).apply(ARGS);  // mismatch recorded
    experiment.trial().withEntryGuard(noMismatchesAllowed).apply(ARGS);  // guard must deny

    verify(candidate, times(1)).apply(anyString());
  }

  @Test
  @DisplayName("Control-only runs are not counted towards the experiment's totals")
  public void testApply_controlOnlyRunsAreNotCounted() {
    Experiment<String, String> experiment = Experiment.<String, String>named("control-only")
        .control(control)
        .candidate(candidate)
        .publishedBy(publisher)
        .timedBy(clock)
        .doExperimentWhen(Selectors.never());

    experiment.trial().apply(ARGS);

    assertEquals(0, experiment.getStats().getTotalRuns());
    assertEquals(0, experiment.getStats().getMismatches());
  }

  @Test
  @DisplayName("Overrides keep the experiment's running totals")
  public void testOverride_keepsRunningTotals() {
    Experiment<String, String> experiment = Experiment.<String, String>named("overridden")
        .control(control)
        .candidate(candidate)
        .publishedBy(publisher)
        .timedBy(clock)
        .doExperimentWhen(Selectors.never());

    experiment.trialWithOverrides(Overrides.<String>overrides().doExperimentWhen(Selectors.always()))
        .apply(ARGS);

    assertEquals(1, experiment.getStats().getTotalRuns());
  }

  @Test
  @DisplayName("Publisher is invoked exactly once per trial")
  public void testApply_publishesOncePerTrial() {
    AtomicInteger publishCount = new AtomicInteger();
    Experiment<String, String> experiment = Experiment.<String, String>named("published-once")
        .control(control)
        .candidate(candidate)
        .publishedBy((matchType, result) -> publishCount.incrementAndGet())
        .timedBy(clock)
        .doExperimentWhen(Selectors.always());

    experiment.trial().apply(ARGS);
    experiment.trial().apply(ARGS);

    assertEquals(2, publishCount.get());
  }

  @Test
  @DisplayName("A built-in publisher emits one report per trial")
  public void testApply_builtInPublisherReportsOncePerTrial() {
    List<String> lines = new ArrayList<>();
    Experiment<String, String> experiment = Experiment.<String, String>named("string-published")
        .control(control)
        .candidate(candidate)
        .publishedBy(new StringConsumerPublisher<>(lines::add))
        .timedBy(clock)
        .doExperimentWhen(Selectors.always());

    experiment.trial().apply(ARGS);

    // StringConsumerPublisher writes 3 lines per publish: candidate time, control time, match message
    assertEquals(3, lines.size());
    assertEquals(1, lines.stream().filter(l -> l.startsWith("string-published: ")).count());
  }
}
