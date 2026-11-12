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

import org.junit.jupiter.api.Test;

import java.util.function.Function;

public class ExamplesTest {
  public static class Person {
    private final String firstName;
    private final String lastName;

    public Person(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getLastName() {
      return lastName;
    }
  }

  Function<Person, String> oldToName = (p) -> p.getFirstName() + " " + p.getLastName();
  Function<Person, String> newToName = (p) -> "%s %s".formatted(p.getFirstName(), p.getLastName());
  //can do a DB lookup to determine if this request is unique (to count towards Fixed Run Experiment)
  Function<Person, Boolean> isUnique = (p) -> p.getFirstName().length() + p.getLastName().length() < 10;

  Person[] people = {
      new Person("George", "Washington"),
      new Person("John", "Adams"),
      new Person("Thomas", "Jefferson"),
  };

  @Test
  public void showInlineSyntax() throws Exception {
//    for (Person person : people) {
//      inlineSyntax(person);
//    }

  }

  /**
   * This syntax is designed to keep the configuration of the experiment as close as
   * possible to the execution of the experiment
   * @throws Exception
   */
//  public void inlineSyntax(Person person) throws Exception {
//    Trial<Person, String> trial = science()
//            .experiment(
//                    "person-name",
//                    () -> Experiment.<Person, String>named("person-name")
//                            .control(oldToName)
//                            .candidate(newToName)
//                            .exceptionsSameWhen(SameWhens.messagesMatch())
//                            //.exceptionsSameWhen(SameWhens.causesMatch())
//                            .sameWhen(Objects::equals)
//                            .doExperimentWhen(Selectors.always())
//                            .returnChoice(ReturnChoices.alwaysControl())
//                            .withExecutorService(Executors.newSingleThreadExecutor())
//                            .publishedBy(new PrintStreamPublisher<>())
//                            .timedBy(Clock.systemUTC())
////                    .exitCondition(request -> request
////                            .maxDuration(java.time.Duration.ofSeconds(2))
////                            .maxControlExecs(100)
////                            .maxCandidateExecs(100)
////                            .minExecsPerVariant(10)
////                            .uniqueRequestsForFixedRun(isUnique, 20)
//            )
//            .trialWithOverrides(Overrides.<String>overrides()
//                    .doExperimentWhen(Selectors.percent(5))
//                    .returnChoice(ReturnChoices.candidateWhen(Selectors.percent(5)))
//            );
//    String personName = trial.apply(person);
//
//  }
}
