/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
File originalPom = new File(basedir, 'pom.xml')
assert originalPom.exists()

def originalProject = new XmlSlurper().parse(originalPom)
// required elements
assert '4.0.0' == originalProject.modelVersion.text()
assert 'com.github.mavenhaus.starter.its' == originalProject.groupId.text()
assert 'optional-elements-modeOssrh' == originalProject.artifactId.text()
assert '0.0.1-SNAPSHOT' == originalProject.version.text()
// banned elements for artifact
assert 1 == originalProject.build.size()
assert 1 == originalProject.ciManagement.size()
assert 1 == originalProject.contributors.size()
assert 1 == originalProject.dependencyManagement.size()
assert 1 == originalProject.description.size()
assert 1 == originalProject.developers.size()
assert 1 == originalProject.distributionManagement.size()
assert 1 == originalProject.issueManagement.size()
assert 1 == originalProject.mailingLists.size()
assert 0 == originalProject.name.size()
assert 1 == originalProject.organization.size()
assert 1 == originalProject.parent.size()
assert 1 == originalProject.pluginRepositories.size()
assert 1 == originalProject.repositories.size()
assert 1 == originalProject.prerequisites.size()
assert 1 == originalProject.properties.size()
assert 1 == originalProject.reporting.size()
assert 1 == originalProject.reports.size()
assert 1 == originalProject.scm.size()
assert 1 == originalProject.url.size()

File resolvedPom = new File(basedir, '.resolved-pom.xml')
assert resolvedPom.exists()

def resolvedProject = new XmlSlurper().parse(resolvedPom)
// required elements
assert '4.0.0' == resolvedProject.modelVersion.text()
assert 'com.github.mavenhaus.starter.its' == resolvedProject.groupId.text()
assert 'optional-elements-modeOssrh' == resolvedProject.artifactId.text()
assert '0.0.1-SNAPSHOT' == resolvedProject.version.text()
// banned elements for artifact
assert 0 == resolvedProject.build.size()
assert 0 == resolvedProject.ciManagement.size()
assert 0 == resolvedProject.contributors.size()
assert 0 == resolvedProject.dependencyManagement.size()
assert 1 == resolvedProject.description.size()
assert 1 == resolvedProject.developers.size()
assert 0 == resolvedProject.distributionManagement.size()
assert 0 == resolvedProject.issueManagement.size()
assert 0 == resolvedProject.mailingLists.size()
assert 0 == resolvedProject.name.size()
assert 0 == resolvedProject.organization.size()
assert 0 == resolvedProject.parent.size()
assert 0 == resolvedProject.pluginRepositories.size()
assert 1 == resolvedProject.repositories.size()
assert 0 == resolvedProject.prerequisites.size()
assert 0 == resolvedProject.properties.size()
assert 0 == resolvedProject.reporting.size()
assert 0 == resolvedProject.reports.size()
assert 1 == resolvedProject.scm.size()
assert 1 == resolvedProject.url.size()

