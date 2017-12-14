package de.bioforscher.fit3d.web.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A class to recursively zip a whole directory.
 * 
 * @author fkaiser
 *
 */
public class DirectoryZip {

	private String directoryPath;
	private String zipFileName;
	private List<String> ignoreFileList;

	private boolean ignoreFiles;

	public DirectoryZip(String zipFileName, String directoryPath) {

		this.zipFileName = zipFileName;
		this.directoryPath = directoryPath;
	}

	public String getZipFileName() {
		return this.zipFileName;
	}

	public boolean isIgnoreFiles() {
		return this.ignoreFiles;
	}

	public void setIgnoreFileList(List<String> ignoreFileList) {

		this.ignoreFileList = ignoreFileList;
	}

	public void setIgnoreFiles(boolean ignoreFiles) {

		this.ignoreFiles = ignoreFiles;
	}

	public void zipRecursively() throws IOException {

		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(
				this.zipFileName));
		addDir(new File(this.directoryPath), zos);
		zos.close();
	}

	private void addDir(File directory, ZipOutputStream zos) throws IOException {

		File[] files = directory.listFiles();

		byte[] buf = new byte[1024];

		for (int i = 0; i < files.length; i++) {

			File currentFile = files[i];

			// ignore files if enabled
			if (this.ignoreFiles) {

				if (this.ignoreFileList.contains(currentFile.getName())) {

					continue;
				}
			}

			if (currentFile.isDirectory()) {

				addDir(currentFile, zos);
				continue;
			}

			FileInputStream in = new FileInputStream(
					currentFile.getAbsolutePath());

			zos.putNextEntry(new ZipEntry(currentFile.getAbsolutePath()
					.replaceAll(this.directoryPath, "")));
			int len;
			while ((len = in.read(buf)) > 0) {
				zos.write(buf, 0, len);
			}
			in.close();
			zos.closeEntry();
		}
	}
}
