package it.finmatica.dmServer.util;

import java.io.InputStream;

public class FileStruct {
	private String      fileName, fileNameRename;
	private InputStream file, fileRename;


	private long		   idOggettoFile=0;

	public FileStruct(String fileName,InputStream file) {
		this.fileName=fileName;
		this.file=file;
	}

	public FileStruct(String fileName,long idOggettoFile) {
		this.fileName=fileName;
		this.idOggettoFile=idOggettoFile;
	}

	public void setFileNameToRename(String fnRen) {
		fileNameRename=fnRen;
	}

	public void setFileToRename(InputStream fRen) {
		fileRename=fRen;
	}

	public String getFileNameToRename() {
		return fileNameRename;
	}

	public InputStream getFileToRename() {
		return fileRename;
	}

	public String getFileName() {
		return fileName;
	}

	public InputStream getFile() {
		return file;
	}

	public long getIdOggettoFile() {
		return idOggettoFile;
	}
}