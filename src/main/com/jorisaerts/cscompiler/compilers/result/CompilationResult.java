package com.jorisaerts.cscompiler.compilers.result;

public interface CompilationResult {

	public abstract SourceMap getSourceMap();

	public abstract String getCode();

}