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

window.CSCompiler = (function(ret) {

	var options = {}, output = [], defaultOptions = {
		bare : false,
		combine : true,
		compress : true,
		mangleNames : true,
		sourceMap : true
	}, coffeeScriptHelpers = {},

	Output, OutputScript, OutputScriptPrototype, OutputJavaScript, OutputCoffeeScript,

	rxJavaScript = /(function|\{|\}|\;|var)/g, rxCoffeeScript = /(class|\-\>|\=\>|extends)/g;

	/** Output - renders the script according to the options */
	Output = function Output(options) {
		this.options = defaults({}, options || {});
		this.options.csHelpers = initCoffeeScriptHelpers();
		this.contents = [];
		this.result = "";
	};
	Output.prototype = {
		compile : function() {
			var result = [];
			for ( var i = 0, size = this.contents.length; i < size; i++) {
				result.push(this.contents[i].compile().result);
			}
			this.result = this.options.combine === true ? minify(
					getHelpersDeclaration(this) + result.join("\n"),
					this.options) : result;
			return this;
		},
		add : function(script) {
			this.contents.push(OutputScript.create(script, this.options));
		}
	};

	/** One individual render item. */
	OutputScript = function CompilationResult(script, options) {
		this.options = options;
		this.helperOffsets = [];
		this.sourceMaps = {
			v1 : script.sourceMap,
			v3 : script.v3SourceMap
		};
		this.fragments = script.fragments;
		this.source = script.source;
		this.js = script.js;
	};
	OutputScriptPrototype = {
		compile : function() {
			var js = this._getJs();
			this.result = this.options.combine === true ? js : minify(js, this.options);
			return this;
		},
		_getJs : function() {
			if (this.combine === false) {
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
	OutputScript.prototype = OutputScriptPrototype;
	OutputScript.create = function(script, options) {
		if (rxJavaScript.test(script) && !rxCoffeeScript.test(script)) {
			return new OutputJavaScript(script, options);
		} else {
			return new OutputCoffeeScript(script, options);
		}
	}

	OutputCoffeeScript = function(script, options) {
		OutputScript.apply(this, [ CoffeeScript.compile(script, {
			sourceMap : true,
			bare : true
		// use the bare option, we'll wrap it ourself
		}) ].concat(Array.prototype.slice.call(arguments, 1)));
	};
	OutputCoffeeScript.prototype = OutputScriptPrototype;

	OutputJavaScript = function(script, options) {
		OutputScript.apply(this, [ {
			v3SourceMap : {},
			sourceMap : {},
			js : script,
			fragments : {
				code : script
			},
		} ].concat(Array.prototype.slice.call(arguments, 1)));
	};
	OutputJavaScript.prototype = OutputScriptPrototype;

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
					rx : new RegExp("__" + words[i]
							+ "\\s+\\=\\s+(.*)?(\\,\\n)|$"),
					found : false
				}
			}
			return ret;
		}({}, [ "bind", "hasProp", "extends", "slice" ])
	}

	function getHelpersDeclaration(output) {
		var result = [];
		for ( var r in output.csHelpers) {
			var helper = output.csHelpers[r];
			if (helper.found === true) {
				result.push(helper.code);
			}
		}
		if (result.length == 0) {
			return "";
		}
		return "var " + result.join(",") + ";";
	}

	function processFragment(f, result) {
		var r, m, w;
		for (w in coffeeScriptHelpers) {
			r = coffeeScriptHelpers[w];
			r.found = !r.found && f.code.match(r.rx);
			if (!r.found && r.rx.test(r.code)) {
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
				script.helperOffsets.add({
					begin : i,
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

		if (!options.compress === false || options.mangleNames === false) {
			return wrapScript(script, options);
		}

		var ast = UglifyJS.parse(script), compressor;

		ast.figure_out_scope();

		if (options.compress) {
			compressor = UglifyJS.Compressor({});
			ast = ast.transform(compressor);
		}

		if (options.mangleNames) {
			ast.compute_char_frequency();
			ast.mangle_names();
		}

		return wrapScript(ast.print_to_string(), options);
	}

	// wrap a script into a self-executing anonymous function
	function wrapScript(script, options) {
		return options.bare === true ? script : "(function(){" + script
				+ (options.compress === true ? "" : "\n") + "})()";
	}

	(function hackTheCoffee() {
		// TODO:
		// write coffeeScript hack to preserve the fragments
		// currently this is done in the "hacked" coffees-cript.js itself
	})();

	// process a single coffee-script file
	ret.add = function(script) {
		var result = output.add(script);
		return ret;
	};

	// wrap and minify all scripts into one
	// combine all source maps into one
	ret.compile = function() {
		return output.compile().result;
	};

	// initialization
	ret.reInitialize = init;

	// return the object
	return init();

})({});