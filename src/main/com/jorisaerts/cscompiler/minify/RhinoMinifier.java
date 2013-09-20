package com.jorisaerts.cscompiler.minify;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import com.jorisaerts.cscompiler.helpers.FileIOHelper;

// TODO: minify using Java syntax!
public class RhinoMinifier extends Minifier implements AutoCloseable {

	private static String FILENAME = "./js/uglifyjs.js";

	Context cx;
	Scriptable scope;
	NativeObject minifyJSObject;
	Function minifyFunction;

	public RhinoMinifier() throws Throwable {
		cx = Context.enter();
		cx.setOptimizationLevel(9);
		cx.setLanguageVersion(Context.VERSION_1_7);
		cx.setGeneratingSource(false);

		scope = cx.initStandardObjects();

		StringBuilder sb = new StringBuilder().append(FileIOHelper.readFile(FILENAME))

		.append("function minify(script){").append("var ast = UglifyJS.parse(script);").append("ast.figure_out_scope();").append("var compressor = UglifyJS.Compressor({ });")
				.append("ast = ast.transform(compressor);").append("ast.compute_char_frequency();").append("ast.mangle_names();").append("return ast.print_to_string();").append("}");

		// cx.evaluateReader(scope, FileIOHelper.getFileReader(FILENAME),
		// "<cmd>", 1, null);
		cx.evaluateString(scope, sb.toString(), "<cmd>", 1, null);

		minifyJSObject = (NativeObject) scope.get("UglifyJS", scope);
		minifyFunction = (Function) scope.get("minify", scope);
	}

	public String minify(String javaScript) throws Throwable {
		Object functionArgs[] = { javaScript };
		Function f = (Function) minifyFunction;
		Object result = f.call(cx, scope, scope, functionArgs);
		return Context.toString(result);
	}

	// public String minify(String javaScript) throws Throwable {
	//
	// Function f;
	//
	// f = (Function) minifyJSObject.get("Compressor", scope);
	// NativeObject compressor = (NativeObject) f.call(cx, scope, scope, new
	// Object[0]);
	//
	// f = (Function) minifyJSObject.get("parse", scope);
	// NativeObject ast = (NativeObject) f.call(cx, scope, scope, new Object[] {
	// javaScript });
	//
	// Scriptable astProto = ast.getPrototype();
	// f = (Function) astProto.get("figure_out_scope", astProto);
	// f.call(cx, scope, astProto, new Object[0]);
	// //
	// // f = (Function) ast.get("transform", scope);
	// // f.call(cx, scope, scope, new Object[] { compressor });
	// //
	// // f = (Function) ast.get("compute_char_frequency", scope);
	// // f.call(cx, scope, scope, new Object[0]);
	// //
	// // f = (Function) ast.get("mangle_names", scope);
	// // f.call(cx, scope, scope, new Object[0]);
	// //
	// // f = (Function) ast.get("print_to_string", scope);
	// // Object result = f.call(cx, scope, scope, new Object[0]);
	// //
	// // return Context.toString(result);
	// return "";
	// }

	@Override
	public void close() {
		Context.exit();
	}

}
