package com.jorisaerts.cscompiler.compilers.result;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CompilationResultList extends ArrayList<CompilationResult> implements List<CompilationResult> {

	public CompilationResultList() {

	}

	public CompilationResultList(CompilationResult[] results) {

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (CompilationResult c : this) {
			sb.append(c).append(",");
		}
		int i = sb.length() - 1;
		if (i < 0) {
			return "";
		}
		sb.delete(i, i + 1);
		return sb.insert(0, '[').append(']').toString();
	}
}
