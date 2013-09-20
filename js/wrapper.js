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

window.Joris = (function(ret){
	
	var 
		options = {},
		output = [],
		defaultOptions ={ 
			minify: true,
			sourceMap: true
		},
		coffeeScriptHelpers = {};
	
	function init(){
		initCoffeeReserveds();
		options = {};
		output = [];
		return ret;
	}
		
	function initCoffeeReserveds() {
		coffeeScriptHelpers = function(ret, words){
			for (var i=0, size=words.length; i<size; i++){
				ret[words[i]] = { rx: new RegExp("__" + words[i] + "(.*)?(\\,\\n)|$"), found:false }
			}
			return ret;
		}({},["bind", "hasProp", "extends", "slice"])
	}
		
	function processFragment(f, result) {
		var r,m,w;
		for (w in coffeeReserveds){
			r = coffeeReserveds[w];
			r.found = !r.found && f.code.match(r.rx);
			if(r.rx.text(r.code)){
				r.found = true;
				m = f.code.match(r.rx);
			}
		}
		return coffeeReserveds;
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
	
	function defaults(){
		var obj = arguments[0] || {}, trg;
		for(var p, i=1, size = arguments.length; i<size; i++){
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
	function minify(script){
		var ast = UglifyJS.parse(script),
			compressor = UglifyJS.Compressor({ });
		
		ast.figure_out_scope();
		ast = ast.transform(compressor);
		ast.compute_char_frequency();
		ast.mangle_names();
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
	ret.process = function(script, options){
		ret.options = defaults(options, defaultOptions);	
		var result = CoffeeScript.compile({
			sourceMap: true, 
			bare: true			// use the bare option, we'll wrap it ourself
		});
		ret.output.push(script);
		return script;		
	};
	
	// wrap and minify all scripts into one
	// combine all source maps into one
	ret.wrapUp = function(options){
		options = defaults(options, { 
			combine: true
		});
	};
	
	// initialization
	ret.reInitialize = init;
	
	// return the object
	return init();
	
})({});