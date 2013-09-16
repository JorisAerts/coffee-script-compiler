CoffeeScript Compiler
=====================

A CoffeeScript compiler for Java, without [node.js](http://nodejs.org/) dependency, using the browser version of CoffeeScript.

Compilation is achieved using [Rhino](https://developer.mozilla.org/en/docs/Rhino), or using the native [javax.script](http://docs.oracle.com/javase/7/docs/api/index.html?javax/script/package-summary.html) package.


Goals:
------
- Command-line compilation
- Compilation using the .jar package
- Compilation as an ant-task

Todo:
------
- Improve the code :-)
- Add Source Map support