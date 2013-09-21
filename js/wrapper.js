/**

Copyright (c) 2013 Joris Aerts

This wrapper object is used to process (compile, minify, extract CoffeeScript Helpers) 
all individual scripts, and glue them together at the end.

Results are stored in the object itself, because for each compilation / minification process,
a new environment is initialized. If the same environment is reused, use the init-function
to clean the old environment

This script is loaded through the Java Rhino API, so Java-classes can be referenced to
(ea. for output)

*/

window.CSCompiler = (function(ret){
	
	var 
		options = {},
		output = [],
		defaultOptions ={ 
			combine: true,
			compress: true,
			mangleNames: true,
			sourceMap: true
		},
		coffeeScriptHelpers = {},
		
		
		Output,
		CompilationResult
		;
	
	/** Output - renders the script according to the options */
	Output = function Output(options){
		this.options = options;
		this.contents = [];
	};
	Output.prototype = {
		compile: function(){
			result = "";
			for(var i=0, size = this.contents.length; i<size; i++){
				result += this.content[i].compile();
			}
			return result;
		}
	};
	
	
	/** One individual render item. */	
	CompilationResult = function CompilationResult(script, options){
		this.options = options;
		this.csHelpers = {
			fragments: [{ start:0, stop:0 }],
		};
		this.sourceMaps = {
			v1: script.sourceMap,
			v3: script.v3SourceMap	
		};
		this.fragments = script.fragments;
		this.source = script.source;
		this.js = script.js;
	};
	CompilationResult.prototype = {
		compile: function(){
			var options = this.options,
				result = "";
				
			if(options.combine === true){
				
			}else{
				if(options.compress === true){
					
				}
				if(options.mangleNames === true){
					
				}
			}
		}
	};
	
	
	/** Initialization function */
	function init(opts){
		options = defaults(opts, defaultOptions);	
		initCoffeeReserveds();
		output = [];
		return ret;
	}
		
	function initCoffeeReserveds() {
		coffeeScriptHelpers = function(ret, words){
			for (var i=0, size=words.length; i<size; i++){
				ret[words[i]] = { rx: new RegExp("__" + words[i] + "\\s+\\=\\s+(.*)?(\\,\\n)|$"), found:false }
			}
			return ret;
		}({},["bind", "hasProp", "extends", "slice"])
	}
		
	function processFragment(f, result) {
		var r,m,w;
		for (w in coffeeScriptHelpers){
			r = coffeeScriptHelpers[w];
			r.found = !r.found && f.code.match(r.rx);
			if(r.rx.test(r.code)){
				r.found = true;
				m = f.code.match(r.rx);
				if (m.length){
					// forbidden
				}
			}
		}
		return coffeeScriptHelpers;
	}
		
	function extractCoffeeScriptHelpers(script) {
		for (var f, i=0, size = script.fragments.length; i<size; i++){
			f = script.fragments[i];
			if(/__bind|__hasProp|__extend|__slice/.test(f.code)){
				console.log(f);
				var cR = processFragment(f,script),
					q = 1;
					if (/^\s*?;\s*?$/.test(script.fragments[i+1].code)){
						q++;
					if (/^\s*?var\s*?$/.test(script.fragments[i-1].code)){
						q++; i--;
					}
				}
				script.fragments.splice(i, q);
				break;
			}
		}
	}
	
	function hasProp( obj, prop) {
		return obj.hasOwnProperty(prop);
	}
	
	function defaults(obj){
		obj = obj || {};
		for(var trg, p, i=1, size = arguments.length; i<size; i++){
			trg = arguments[i];
			for (p in trg){
				if(hasProp(trg, p) && !typeof(obj[p]) === "undefined"){
					obj[p] = trg[p];
				}
			}
		}
		return obj;
	}
	
	// minify using UglifyJS
	function minify(script, options){
		options = options || {};
		var ast = UglifyJS.parse(script),
			compressor;
		
		ast.figure_out_scope();
		
		if(options.compress !== false){
			compressor = UglifyJS.Compressor({ });
			ast = ast.transform(compressor);
		}
		
		if(options.mangleNames !== false){
			ast.compute_char_frequency();
			ast.mangle_names();
		}
		
		return ast.print_to_string();	
	}
	
	// wrap a script into a self-executing anonymous function
	function wrapScript(script){ 
		return 	"(function(){" + 
					script + 
				"\n})()"; 
	}
	
	(function hackTheCoffee(){
		// TODO:
		// write coffeeScript hack to preserve the fragments
		// currently this is done in the "hacked" coffees-cript.js itself	
	})();
	
	
	// process a single coffee-script file
	ret.add = function(script){
		var result = CoffeeScript.compile(script, {
			sourceMap: true, 
			bare: true			// use the bare option, we'll wrap it ourself
		});
		result.source = script;
		for (var f, i=0, size = result.fragments.length; i<size; i++){
			f = result.fragments[i];
			if(/__bind|__hasProp|__extend|__slice/.test(f.code)){
				console.log(f);
				var cR = processFragment(f,result),
					q = 1;
					
				console.log (cR);	
				
				if (/^\s*?;\s*?$/.test(result.fragments[i+1].code) && q++){
					if (/^\s*?var\s*?$/.test(result.fragments[i-1].code) && q++){
						--i;
					}
				}
				result.fragments.splice(i, q);
				break;
			}
		}
		
		output.push(result);
		return result;		
	};
	
	// wrap and minify all scripts into one
	// combine all source maps into one
	ret.compile = function(){
		return output;
	};
	
	// initialization
	ret.reInitialize = init;
	
	// return the object
	return init();
	
})({});