# CozyVillagers (Forge 1.8.9)

Project: CozyVillagers  
MC: 1.8.9  
Forge: 11.15.1.2318  
Java: 8 ONLY  
Gradle: 2.14.1 (wrapper pinned)

## 1) Verify Java 8
Open **Command Prompt**:

- `java -version`

You must see `1.8.0_xxx`.

## 2) One-time: generate gradle-wrapper.jar
This repo can’t include binaries. You already installed Gradle 2.14.1 — run from the project root:

- `gradle wrapper --gradle-version 2.14.1`

This creates:
- `gradle\wrapper\gradle-wrapper.jar`

## 3) Build / setup (Windows)
From project root:

- `gradlew.bat setupDecompWorkspace`
- `gradlew.bat idea`
- `gradlew.bat build`

Jar output:
- `build\libs\cozyvillagers-1.0.0.jar`

## 4) IntelliJ import (works first try)
1. IntelliJ -> File -> Open -> select the folder or `build.gradle`
2. Set:
   - Project SDK = **Java 1.8**
   - Gradle JVM = **Java 1.8**
3. After sync, use Gradle tasks:
   - `fg_runs -> runClient`
   - `fg_runs -> runServer`

## Commands
- `/cozy rep`   (look at a villager)
- `/cozy favor` (see active favor)