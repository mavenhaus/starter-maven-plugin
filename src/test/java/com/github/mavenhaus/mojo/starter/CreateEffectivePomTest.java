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

import com.github.mavenhaus.mojo.starter.resolve.Mojo;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.handler.manager.DefaultArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.shared.dependencies.resolve.internal.DefaultDependencyResolver;
import org.junit.Test;

/**
 * Test-Case for {@link Mojo}.
 */
public class CreateEffectivePomTest {

  Mojo resolveMojo = new Mojo();

  /**
   * Tests method to create effective POM.
   *
   * @throws Exception if something goes wrong.
   */
  @Test
  public void testCreateEffectivePom() throws Exception {

//    resolveMojo.

    String magicValue = "magic-value";
    Properties userProperties = new Properties();
    userProperties.setProperty("cmd.test.property", magicValue);

    File pomFile = new File("src/test/resources/cmdpropertysubstituion/pom.xml");
    ArtifactRepository localRepository = new MavenArtifactRepository();
    localRepository.setLayout(new DefaultRepositoryLayout());
    ArtifactFactory artifactFactory = new DefaultArtifactFactory();
    ArtifactHandlerManager artifactHandlerManager = new DefaultArtifactHandlerManager();
    setDeclaredField(artifactFactory, "artifactHandlerManager", artifactHandlerManager);
    Map<String, ArtifactHandler> artifactHandlers = new HashMap<>();
    setDeclaredField(artifactHandlerManager, "artifactHandlers", artifactHandlers);
    DefaultDependencyResolver dependencyResolver = new DefaultDependencyResolver();
    DefaultProjectBuildingRequest projectBuildingRequest = new DefaultProjectBuildingRequest();
//    StarterModelResolver resolver = new StarterModelResolver(localRepository, artifactFactory, dependencyResolver, projectBuildingRequest, Collections.emptyList());
//    ModelBuildingRequest buildingRequest = new DefaultModelBuildingRequest().setPomFile(pomFile).setModelResolver(resolver).setUserProperties(userProperties);
//    setDeclaredField(tested, "defaultModelBuilder", new DefaultModelBuilderFactory().newInstance());
//    Model resolvedPom = tested.createResolvedPom(pomFile);
//    assertThat(resolvedPom.getName()).isEqualTo(magicValue);

//    Model effectivePom = tested.createEffectivePom(buildingRequest, false, ResolveMode.starter);
//    assertThat(effectivePom.getName()).isEqualTo(magicValue);
  }

  private void setDeclaredField(Object pojo, String fieldName, Object propertyValue)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = pojo.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(pojo, propertyValue);
  }
}
