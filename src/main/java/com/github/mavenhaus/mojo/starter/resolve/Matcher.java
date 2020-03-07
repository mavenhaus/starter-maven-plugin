package com.github.mavenhaus.mojo.starter.resolve;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.regex.Pattern;

/**
 * This is the matcher configuration for the parameter names.
 */
public class Matcher {

  private static final Pattern EMPTY_PATTERN = Pattern.compile("");

  /**
   * The pattern string coming from the configuration section.
   * The <code>any character</code> has been changed to <code>~</code> as the
   * <code>.</code> is heavily used in parameter names and it would be
   * inconvenient to escape them all the time. As an example the pattern for
   * <code>match all the strings starting with project</code> would look like
   * <code>project.~*</code>.
   */
  private String pattern;

  /**
   * The precompiled regex pattern to use for matching.
   */
  private Pattern compiledPattern;

  /**
   * The action to be taken in case of a pattern match.
   */
  private Action action;

  /**
   * Pre-compiles the pattern.
   *
   * The <code>any character</code> has been changed to <code>~</code> as the
   * <code>.</code> is heavily used in parameter names and it would be
   * inconvenient to escape them all the time. As an example the pattern for
   * <code>match all the strings starting with project</code> would look like
   * <code>project.~*</code>.

   * @param pattern the regex pattern (<code>~</code> instead of <code>.</code>).
   * @return the compiled regex pattern.
   */
  public static Pattern compilePattern(String pattern) {
    pattern = pattern.replaceAll("\\.", "\\\\.");
    pattern = pattern.replaceAll("~", ".");
    pattern = "^" + pattern + "$";

    return Pattern.compile(pattern);
  }

  /**
   * Creates a new empty instance of {@link Matcher}.
   * Needed by maven to configure the plugin.
   */
  public Matcher() {
    this.pattern = null;
    this.compiledPattern = null;
    this.action = null;
  }

  /**
   * Creates a new instance of {@link Matcher}.
   *
   * @param pattern the regex pattern.
   * @param action the action to take.
   */
  public Matcher(String pattern, String action) {
    this(pattern, Action.valueOf(action));
  }

  /**
   * Creates a new instance of {@link Matcher}.
   *
   * @param pattern the regex pattern.
   * @param action the action to take.
   */
  public Matcher(String pattern, Action action) {
    this.pattern = pattern;
    this.compiledPattern = compilePattern(pattern);
    this.action = action;
  }

  /**
   * Returns the regex pattern.
   * Defaults to empty pattern if not specified.
   *
   * @return the regex pattern.
   */
  public String getPattern() {
    if (pattern == null) {
      return "";
    }
    return pattern;
  }

  /**
   * Returns the compiled regex pattern.
   * Defaults to empty pattern if not specified.
   *
   * @return the compiled regex pattern.
   */
  public Pattern getCompiledPattern() {
    if (compiledPattern == null) {
      if (pattern == null) {
        return EMPTY_PATTERN;
      }
      compiledPattern = compilePattern(pattern);
    }
    return compiledPattern;
  }

  /**
   * Returns the action to be taken.
   * Defaults to keep if not specified.
   *
   * @return the action to be taken.
   */
  public Action getAction() {
    if (action == null) {
      return Action.keep;
    }
    return action;
  }

}
