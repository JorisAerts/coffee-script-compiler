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

window.MOZ_SourceMap = sourceMap;

window.CSCompiler = (function(ret) {
	
	var options = {}, output = [], defaultOptions = {
	    bare : false,
	    combine : true,
	    compress : true,
	    mangleNames : true,
	    sourceMap : true
	}, coffeeScriptHelpers = {},

	Output, OutputScript, OutputJavaScript, OutputCoffeeScript,

	rxJavaScript = /(function|\{|\}|\;|var)/g, rxCoffeeScript = /(class|\-\>|\=\>|extends)/g;
	
	/** Output - renders the script according to the options */
	Output = function Output(options) {
		this.options = defaults({}, options || {});
		this.options.csHelpers = initCoffeeScriptHelpers();
		this.contents = [];
	};
	Output.prototype = {
	    compile : function() {
		    var result = [];
		    for ( var i = 0, size = this.contents.length; i < size; i++) {
			    result.push(this.contents[i].compile().result);
		    }
		    var ret = [], code;
		    if (this.options.combine === true) {
			    ret = minify(getHelpersDeclaration(this) + result.join("\n"), this.options);
		    } else {
			    ret = result;
		    }
		    return ret;
	    },
	    add : function(script) {
		    this.contents.push(OutputScript.create(script, this.options));
	    }
	};
	
	/** One individual render item. */
	OutputScript = function CompilationResult(script, options) {
		this.options = options;
		this.code = script.code;
		this.filename = script.filename;
		this.sourceMap = {}
		if (script.sourceMap) {
			if (script.sourceMap.v1) {
				this.sourceMap.v1 = script.sourceMap.v1;
			}
			if (script.sourceMap.v3) {
				this.sourceMap.v1 = script.sourceMap.v3;
			}
		}
	};
	OutputScript.prototype = {
		compile : function() {
			var js = this._getJS();
			this.result = this.options.combine === true ? js : minify(js, this.options);
			return this;
		}
	};
	
	OutputScript.create = function(script, options) {
		script = new String(script);
		var mC = script.match(rxCoffeeScript), mJs = script.match(rxJavaScript);
		if (mC != null && mC.length > 1) {
			return new OutputCoffeeScript(script, options);
		} else {
			return new OutputJavaScript(script, options);
		}
	};
	
	/** Individual JavaScript */
	OutputJavaScript = function(script, options) {
		OutputScript.apply(this, [ {
		    code : script,
		    sourceMap : {}
		} ].concat(Array.prototype.slice.call(arguments, 1)));
	};
	OutputJavaScript.prototype = {
	    compile : function() {
		    return OutputScript.prototype.compile.apply(this, arguments);
	    },
	    _getJS : function() {
		    return this.code;
	    }
	}

	/** Individual CoffeeScript */
	OutputCoffeeScript = function(script, options) {
		var coffeescript = CoffeeScript.compile(script, {
		    sourceMap : true,
		    bare : true
		// use the bare option, we'll wrap it ourself
		});
		OutputScript.apply(this, [ {
			code : script
		} ].concat(Array.prototype.slice.call(arguments, 1)));
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
		    this.helperOffsets.sort(function(a, b) {
			    if (a.index === b.index && a.count === b.count) {
				    return 0;
			    }
			    if (a.index > b.index) {
				    return -1;
			    }
			    return 1;
		    });
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
		if (result.length == 0) {
			return "";
		}
		return "var " + result.join(",") + ";";
	}
	
	function processFragment(f, result) {
		var r, m, w;
		for (w in result.options.csHelpers) {
			r = result.options.csHelpers[w];
			if (!r.found && r.rx.test(f.code)) {
				r.found = true;
				m = f.code.match(r.rx);
				if (m.length) {
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
			for (p in trg) {
				if (hasProp(trg, p) && typeof (obj[p]) === "undefined") {
					obj[p] = trg[p];
				}
			}
		}
		return obj;
	}
	
	// minify using UglifyJS
	function minify(script, options) {
		
		script = wrapScript(script, options);
		
		if (options.compress === false && options.mangleNames === false) {
			return wrapScript(script, options);
		}
		
		var stream, ast, compressor, sourceMap;
		
		sourceMap = UglifyJS.SourceMap({});
		stream = UglifyJS.OutputStream({
		    space_colon : false,
		    source_map : sourceMap
		})
		ast = UglifyJS.parse(script)
		ast.figure_out_scope();
		
		if (options.compress === true) {
			compressor = UglifyJS.Compressor({});
			ast = ast.transform(compressor);
		}
		
		if (options.mangleNames === true) {
			ast.compute_char_frequency();
			ast.mangle_names();
		}
		
		ast.print(stream);
		var code = stream.toString();
		
		return {
		    code : code,
		    sourceMap : {
			    v3 : sourceMap.toString()
		    }
		};
	}
	
	// wrap a script into a self-executing anonymous function
	function wrapScript(script, options) {
		return options.bare === true ? script : "(function(){" + script + (options.compress === true ? "" : "\n") + "})()";
	}
	
	(function hackTheCoffee() {
		// TODO:
		// write coffeeScript hack to preserve the fragments
		// currently this is done in the "hacked" coffees-cript.js itself
	})();
	
	// process a single coffee-script file
	ret.add = function(script) {
		return output.add(script);
	};
	
	// wrap and minify all scripts into one
	// combine all source maps into one
	ret.compile = function() {
		return output.compile();
	};
	
	// initialization
	ret.reInitialize = init;
	
	// return the object
	return init();
	
})({});