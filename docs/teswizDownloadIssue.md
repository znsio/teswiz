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
    buildscript {
        ext {
            gradleVersion = '8.11.1'
            teswizVersion = '1.0.13'
        }
        repositories {
            mavenLocal()
        }
    }
```

### 2. Define the folder where you want to download the teswiz as a jar dependency

Example:
```
    // Define the libs directory in the project root
    ext.libsDir = file("$projectDir/libs")
```

### 3. Define a new task that will use the teswizVersion specified in Step 1, and download it from the [teswiz release in Github](https://github.com/znsio/teswiz/releases)

Example:
```
    def downloadDependency(String name, String type, Map<String, String> params) {
        if (!libsDir.exists()) {
            libsDir.mkdirs()
        }
    
        println "Download dependency: ${name} from ${type}"
        def jarFile = new File(libsDir, "${name}-${params.version}.jar")
        if (jarFile.exists()) {
            println "\tDependency $name already exists at: $jarFile. No need to redownload it"
            return jarFile
        }
    
        def jarUrl
        if (type == "github") {
            println "\tFetching latest GitHub release information for ${params.repoUrl}..."
            def jsonResponse = new URL(params.repoUrl).text
            def jsonSlurper = new groovy.json.JsonSlurper()
            def releaseInfo = jsonSlurper.parseText(jsonResponse)
    
            // Ensure assets field is a list and each asset is a Map
            def assets = releaseInfo.assets
            if (!(assets instanceof List)) {
                throw new GradleException("Assets field is not a list: $assets")
            }
    
            def jarAsset = releaseInfo.assets.find { it.name.matches(/${name}-\d+\.\d+\.\d+\.jar/) }
            if (!jarAsset) {
                throw new GradleException("No ${name} JAR file found in the latest GitHub release.")
            }
            jarUrl = jarAsset.browser_download_url
        } else if (type == "jitpack") {
            jarUrl = "https://jitpack.io/${params.group.replace('.', '/')}/${params.artifact}/${params.version}/${params.artifact}-${params.version}${params.fileNameSuffix}.jar"
        } else {
            throw new GradleException("Unknown type: $type")
        }
    
        println "\tDownloading ${name} JAR from ${jarUrl}"
        def downloadCommand
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            downloadCommand = ["cmd", "/c", "curl", "-o", jarFile.absolutePath, jarUrl]
        } else {
            downloadCommand = ["wget", "-O", jarFile.absolutePath, jarUrl]
        }
        // Explicitly convert all elements to String
        downloadCommand = downloadCommand.collect { it.toString() }
    
        println "\tDownloading using command: ${downloadCommand}"
    
        def process = new ProcessBuilder(downloadCommand).redirectErrorStream(true).start()
        process.inputStream.eachLine { println it }
        process.waitFor()
    
        if (process.exitValue() != 0) {
            throw new GradleException("Failed to download ${name} JAR.")
        }
    
        println "${name} JAR downloaded to $jarFile"
        return jarFile
    }
    
    // Define a custom task to download dependencies
    task downloadDependencies {
        doLast {
            println "Downloading required dependencies..."
            def dependencies = [
                    [
                            name  : "teswiz",
                            type  : "github",
                            params: [
                                    repoUrl: "https://api.github.com/repos/znsio/teswiz/releases/tags/$project.teswizVersion",
                                    artifact: "teswiz",
                                    version : "$project.teswizVersion",
                                    fileNameSuffix: ""
                            ]
                    ]
            ]
    
            println "\n---------------------------------------------"
    
            dependencies.each { dep ->
                println "Processing dependency: ${dep.name}"
                downloadDependency(dep.name, dep.type, dep.params)
                println "\n---------------------------------------------"
    
            }
        }
    }
```

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
