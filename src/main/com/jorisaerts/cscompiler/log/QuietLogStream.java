package com.jorisaerts.cscompiler.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public final class QuietLogStream extends PrintStream {

	public QuietLogStream() {
		super(new ByteArrayOutputStream());
	}

}
