# Trouble downloading teswiz from jitpack.io?

If you are getting an error similar to the one shown below when downloading teswiz from jitpack.io, the fix is
almost always to declare teswiz as a normal Gradle dependency — via JitPack's Maven repository — rather than
trying to download the jar file directly.

### Error when downloading teswiz from jitpack.io as part of your build:

```
* What went wrong:
Execution failed for task ':compileTestJava'.
> Could not resolve all files for configuration ':testCompileClasspath'.
    > Could not find com.github.anandbagmar:teswiz:1.0.10.
      Searched in the following locations:
        - file:/home/runner/.m2/repository/com/github/anandbagmar/teswiz/1.0.10/teswiz-1.0.10.pom
        - https://jitpack.io/com/github/anandbagmar/teswiz/1.0.10/teswiz-1.0.10.pom
      Required by:
          root project :
```

This almost always means either the version doesn't exist (check [teswiz releases](https://github.com/anandbagmar/teswiz/releases))
or the JitPack repository isn't declared in your `repositories` block.

## Refer to [build.gradle.sample](../../build.gradle.sample) for a complete reference

## [getting-started-with-teswiz](https://github.com/anandbagmar/getting-started-with-teswiz/blob/main/build.gradle) already has this set up for your reference

## Changes required in build.gradle:

### 1. Declare the JitPack repository

```groovy
repositories {
    mavenCentral()
    maven {
        url = 'https://jitpack.io'
    }
    mavenLocal()
}
```

### 2. Specify the teswiz version you want to use

Example:
```groovy
def teswizVersion = '1.0.31'
```

If you want the latest teswiz snapshot build instead of a tagged release, resolve the version from teswiz's
GitHub releases API first — see `build.gradle.sample` for a worked example that resolves either the latest
tagged release or a `SNAPSHOT` prerelease automatically.

### 3. Declare teswiz as a normal dependency

```groovy
dependencies {
    implementation "com.github.anandbagmar:teswiz:${teswizVersion}"
}
```

JitPack publishes proper POM/module metadata for teswiz, so this pulls in teswiz's full transitive dependency
graph automatically — no manual jar download or `fileTree` needed.

**Note:** teswiz only exposes a dependency transitively when its own public API needs it (see the
`api`/`implementation` split in teswiz's own `build.gradle`). If your project uses a library like log4j,
unirest, or the ReportPortal client directly in your own code — not just through teswiz — declare it as a
direct dependency in your own project too.

### 4. Support Multi-release JARs
You may need to add the following in your build.gradle file:

```groovy
    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }

    tasks.withType(JavaCompile).configureEach {
        options.compilerArgs += ['--release', '17']
    }

    tasks.withType(JavaExec).configureEach {
        javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
        }
    }
```

## Still stuck?

* Confirm the version actually exists as a [teswiz release](https://github.com/anandbagmar/teswiz/releases) or
  tag — JitPack can only build versions that exist as git tags.
* Try building the version on JitPack directly first: `https://jitpack.io/com/github/anandbagmar/teswiz/<version>/build.log`
  will show the build log if JitPack failed to build that tag.
* As a last resort for local development, you can build and publish teswiz to your local Maven cache yourself:
  `./gradlew publishToMavenLocal` from a local teswiz checkout, with `mavenLocal()` declared in your
  `repositories` block.
