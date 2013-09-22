package com.jorisaerts.cscompiler.compilers.result;

public class SourceMapImpl implements SourceMap {
	String v1;
	String v3;

	public String getV1() {
		return v1;
	}

	public void setV1(String v1) {
		this.v1 = v1;
	}

	public String getV3() {
		return v3;
	}

	public void setV3(String v3) {
		this.v3 = v3;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append("{");
		String v = getV1();
		if (v != null) {
			sb.append("v1: ").append(v);
		}
		v = getV3();
		if (v != null) {
			if (sb.length() > 1) {
				sb.append(";");
			}
			sb.append("v3: ").append(v);
		}
		return sb.append("}").toString();
	}
}
