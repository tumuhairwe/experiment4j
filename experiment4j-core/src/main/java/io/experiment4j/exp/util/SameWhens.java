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

package io.experiment4j.exp.util;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;

public class SameWhens {
  SameWhens() {
  }

  public static <M> BiFunction<M, M, Boolean> classesMatch() {
    return (M m1, M m2) -> m1 != null && m2 != null && m1.getClass().equals(m2.getClass());
  }

  /** Exceptions match when their messages are equal; two missing (null) messages are equal. */
  public static BiFunction<Exception, Exception, Boolean> messagesMatch() {
    return (Exception e1, Exception e2) -> Objects.equals(e1.getMessage(), e2.getMessage());
  }

  public static <M> BiFunction<M, M, Boolean> fromComparator(Comparator<M> comparator) {
    return (M m1, M m2) -> comparator.compare(m1, m2) == 0;
  }

  /** Exceptions match when their causes are equal; two missing (null) causes are equal. */
  public static BiFunction<Exception, Exception, Boolean> causesMatch() {
    return (Exception e1, Exception e2) -> Objects.equals(e1.getCause(), e2.getCause());
  }
}
