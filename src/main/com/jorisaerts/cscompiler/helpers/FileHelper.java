package com.jorisaerts.cscompiler.helpers;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import com.jorisaerts.cscompiler.dependencies.FileList;

public final class FileHelper {

	public static List<File> getCoffeeFiles(String path) {
		return getCoffeeFiles(new File(path));
	}

	public static List<File> getCoffeeFiles(File folder) {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return new File(dir.getAbsolutePath() + "/" + name).isDirectory() || isCoffeeScriptFile(name);
			}
		};
		return getFiles(folder, filter);
	}

	public static FileList getCoffeeScriptFiles(FileList fileList) {
		FileList resultList = new FileList();
		for (File file : fileList) {
			if (isCoffeeScriptFile(file)) {
				resultList.add(file);
			}
		}
		return resultList;
	}

	public static FileList getJavaScriptFiles(FileList fileList) {
		FileList resultList = new FileList();
		for (File file : fileList) {
			if (isJavaScriptFile(file)) {
				resultList.add(file);
			}
		}
		return resultList;
	}

	public static boolean isCoffeeScriptFile(File file) {
		return isCoffeeScriptFile(file.getName());
	}

	public static boolean isCoffeeScriptFile(String fileName) {
		return fileName.endsWith(".coffee");
	}

	public static boolean isJavaScriptFile(File file) {
		return isJavaScriptFile(file.getName());
	}

	public static boolean isJavaScriptFile(String fileName) {
		return fileName.endsWith(".js");
	}

	public static List<File> getFiles(File folder, FilenameFilter filter) {
		return getFiles(folder, filter, true);
	}

	public static List<File> getFiles(File folder, FilenameFilter filter, Boolean recursive) {
		List<File> files = new ArrayList<File>();
		if (folder.isDirectory()) {
			for (File file : folder.listFiles(filter)) {
				if (file.isDirectory()) {
					files.addAll(getFiles(file, filter, recursive));
				} else {
					files.add(file);
				}
			}

		}
		return files;
	}

	public static File getDirectory(File file) {
		if (file.exists() && !file.isDirectory()) {
			return file.getParentFile();
		}
		return file;
	}

	public static File getJavaScriptFromCoffeeFile(File input) {
		String path = input.getParent();
		String jsFilename = input.getName();
		String fileName = jsFilename.substring(0, jsFilename.lastIndexOf('.'));
		return new File(path + "/" + fileName + ".js");
	}

}
