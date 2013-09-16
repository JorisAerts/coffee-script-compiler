package com.jorisaerts.cscompiler.dependencies.exception;

import java.io.File;

@SuppressWarnings("serial")
public class CircularDependencyException extends RuntimeException {

	File sourceFile;
	File targetFile;

	public CircularDependencyException(File sourceFile, File targetFile) {
		super();
		this.sourceFile = sourceFile;
		this.targetFile = targetFile;
	}

	@Override
	public String getMessage() {
		return "Circular dependency exception occured between '" + sourceFile.getAbsoluteFile() + "' and '" + targetFile.getAbsoluteFile() + "'.";
	}

}
