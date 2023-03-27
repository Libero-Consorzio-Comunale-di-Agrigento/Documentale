package it.finmatica.dmServer.gdmSyncro;

import java.io.InputStream;

import it.finmatica.dmServer.gdmSyncro.struct.Servizi;

public interface ISyncro {
	public String syncro(Servizi infoServizio, InputStream is, String idDocumentoRemoto) throws Exception;
	
	public InputStream download(Servizi infoServizio, String idDocumentoRemoto) throws Exception;	
}
