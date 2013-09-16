package com.jorisaerts.cscompiler.compilation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.jorisaerts.cscompiler.compilers.CoffeeScriptCompiler;
import com.jorisaerts.cscompiler.dependencies.DependencyList;
import com.jorisaerts.cscompiler.dependencies.FileList;
import com.jorisaerts.cscompiler.helpers.FileHelper;
import com.jorisaerts.cscompiler.helpers.FileIOHelper;

public class Compilation extends CompilationBase {

	final Class<CoffeeScriptCompiler> Compiler;

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

	public void compile() {
		FileList fileList = new DependencyList();
		fileList.addAll(getInputFileList());
		try (CoffeeScriptCompiler compiler = Compiler.newInstance()) {
			if (isCombine()) {
				compileCombined(compiler, fileList, FileHelper.getJavaScriptFromCoffeeFile(fileList.get(fileList.size() - 1)));
			} else {
				compile(compiler, fileList);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private void compile(CoffeeScriptCompiler compiler, FileList fileList) throws Throwable {
		String compiledScript;
		for (File file : FileHelper.getCoffeeScriptFiles(fileList)) {
			compiledScript = compiler.compile(file);
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
		compiledScript = compiler.compile(combinedScript.toString());
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