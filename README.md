# LocalLib
A Gradle plugin that makes composite builds and dependency substitution easy

It enables composite build in local.properties so its easy to disable without changing project structure.
It can also replace implemetation declared in any module build.gradle

Current version: 1.0.0-SNAPSHOT

**Quick Setup:**

```Gradle
//######## Root settings.gradle

buildscript {
    repositories {
        maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
        jcenter()
    }
    dependencies {
        classpath 'com.github.xpenatan:LocalLib:{VERSION}'
    }
}

apply plugin: 'locallib'

...

include (your project modules)

```

```Gradle
//######## Root local.properties

locallib.includeBuild = [ProjectDir 1], [ProjectDir 2], ...
locallib.replace =\
[implementation lib 1] = [Replace 1]\
[implementation lib 2] = [Replace 2]


// Example:

locallib.includeBuild = D:\\Libgdx
locallib.replace = \
com.badlogicgames.gdx:gdx-platform:1.9.10-SNAPSHOT:natives-desktop = files("C:\\Libgdx\\gdx\\libs\\gdx-natives.jar"),\
com.badlogicgames.gdx:gdx-bullet-platform:1.9.10-SNAPSHOT:natives-desktop = files("D:\\Libgdx\\extensions\\gdx-bullet\\libs\\gdx-bullet-natives.jar"),\
com.badlogicgames.gdx:gdx-box2d-platform:1.9.10-SNAPSHOT:natives-desktop = files("D:\\Libgdx\\extensions\\gdx-box2d\\gdx-box2d\\libs\\gdx-box2d-natives.jar")
```

**Supported substitution**

```
files("[DIR]") or files([DIR])

// Will support later:
project(":[Name])"
"[implementation lib]"
```