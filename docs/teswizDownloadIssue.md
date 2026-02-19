# Trouble downloading teswiz from jitpack.io?

If you are getting an error similar to one shown below when downloading teswiz from jitpack.io, follow these instructions to change your build.gradle file to download it directly from GitHub instead.

### Error when downloading teswiz from jitpack.io as part of your build:

```
* What went wrong:
Execution failed for task ':compileTestJava'.
> Could not resolve all files for configuration ':testCompileClasspath'.
    > Could not find com.github.znsio:teswiz:1.0.10.
      Searched in the following locations:
        - file:/home/runner/.m2/repository/com/github/znsio/teswiz/1.0.10/teswiz-1.0.10.pom
        - https://jitpack.io/com/github/znsio/teswiz/1.0.10/teswiz-1.0.10.pom
      Required by:
          root project :
```

## Refer to [build.gradle.sample](../build.gradle.sample) for reference

## [getting-started-with-teswiz](https://github.com/znsio/getting-started-with-teswiz/blob/main/build.gradle) already has this change implemented for your reference

## Changes required in build.gradle:
### 1. Specify the teswiz version you want to use

Example:
```
    def teswizVersion = '1.0.15'
```

### 2. Define the folder where you want to download the teswiz as a jar dependency

Example:
```
    // Define the libs directory in the project root
    ext.libsDir = file("$projectDir/libs")
```

### 3. Define a new task that will use the teswizVersion specified in Step 1, and download it from the [teswiz release in Github](https://github.com/znsio/teswiz/releases)

Example: See the `downloadDependencies` task in [build.gradle](../build.gradle)

### 4. Ensure dependencies are downloaded before compiling

```
    tasks.compileJava {
        dependsOn downloadDependencies
        options.encoding = "UTF-8"
    }
```

### 5. Use the downloaded jar as a dependency for your gradle project

```
    dependencies {
        implementation fileTree(dir: "$project.projectDir/libs", include: ['*.jar'])
    }
```

### 6. Support Multi-release JARs
You may need to add the following in your build.gradle file:

```
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

## To force re-download of teswiz jar:

* Delete the directory or the downloaded jar, and then run the `./gradlew clean run` command. This will automatically re-download the teswiz jar for you.
