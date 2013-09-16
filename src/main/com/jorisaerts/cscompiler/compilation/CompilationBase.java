package com.jorisaerts.cscompiler.compilation;

import java.io.File;

import com.jorisaerts.cscompiler.dependencies.FileList;

public class CompilationBase {

	protected final static String defaultCompilerClassName = "com.jorisaerts.cscompiler.compilers.RhinoCompiler";

	private boolean combine = true;
	private boolean sourceMap = true;
	private boolean minify = true;

	public boolean isSourceMap() {
		return sourceMap;
	}

	public void setSourceMap(boolean sourceMap) {
		this.sourceMap = sourceMap;
	}

	public boolean isMinify() {
		return minify;
	}

	public void setMinify(boolean minify) {
		this.minify = minify;
	}

	protected FileList inputFileList;
	protected File outputDirectory;

	public CompilationBase() {
		super();
	}

	public FileList getInputFileList() {
		return inputFileList;
	}

	public void setInputFileList(FileList inputFileList) {
		this.inputFileList = inputFileList;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public boolean isCombine() {
		return combine;
	}

	public void setCombine(boolean combine) {
		this.combine = combine;
	}

}