package com.jorisaerts.cscompiler.compilers.exception;

@SuppressWarnings("serial")
public class CompileException extends Exception {

	public CompileException(Exception e, Integer line, Integer column, String code) {
		super(getMessage(e, line, column, code));
		setStackTrace(e.getStackTrace());
	}

	private static String getMessage(Exception e, Integer line, Integer column, String lineCode) {
		String msg = "Compile Exception";
		if (line != null) {
			msg += "\nline: " + line;
			if (column != null) {
				msg += "\ncolumn: " + column;
			}
		}
		if (lineCode != null) {
			msg += "\ncode: '" + lineCode + "'.";
		}
		msg += "\nmessage: '" + e.getLocalizedMessage() + "'.";
		return msg;
	}
}
