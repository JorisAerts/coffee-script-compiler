package com.jorisaerts.cscompiler.compilers;

import java.io.File;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.jorisaerts.cscompiler.compilers.exception.CompileException;
import com.jorisaerts.cscompiler.compilers.result.CompilationResultList;

public class NativeCompiler extends CoffeeScriptCompiler {

	private final CompiledScript cscript;
	private final ScriptEngine engine;

	public NativeCompiler() throws Throwable {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		engine = scriptEngineManager.getEngineByName("JavaScript");

		Compilable compilingEngine = (Compilable) engine;
		cscript = compilingEngine.compile(getScript());

		Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		cscript.eval();
		cscript.eval(bindings);
	}

	public void add(String coffeeScript, File file) throws Throwable {
		Invocable invocable = (Invocable) cscript.getEngine();
		Object coffeeScriptObject = engine.get("CoffeeScript");
		String compiledScript = (String) invocable.invokeMethod(coffeeScriptObject, "compile", coffeeScript);
		// return compiledScript;
	}

	@Override
	public CompilationResultList compile() throws CompileException {
		return null;
	}

}
