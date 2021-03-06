package com.jorisaerts.cscompiler.compilers.result;

public class CompilationResultImpl implements CompilationResult {
	SourceMap sourceMap = new SourceMapImpl();
	String code;

	@Override
	public SourceMap getSourceMap() {
		return sourceMap;
	}

	public void setSourceMap(SourceMap sourceMap) {
		this.sourceMap = sourceMap;
	}

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return new StringBuilder().append('{').append("code: ").append(getCode()).append("; source-map: ").append(getSourceMap().toString()).append('}').toString();
	}

}
