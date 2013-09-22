/**
 * 
 * Copyright (c) 2013 Joris Aerts
 * 
 * This wrapper object is used to process (compile, minify, extract CoffeeScript
 * Helpers) all individual scripts, and glue them together at the end.
 * 
 * Results are stored in the object itself, because for each compilation /
 * minification process, a new environment is initialized. If the same
 * environment is reused, use the init-function to clean the old environment
 * 
 * This script is loaded through the Java Rhino API, so Java-classes can be
 * referenced to (ea. for output)
 * 
 */

(function(global) {
	
	global.MOZ_SourceMap = sourceMap;
	global.CSCompiler = (function(ret) {
		
		var options = {}, output = [], defaultOptions = {
		    bare : false,
		    combine : true,
		    compress : true,
		    mangleNames : true,
		    sourceMap : true
		}, coffeeScriptHelpers = {},

		Output, OutputScript, OutputJavaScript, OutputCoffeeScript,

		rxJavaScript = /(function|var)/g, rxCoffeeScript1 = /(class|extends|of|when|isnt)/g, rxCoffeeScript2 = /(\-\>|\=\>|^#)/g,

		isUndef = function(obj) {
			return typeof (obj) === "undefined";
		},

		trace = function() {
			if (!isUndef(global.java) && !isUndef(java.lang) && !isUndef(java.lang.System)) {
				return function(obj) {
					java.lang.System.out.println("" + obj)
				};
			} else {
				return function(obj) {
					console.log(obj);
				}
			}
		}();
		
		/** Output - renders the script according to the options */
		Output = function Output(options) {
			this.options = defaults({}, options);
			this.options.csHelpers = initCoffeeScriptHelpers();
			this.contents = [];
		};
		Output.prototype = {
		    compile : function() {
			    var result = [];
			    for ( var i = 0, size = this.contents.length; i < size; i++) {
				    trace("Compiling" + (this.contents[i].filename ? " " + this.contents[i].filename : "") + "...");
				    result.push(this.contents[i].compile().result);
			    }
			    var ret = [], code, nl = "\n";
			    if (this.options.combine === true) {
				    ret = minify(getHelpersDeclaration(this) + nl + result.join(nl), this.options);
			    } else {
				    ret = result;
			    }
			    return ret;
		    },
		    add : function(script, filename, type) {
			    trace("Adding" + (filename ? " " + filename : "") + "...");
			    this.contents.push(OutputScript.create(script, filename, type, this.options));
		    }
		};
		
		/** One individual render item. */
		OutputScript = function CompilationResult(script, options) {
			this.options = options;
			this.code = script.code;
			this.filename = script.filename;
			this.sourceMap = {};
		};
		OutputScript.prototype = {
			compile : function() {
				var js = this._getJS();
				if (this.options.combine === true) {
					this.result = js;
				} else {
					this.result = result.code;
					this.sourceMap = result.sourceMap;
				}
				return this;
			}
		};
		
		OutputScript.create = function(script, filename, type, options) {
			script = new String(script);
			var mC = script.match(rxCoffeeScript2), mJs = script.match(rxJavaScript);
			if (type == "javascript") {
				return new OutputJavaScript(script, filename, options);
			} else {
				return new OutputCoffeeScript(script, filename, options);
			}
		};
		
		/** Individual JavaScript */
		OutputJavaScript = function(script, filename, options) {
			OutputScript.apply(this, [ {
			    filename : filename,
			    code : script,
			    sourceMap : {}
			} ].concat(Array.prototype.slice.call(arguments, 2)));
		};
		OutputJavaScript.prototype = {
		    compile : function() {
			    return OutputScript.prototype.compile.apply(this, arguments);
		    },
		    _getJS : function() {
			    return this.code;
		    }
		};
		
		/** Individual CoffeeScript */
		OutputCoffeeScript = function(script, filename, options) {
			var coffeescript = CoffeeScript.compile(script, {
			    sourceMap : true,
			    bare : true
			// use the bare option, we'll wrap it ourself
			});
			OutputScript.apply(this, [ {
			    filename : filename,
			    code : script,
			    sourceMap : {}
			} ].concat(Array.prototype.slice.call(arguments, 2)));
			this.helperOffsets = [];
			this.sourceMap = {
			    v1 : coffeescript.sourceMap,
			    v3 : coffeescript.v3SourceMap
			};
			this.fragments = coffeescript.fragments;
			this.js = coffeescript.js;
			extractCoffeeScriptHelpers(this);
		};
		OutputCoffeeScript.prototype = {
		    compile : function() {
			    return OutputScript.prototype.compile.apply(this, arguments);
		    },
		    _getJS : function() {
			    if (this.options.jscombine === false) {
				    return this.js;
			    }
			    var fragments = [].concat(this.fragments), result = [];
			    for ( var offset, i = 0, size = this.helperOffsets.length; i < size; i++) {
				    offset = this.helperOffsets[i];
				    fragments.splice(offset.index, offset.count);
			    }
			    for ( var i = 0, size = fragments.length; i < size; i++) {
				    result.push(fragments[i].code);
			    }
			    return result.join("");
		    }
		};
		
		/** Initialization function */
		function init(options_n) {
			options = defaults({}, options_n, defaultOptions);
			coffeeScriptHelpers = initCoffeeScriptHelpers();
			output = new Output(options);
			return ret;
		}
		
		function initCoffeeScriptHelpers() {
			return function(ret, words) {
				for ( var i = 0, size = words.length; i < size; i++) {
					ret[words[i]] = {
					    rx : new RegExp("__" + words[i] + "\\s+\\=\\s+(.*)?(\\,\\n)|$"),
					    found : false
					}
				}
				return ret;
			}({}, [ "bind", "hasProp", "extends", "slice" ])
		}
		
		function getHelpersDeclaration(output) {
			var result = [];
			for ( var r in output.options.csHelpers) {
				var helper = output.options.csHelpers[r];
				if (helper.found === true) {
					result.push("__" + r + "=" + helper.code);
				}
			}
			if (result.length === 0) {
				return "";
			}
			return "var " + result.join(",") + ";";
		}
		
		function processFragment(f, result) {
			var r, m, w;
			for (w in result.options.csHelpers) {
				r = result.options.csHelpers[w];
				if (!r.found && r.rx.test(f.code)) {
					m = f.code.match(r.rx);
					if (m.length > 0 && !isUndef(m[1])) {
						r.found = true;
						r.code = m[1];
					}
				}
			}
			return coffeeScriptHelpers;
		}
		
		function extractCoffeeScriptHelpers(script) {
			for ( var f, i = 0, size = script.fragments.length; i < size; i++) {
				f = script.fragments[i];
				if (/__bind|__hasProp|__extend|__slice/.test(f.code)) {
					processFragment(f, script);
					var q = 1;
					if (/^\s*?;\s*?$/.test(script.fragments[i + 1].code)) {
						q++;
						if (/^\s*?var\s*?$/.test(script.fragments[i - 1].code)) {
							q++;
							i--;
						} else if (/^\s*?\,\s*?$/.test(script.fragments[i - 1].code)) {
							i--;
						}
					}
					script.helperOffsets.push({
					    index : i,
					    count : q
					});
					break;
				}
			}
		}
		
		function hasProp(obj, prop) {
			return obj.hasOwnProperty(prop);
		}
		
		function defaults(obj) {
			obj = obj || {};
			for ( var trg, p, i = 1, size = arguments.length; i < size; i++) {
				trg = arguments[i];
				if (!isUndef(trg)) {
					for (p in trg) {
						if (hasProp(trg, p) && isUndef(obj[p])) {
							obj[p] = trg[p];
						}
					}
				}
			}
			return obj;
		}
		
		// minify using UglifyJS
		function minify(script, options) {
			trace("Minifying...")
			script = wrapScript(script, options);
			
			if (options.compress === false && options.mangleNames === false) {
				return script;
			}
			
			var stream, ast, compressor, sourceMap;
			sourceMap = UglifyJS.SourceMap({});
			stream = UglifyJS.OutputStream({
			    space_colon : false,
			    source_map : sourceMap
			});
			
			trace(" -> Parsing...");
			ast = UglifyJS.parse(script);
			
			trace(" -> Figuring out the scope...");
			ast.figure_out_scope();
			
			if (options.compress === true) {
				trace(" -> Compressing...");
				compressor = UglifyJS.Compressor({});
				ast = ast.transform(compressor);
			}
			
			if (options.mangleNames === true) {
				trace(" -> Mangling names...");
				ast.compute_char_frequency();
				ast.mangle_names();
			}
			
			trace(" -> Finalizing...");
			ast.print(stream);
			
			return {
			    code : stream.toString(),
			    sourceMap : {
				    v3 : sourceMap.toString()
			    }
			};
		}
		
		// wrap a script into a self-executing anonymous function
		function wrapScript(script, options) {
			return options.bare === true ? script : "(function(){" + script + (options.compress === true ? "" : "\n") + "}).call(this)";
		}
		
		(function hackTheCoffee() {
			// TODO:
			// write coffeeScript hack to preserve the fragments
			// currently this is done in the "hacked" coffees-cript.js itself
		})();
		
		// process a single coffee-script file
		ret.add = function(script) {
			return output.add.apply(output, arguments);
			
		};
		
		// wrap and minify all scripts into one
		// combine all source maps into one
		ret.compile = function() {
			return output.compile.apply(output, arguments);
		};
		
		// initialization
		ret.reInitialize = init;
		
		// return the object
		return init();
		
	})({})

})(this)