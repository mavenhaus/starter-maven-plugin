# Starter Maven Plugin

This is the [starter-maven-plugin](https://github.com/mavenhaus/starter-maven-plugin/).
 
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mavenhaus/starter-maven-plugin.svg?label=License)](http://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.mavenhaus/starter-maven-plugin.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cstarter-maven-plugin)
[![Build Status](https://travis-ci.com/mavenhaus/starter-maven-plugin.svg?branch=master)](https://travis-ci.com/mavenhaus/starter-maven-plugin)
1.0.x branch: [![Build Status 1.0.x](https://travis-ci.com/mavenhaus/starter-maven-plugin.svg?branch=1.0.x)](https://travis-ci.com/mavenhaus/starter-maven-plugin)

## Quickstart
This plugin generates a resolved version of your pom.xml and makes maven to install and deploy this one instead of the original pom.xml.
```
  <build>
    <plugins>
      <plugin>
        <groupId>com.github.mavenhaus</groupId>
        <artifactId>starter-maven-plugin</artifactId>
        <!--<version>INSERT LATEST VERSION HERE</version>-->
        <executions>
          <execution>
            <goals>
              <goal>generate</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <!-- See usage on maven site from link above for details -->
        </configuration>
      </plugin>
    </plugins>
  </build>
```

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```
