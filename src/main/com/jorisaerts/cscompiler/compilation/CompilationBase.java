package com.jorisaerts.cscompiler.compilation;

import java.io.File;

import com.jorisaerts.cscompiler.dependencies.FileList;

public class CompilationBase {

	protected static String defaultCompilerClassName = "com.jorisaerts.cscompiler.compilers.RhinoCompiler";
	private boolean combine = true;
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