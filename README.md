JWebAssembly Gradle Plugin
======

[![License](https://img.shields.io/github/license/i-net-software/jwebassembly-gradle.svg)](https://github.com/i-net-software/jwebassembly/blob/master/LICENSE.txt)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=Plugin&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fde%2Finetsoftware%2Fjwebassembly%2Fde.inetsoftware.jwebassembly.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/de.inetsoftware.jwebassembly)

This is a Gradle plugin for the [JWebAssembly](https://github.com/i-net-software/JWebAssembly) compiler. A Java bytecode to [WebAssembly](http://webassembly.org/) converter. It produce the WASM and JavaScript file from your *.java, *.class and/or *.jar files.

## Usage

This plugin use the Java plugin to compile your Java sources to class files first. 

```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'gradle.plugin.de.inetsoftware:jwebassembly-gradle:+'
    }
}

apply plugin: 'de.inetsoftware.jwebassembly'

// declare your Java sources like you do it for your other Java projects
sourceSets {
    main {
        java {
            srcDir 'src'
        }
    }
}

wasm {
    format = 'Text'        // possible values are 'Text' and 'Binary'. 'Binary' is the default value.
    compilerVersion = 0.2  // specify a compiler version, default is '+'
    classpath = files(...) // specify libraries, default is sourceSet.compileClasspath
}
```
