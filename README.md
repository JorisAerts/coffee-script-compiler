# CoffeeScript Compiler

A CoffeeScript compiler for Java, without [node.js](http://nodejs.org/) dependency, using the browser version of CoffeeScript.  
Compilation is achieved using [Rhino](https://developer.mozilla.org/en/docs/Rhino), or using the native [javax.script](http://docs.oracle.com/javase/7/docs/api/index.html?javax/script/package-summary.html) package.

## Usage:

To define dependencies in a CoffeeScript file to other CoffeeScript files or JavaScript files, just add a special comment:
```
#= require "file.coffee"
#= require "jquery.js"

<your code>
```
If any circular dependency is detected, an CircularDependencyException will be thrown.

## Goals:
* Command-line compilation
* Compilation using the .jar package
* Compilation as an ant-task

## Todo:
* Improve the code.   
  * Concatenation of all scripts before compilation takes a very long time.
  * A compiler/minifier wrapper needs to be written so that every file can still be compiled/minified separatley, but the CoffeeScript helper functions should be removed and added at the end, so that there is no duplicate code.
* Add Source Map support
