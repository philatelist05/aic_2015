# README #
This repository will contain an implementation for the "Cloud-based Onion Routing" lab exercise of the Advanced Internet Computing course at TU Vienna.

## How do I get set up? ##

### Gradle ###

A [Gradle](https://gradle.org) wrapper is already contained in the repository. You can run it with `./gradlew`. Verify
that it's working by running `./gradlew tasks` in the project's root directory.

### IntelliJ ###

You should be able to import the project using IntelliJ by selecting the
build.gradle file in the import dialogue. Alternatively, running `./gradlew idea`
should generate IntelliJ project files that can be opened directly.

### Prerequisites / Dependencies ###

* Java 8 JDK (Recommended: Oracle JDK 8)
* [Apache Thrift](https://thrift.apache.org/) (>= 0.9.1)

Gradle will automatically download Gradle plugins and Java dependencies on its 
own (and on demand).

### How to run tests ###

Not yet, but using `gradle test`

### Deployment instructions ###

TODO