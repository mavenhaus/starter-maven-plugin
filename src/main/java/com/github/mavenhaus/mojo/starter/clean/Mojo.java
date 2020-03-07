package com.github.mavenhaus.mojo.starter.clean;

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

import com.github.mavenhaus.mojo.starter.AbstractMojo;
import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This MOJO realizes the goal <code>starter:clean</code> that deletes any files created by
 * <code>{@link com.github.mavenhaus.mojo.starter.resolve.Mojo starter:resolve}</code>
 * (more specific the resolved POM file which is by default <code>.resolved-pom.xml</code>).
 */
@org.apache.maven.plugins.annotations.Mojo(name = "clean", requiresProject = true, requiresDirectInvocation = false, executionStrategy = "once-per-session", threadSafe = true)
public class Mojo extends AbstractMojo {

  /**
   * Creates a new instance of {@link Mojo clean mojo}.
   */
  public Mojo() {
    super();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    File resolvedPomFile = getResolvedPomFile();

    if (resolvedPomFile.isFile()) {
      getLog().info("Deleting " + resolvedPomFile.getPath());
      boolean deleted = resolvedPomFile.delete();
      if (!deleted) {
        throw new MojoFailureException("Could not delete " + resolvedPomFile.getAbsolutePath());
      }
    }
  }

}
