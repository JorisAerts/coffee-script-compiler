package com.jorisaerts.cscompiler.dependencies;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jorisaerts.cscompiler.dependencies.exception.CircularDependencyException;
import com.jorisaerts.cscompiler.helpers.FileHelper;
import com.jorisaerts.cscompiler.helpers.FileIOHelper;

@SuppressWarnings("serial")
public class DependencyList extends FileList {

	private final static Pattern requirePattern = Pattern.compile("^\\#\\=\\s+require\\s+\"(.+)\"", Pattern.MULTILINE);

	private final FileListMap dependencyMap = new FileListMap();

	@Override
	// override, so that each dependency is resolved
	public boolean addAll(Collection<? extends File> fileList) {
		for (File file : fileList) {
			add(file);
		}
		return true;
	}

	@Override
	public boolean add(File file) {
		if (!file.exists()) {
			return false;
		}
		if (contains(file)) {
			return false;
		}
		addDependencies(file);
		return super.add(file);
	}

	private void addDependencies(File file) {
		try {
			String contents = FileIOHelper.readFile(file);
			Matcher matcher = requirePattern.matcher(contents);
			while (matcher.find()) {
				File dependency = new File(FileHelper.getDirectory(file) + File.separator + matcher.group(1));
				addDependency(file, dependency);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addDependency(File sourceFile, File dependendFile) {
		checkCircularDependency(sourceFile, dependendFile);
		FileList dependencyList = getDependencyMapList(sourceFile);
		if (!dependencyList.contains(dependendFile)) {
			dependencyList.add(dependendFile);
			add(dependendFile);
		}
	}

	private FileList getDependencyMapList(File file) {
		if (!dependencyMap.containsKey(file)) {
			dependencyMap.put(file, new FileList());
		}
		return dependencyMap.get(file);
	}

	private void checkCircularDependency(File sourceFile, File targetFile) {
		if (getDependencyMapList(targetFile).contains(sourceFile)) {
			throw new CircularDependencyException(sourceFile, targetFile);
		}
	}

}