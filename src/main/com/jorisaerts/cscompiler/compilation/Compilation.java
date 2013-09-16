package com.jorisaerts.cscompiler.compilation;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import com.jorisaerts.cscompiler.compilers.CoffeeScriptCompiler;
import com.jorisaerts.cscompiler.dependencies.DependencyList;
import com.jorisaerts.cscompiler.dependencies.FileList;
import com.jorisaerts.cscompiler.helpers.FileHelper;
import com.jorisaerts.cscompiler.helpers.FileIOHelper;
import com.jorisaerts.cscompiler.log.QuietLogStream;

public class Compilation extends CompilationBase {

	/** The PrintStream to which is traced. */
	public PrintStream out = new QuietLogStream();
	private final Class<CoffeeScriptCompiler> Compiler;

	public Compilation(Class<CoffeeScriptCompiler> compiler, List<File> inputFileList) {
		this.Compiler = compiler;
		setInputFileList(resolveAsFileList(inputFileList));
	}

	public Compilation(Class<CoffeeScriptCompiler> compiler, List<File> inputFileList, File outputDirectory) {
		this(compiler, inputFileList);
		setOutputDirectory(outputDirectory);
	}

	@SuppressWarnings("unchecked")
	public Compilation(List<File> inputFileList) throws ClassNotFoundException {
		this((Class<CoffeeScriptCompiler>) Class.forName(defaultCompilerClassName), inputFileList);
	}

	public Compilation(File inputFile, File outputDirectory) throws ClassNotFoundException {
		this(Arrays.asList(new File[] { inputFile }));
		setOutputDirectory(outputDirectory);
	}

	private FileList resolveAsFileList(List<File> fileList) {
		FileList resultList = new FileList();
		for (File file : fileList) {
			if (file.isDirectory()) {
				resultList.addAll(resolveAsFileList(FileHelper.getCoffeeFiles(file)));
			} else {
				resultList.add(file);
			}
		}
		return resultList;
	}

	private FileList resolveDependencies() {
		out.print("Resolving dependencies...");
		FileList fileList = new DependencyList();
		fileList.addAll(getInputFileList());
		out.println(" done.");
		return fileList;
	}

	/** Start the compilation process. */
	public void compile() {
		long starttime = System.currentTimeMillis();
		FileList fileList = resolveDependencies();
		try (CoffeeScriptCompiler compiler = Compiler.newInstance()) {
			if (isCombine()) {
				compileCombined(compiler, fileList, FileHelper.getJavaScriptFromCoffeeFile(fileList.get(fileList.size() - 1)));
			} else {
				compile(compiler, fileList);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		long totaltime = System.currentTimeMillis() - starttime;

		out.println("Compilation done in " + totaltime / 1000.0 + " seconds.");
	}

	private void compile(CoffeeScriptCompiler compiler, FileList fileList) throws Throwable {
		String compiledScript;
		for (File file : FileHelper.getCoffeeScriptFiles(fileList)) {

			out.print("Compiling '" + file + "'...");
			compiledScript = compiler.compile(file);
			out.println(" done.");

			File targetFile = FileHelper.getJavaScriptFromCoffeeFile(file);
			FileIOHelper.writeFile(targetFile, compiledScript);
		}
	}

	private void compileCombined(CoffeeScriptCompiler compiler, FileList fileList, File targetFile) throws Throwable {
		String compiledScript;
		StringBuffer combinedScript = new StringBuffer();
		for (File file : FileHelper.getCoffeeScriptFiles(fileList)) {
			try {
				combinedScript.append(FileIOHelper.readFile(file)).append("\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		out.print("Compiling...");
		compiledScript = compiler.compile(combinedScript.toString());
		out.println(" done.");

		combinedScript = new StringBuffer();
		for (File file : FileHelper.getJavaScriptFiles(fileList)) {
			try {
				combinedScript.append(FileIOHelper.readFile(file)).append("\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		FileIOHelper.writeFile(targetFile, combinedScript.toString() + compiledScript);
	}
}