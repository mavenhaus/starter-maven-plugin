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
assert '${revision}${sha1}${changelist}' == originalProject.version.text()
assert '${project.groupId}' == originalProject.dependencyManagement.dependencies.dependency[0].groupId.text()
assert '${artifact.name}' == originalProject.dependencyManagement.dependencies.dependency[0].artifactId.text()
assert '${project.version}' == originalProject.dependencyManagement.dependencies.dependency[0].version.text()
assert 1 == originalProject.build.size()
assert 1 == originalProject.build.defaultGoal.size()
assert 1 == originalProject.build.plugins.size()
assert 1 == originalProject.build.plugins.plugin.size()


File resolvedPom = new File(basedir, '.resolved-pom.xml')
assert resolvedPom.exists()

def resolvedProject = new XmlSlurper().parse(resolvedPom)
assert '${revision}${sha1}${changelist}' == resolvedProject.version.text()
assert '${project.groupId}' == resolvedProject.dependencyManagement.dependencies.dependency[0].groupId.text()
assert '${artifact.name}' == resolvedProject.dependencyManagement.dependencies.dependency[0].artifactId.text()
assert '${project.version}' == resolvedProject.dependencyManagement.dependencies.dependency[0].version.text()
assert 1 == resolvedProject.build.size()
assert 1 == resolvedProject.build.defaultGoal.size()
assert 1 == resolvedProject.build.plugins.size()
assert 1 == resolvedProject.build.plugins.plugin.size()
