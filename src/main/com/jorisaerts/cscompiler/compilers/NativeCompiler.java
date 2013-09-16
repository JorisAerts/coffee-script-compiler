package com.jorisaerts.cscompiler.compilers;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.jorisaerts.cscompiler.helpers.FileIOHelper;

public class NativeCompiler extends CoffeeScriptCompiler {

	private final CompiledScript cscript;
	private final ScriptEngine engine;

	public NativeCompiler() throws Throwable {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		engine = scriptEngineManager.getEngineByName("JavaScript");
		engine.eval(FileIOHelper.getFileReader(COMPILER_FILENAME));

		Compilable compilingEngine = (Compilable) engine;
		cscript = compilingEngine.compile(FileIOHelper.getFileReader(COMPILER_FILENAME));

		Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		cscript.eval();
		cscript.eval(bindings);
	}

	public String compile(String coffeeScript) throws Throwable {
		Invocable invocable = (Invocable) cscript.getEngine();
		Object coffeeScriptObject = engine.get("CoffeeScript");
		String compiledScript = (String) invocable.invokeMethod(coffeeScriptObject, "compile", coffeeScript);
		return compiledScript;
	}

}
