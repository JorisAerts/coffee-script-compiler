package com.jorisaerts.cscompiler.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class LogStream extends PrintStream {

	public LogStream() {
		super(new ByteArrayOutputStream());
	}

}
