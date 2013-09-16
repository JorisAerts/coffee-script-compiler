package com.jorisaerts.cscompiler.dependencies;

import java.io.File;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class FileList extends ArrayList<File> {

	@Override
	public boolean contains(Object o) {
		int size = size();
		if (o == null) {
			for (int i = 0; i < size; i++)
				if (get(i) == null)
					return true;
		} else {
			String path = ((File) o).getAbsolutePath();
			for (int i = 0; i < size; i++)
				if (path.equals(get(i).getAbsolutePath()))
					return true;
		}
		return false;
	}

}
