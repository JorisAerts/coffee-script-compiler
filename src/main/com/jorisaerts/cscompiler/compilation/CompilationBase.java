package com.jorisaerts.cscompiler.compilation;

import java.io.File;

import com.jorisaerts.cscompiler.dependencies.FileList;

public class CompilationBase {

	/** The default Compiler class (which uses Rhino) */
	protected final static String defaultCompilerClassName = "com.jorisaerts.cscompiler.compilers.RhinoCompiler";

	private boolean combine = true;
	private boolean minify = true;
	private boolean sourceMap = true;

	protected FileList inputFileList;
	protected File outputDirectory;

	public CompilationBase() {
		super();
	}

	/** Combine all scripts into 1 script */
	public boolean isCombine() {
		return combine;
	}

	/** Combine all scripts into 1 script */
	public void setCombine(boolean combine) {
		this.combine = combine;
	}

	/** Minify the output */
	public boolean isMinify() {
		return minify;
	}

	/** Minify the output */
	public void setMinify(boolean minify) {
		this.minify = minify;
	}

	/** Generate Source Map(s) */
	public boolean isSourceMap() {
		return sourceMap;
	}

	/** Generate Source Map(s) */
	public void setSourceMap(boolean sourceMap) {
		this.sourceMap = sourceMap;
	}

	/** List of files/directories which need to be compiled */
	public FileList getInputFileList() {
		return inputFileList;
	}

	/** List of files/directories which need to be compiled */
	public void setInputFileList(FileList inputFileList) {
		this.inputFileList = inputFileList;
	}

	/** Output folder for the compiled script(s). */
	public File getOutputDirectory() {
		return outputDirectory;
	}

	/** Output folder for the compiled script(s). */
	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

}