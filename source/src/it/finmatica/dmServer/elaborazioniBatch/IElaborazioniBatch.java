package it.finmatica.dmServer.elaborazioniBatch;

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

public interface IElaborazioniBatch {	
	public void creaElaborazione() throws Exception;
	public void creaElaborazione(String idElaborazione) throws Exception;
	public void terminaElaborazione(InputStream is) throws Exception;
	public void setStatoElaborazione(String stato) throws Exception;
	public String getStatoElaborazione() throws Exception;
	public String getErroreElaborazione() throws Exception; 
	public InputStream getFileElaborazione() throws Exception;
	public void setFileElaborazione(String nomefile, InputStream is) throws Exception;	
	public void startElaborazione() throws Exception;
	public void startElaborazione(String idElaborazione) throws Exception;
	public String getUrlFileElaborazione() throws Exception;
	public String getNomeElaborazione() throws Exception;	
	public void setNomeElaborazione(String nomeElaborazione)throws Exception;	
	public String getDataInizioElaborazione() throws Exception;	
	public void setDataInizioElaborazione(String dataInizioElaborazione) throws Exception;	
}
