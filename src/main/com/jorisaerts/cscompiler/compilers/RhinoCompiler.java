package com.jorisaerts.cscompiler.compilers;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import com.jorisaerts.cscompiler.compilers.result.CompilationResult;
import com.jorisaerts.cscompiler.compilers.result.CompilationResultImpl;

public class RhinoCompiler extends CoffeeScriptCompiler {

	private Context cx;
	Scriptable scope;
	Function addFunction;
	Function compileFunction;

	public RhinoCompiler() throws Throwable {
		cx = Context.enter();
		cx.setOptimizationLevel(9);
		cx.setLanguageVersion(Context.VERSION_1_7);
		cx.setGeneratingSource(false);

		scope = cx.initStandardObjects();

		cx.evaluateString(scope, getScript(), "compiler", 1, null);

		Object coffeeScriptObject = scope.get("CSCompiler", scope);
		NativeObject nativeCompiler = (NativeObject) coffeeScriptObject;
		addFunction = (Function) nativeCompiler.get("add", scope);
		compileFunction = (Function) nativeCompiler.get("compile", scope);
	}

	@Override
	public void add(String coffeeScript) throws Throwable {
		Object functionArgs[] = {};
		((Function) addFunction).call(cx, scope, scope, functionArgs);
	}

	@Override
	public List<CompilationResult> compile() {
		Object functionArgs[] = {};
		Object result = ((Function) addFunction).call(cx, scope, scope, functionArgs);
		return processResult(result);
	}

	private List<CompilationResult> processResult(Object result) {
		List<CompilationResult> resultList = new ArrayList<CompilationResult>();
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
