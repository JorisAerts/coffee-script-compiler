package com.jorisaerts.cscompiler.compilers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import com.jorisaerts.cscompiler.compilers.result.CompilationResult;
import com.jorisaerts.cscompiler.helpers.FileIOHelper;

public abstract class CoffeeScriptCompiler implements AutoCloseable {

	protected static boolean init = false;

	protected final static List<String> COMPILER_FILENAMES = Arrays.asList(new String[] { "./js/CoffeeScript/coffee-script.js", "./js/SourceMap/source-map.js", "./js/UglifyJS2/uglifyjs.js" });

	public abstract void add(String coffeeScript) throws Throwable;

	public void add(File filename) throws Throwable {
		add(FileIOHelper.readFile(filename));
	}

	public abstract List<CompilationResult> compile();

	/** Closes the current instance of CoffeeScriptCompiler */
	public void close() {
	}

	public String getScript() throws FileNotFoundException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, size = COMPILER_FILENAMES.size(); i < size; i++) {
			sb.append(FileIOHelper.getFileReader(COMPILER_FILENAMES.get(i))).append("\n");
		}
		return sb.toString();
	}
}
