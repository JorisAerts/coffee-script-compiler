package com.jorisaerts.cscompiler.minify;

public abstract class Minifier implements AutoCloseable {

	public abstract String minify(String javaScript) throws Throwable;

	/** Closes the current instance of CoffeeScriptCompiler */
	public void close() {
	}

}