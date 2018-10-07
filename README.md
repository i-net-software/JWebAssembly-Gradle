JWebAssembly Gradle Plugin
======

[![License](https://img.shields.io/github/license/i-net-software/jwebassembly-gradle.svg)](https://github.com/i-net-software/jwebassembly/blob/master/LICENSE.txt)

This is a Gradle plugin for the [JWebAssembly](https://github.com/i-net-software/JWebAssembly) compiler. A Java to [WebAssembly](http://webassembly.org/) converter.

## Usage

This plugin use the Java plugin to compile your Java sources to class files first. 

```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'de.inetsoftware:jwebassembly-gradle:+'
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
    compilerVersion = 0.1  // specify a compiler version, default is '+'
}
```