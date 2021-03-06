package com.jorisaerts.cscompiler.compilers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.jorisaerts.cscompiler.compilers.exception.CompileException;
import com.jorisaerts.cscompiler.compilers.result.CompilationResultList;
import com.jorisaerts.cscompiler.helpers.FileIOHelper;

public abstract class CoffeeScriptCompiler implements AutoCloseable {

	protected static boolean init = false;

	protected final static List<String> COMPILER_FILENAMES = Arrays.asList(new String[] { "./js/CoffeeScript/coffee-script.js", "./js/SourceMap/source-map.js", "./js/UglifyJS2/uglifyjs.js",
			"./js/wrapper.js" });

	public abstract void add(String coffeeScript, File file) throws Throwable;

	public void add(File file) throws Throwable {
		add(FileIOHelper.readFile(file), file);
	}

	public abstract CompilationResultList compile() throws CompileException;

	/** Closes the current instance of CoffeeScriptCompiler */
	public void close() {
	}

	public String getScript() throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, size = COMPILER_FILENAMES.size(); i < size; i++) {
			sb.append(FileIOHelper.readFile(COMPILER_FILENAMES.get(i))).append("\n;\n");
		}
		return sb.toString();
	}
}
