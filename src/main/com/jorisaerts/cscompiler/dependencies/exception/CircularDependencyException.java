package com.jorisaerts.cscompiler.dependencies.exception;

import java.io.File;

public class CircularDependencyException extends RuntimeException {

	private static final long serialVersionUID = 3525879675248343486L;
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
