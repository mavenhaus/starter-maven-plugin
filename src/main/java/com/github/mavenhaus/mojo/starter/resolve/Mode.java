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

import java.util.LinkedList;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * This enum contains the predefined modes how to deal with {@link Matcher properties} when {@link Mojo generating the resolved POM}.
 */
public enum Mode {

  /**
   * No parameter is replaced by default {@link Matcher parameters}.
   * Keeps the <code>starter-maven-plugin</code>.
   */
  clean,

  /**
   * Resolves the <code>revision, sha1, changelist and project.*</code>.
   * Removes the <code>starter-maven-plugin</code> from the build/profiles section.
   */
  starter;

  /**
   * Returns the {@link Matcher matchers} defined by this {@link Mode}.
   *
   * @return the {@link Matcher matchers}.
   * @throws MojoExecutionException if anything goes wrong.
   */
  public List<Matcher> getMatchers() throws MojoExecutionException {
    List<Matcher> matchers = new LinkedList<>();

    switch (this) {
      case starter:
        matchers.add(new Matcher("revision", Action.resolve));
        matchers.add(new Matcher("sha1", Action.resolve));
        matchers.add(new Matcher("changelist", Action.resolve));
        matchers.add(new Matcher("project.~*", Action.resolve));
        matchers.add(new Matcher("parent.~*", Action.resolve));
        break;
      case clean:
        break;
      default:
        throw new MojoExecutionException("Unhandled case for the resolve mode");
    }

    return matchers;
  }

}
