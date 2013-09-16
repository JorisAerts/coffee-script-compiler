package com.jorisaerts.cscompiler.compilers;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import com.jorisaerts.cscompiler.helpers.FileIOHelper;

public class RhinoCompiler extends CoffeeScriptCompiler {

	private Context cx;
	Scriptable scope;
	Function compileFunction;

	public RhinoCompiler() throws Throwable {
		cx = Context.enter();
		cx.setOptimizationLevel(9);
		cx.setLanguageVersion(Context.VERSION_1_7);
		cx.setGeneratingSource(false);

		scope = cx.initStandardObjects();
		cx.evaluateReader(scope, FileIOHelper.getFileReader(COMPILER_FILENAME), "coffee", 1, null);

		NativeObject coffeeScriptObject = (NativeObject) scope.get("CoffeeScript", scope);
		compileFunction = (Function) coffeeScriptObject.get("compile", scope);
	}

	public String compile(String coffeeScript) throws Throwable {
		Object functionArgs[] = { coffeeScript };
		Function f = (Function) compileFunction;
		Object result = f.call(cx, scope, scope, functionArgs);
		return Context.toString(result);
	}

	@Override
	public void close() {
		Context.exit();
	}

}
