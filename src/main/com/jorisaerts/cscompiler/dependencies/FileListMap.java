package com.jorisaerts.cscompiler.dependencies;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class FileListMap implements Map<File, FileList> {

	TreeMap<String, FileList> fileMap = new TreeMap<String, FileList>();

	@Override
	public int size() {
		return fileMap.size();
	}

	@Override
	public boolean isEmpty() {
		return fileMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return fileMap.containsKey(((File) key).getAbsolutePath());
	}

	@Override
	public boolean containsValue(Object value) {
		return fileMap.containsValue(value);
	}

	@Override
	public FileList get(Object key) {
		return fileMap.get(((File) key).getAbsolutePath());
	}

	@Override
	public FileList put(File key, FileList value) {
		return fileMap.put(key.getAbsolutePath(), value);
	}

	@Override
	public FileList remove(Object key) {
		return fileMap.remove(key);
	}

	@Override
	public void putAll(Map<? extends File, ? extends FileList> m) {
		for (File file : m.keySet()) {
			put((File) m, m.get(file));
		}
	}

	@Override
	public void clear() {
		fileMap.clear();
	}

	@Override
	public Set<File> keySet() {
		return null;
	}

	@Override
	public Collection<FileList> values() {
		return fileMap.values();
	}

	@Override
	public Set<java.util.Map.Entry<File, FileList>> entrySet() {
		return null;
	}

}
