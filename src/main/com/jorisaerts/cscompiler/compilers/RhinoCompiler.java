package com.jorisaerts.cscompiler.compilers;

import java.io.File;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

import com.jorisaerts.cscompiler.compilers.exception.CompileException;
import com.jorisaerts.cscompiler.compilers.result.CompilationResultImpl;
import com.jorisaerts.cscompiler.compilers.result.CompilationResultList;
import com.jorisaerts.cscompiler.helpers.FileHelper;

public class RhinoCompiler extends CoffeeScriptCompiler {

	private Context cx;
	ScriptableObject scope;
	Function addFunction;
	Function compileFunction;

	public RhinoCompiler() throws Throwable {
		cx = Context.enter();
		cx.setOptimizationLevel(9);
		cx.setLanguageVersion(Context.VERSION_1_7);
		cx.setGeneratingSource(false);

		Script script = cx.compileString(getScript(), "compiler", 1, null);
		scope = cx.initStandardObjects();
		script.exec(cx, scope);

		Object coffeeScriptObject = scope.get("CSCompiler", scope);
		NativeObject nativeCompiler = (NativeObject) coffeeScriptObject;

		addFunction = (Function) nativeCompiler.get("add", scope);
		compileFunction = (Function) nativeCompiler.get("compile", scope);
	}

	@Override
	public void add(String coffeeScript, File file) throws Throwable {
		Object functionArgs[] = { coffeeScript, file.getAbsolutePath() + "", FileHelper.isCoffeeScriptFile(file) ? "coffee" : "javascript" };
		((Function) addFunction).call(cx, scope, scope, functionArgs);
	}

	@Override
	public CompilationResultList compile() throws CompileException {
		try {
			Object functionArgs[] = {};
			Object result = ((Function) compileFunction).call(cx, scope, scope, functionArgs);
			return processResult(result);
		} catch (JavaScriptException e) {
			throw new CompileException(e, e.lineNumber(), e.columnNumber(), e.lineSource());
		}
	}

	private CompilationResultList processResult(Object result) {
		CompilationResultList resultList = new CompilationResultList();
		CompilationResultImpl impl = new CompilationResultImpl();
		impl.setCode(Context.toString(result));
		resultList.add(impl);
		return resultList;
	}

	@Override
	public void close() {
		Context.exit();
	}

}
