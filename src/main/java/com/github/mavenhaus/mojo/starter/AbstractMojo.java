package com.github.mavenhaus.mojo.starter;

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

import java.io.File;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * This is the abstract base class for {@link org.apache.maven.plugin.AbstractMojo}.
 */
public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

  /**
   * The directory where the generated POM file is written to.
   */
  @Parameter(property = "starter.outputDirectory", defaultValue = "${project.basedir}")
  private File outputDirectory;

  /**
   * The filename of the generated POM file.
   */
  @Parameter(property = "starter.resolvedPomFilename", defaultValue = ".resolved-pom.xml")
  private String resolvedPomFilename;

  /**
   * Creates a new instance of {@link AbstractMojo}.
   */
  public AbstractMojo() {
    super();
  }

  /**
   * Returns the filename of the generated POM file.
   *
   * @return the filename of the generated POM file.
   */
  public String getResolvedPomFilename() {
    return resolvedPomFilename;
  }

  /**
   * Returns the directory where the generated POM file is written to.
   *
   * @return the directory where the generated POM file is written to.
   */
  public File getOutputDirectory() {
    return outputDirectory;
  }

  /**
   * Returns a {@link File} instance pointing to the resolved POM.
   *
   * @return a {@link File} instance pointing to the resolved POM.
   */
  protected File getResolvedPomFile() {
    return new File(getOutputDirectory(), getResolvedPomFilename());
  }

}
