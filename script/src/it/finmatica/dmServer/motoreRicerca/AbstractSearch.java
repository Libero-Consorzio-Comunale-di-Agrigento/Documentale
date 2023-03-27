package it.finmatica.dmServer.motoreRicerca;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.LookUpDMTable;
import it.finmatica.dmServer.util.ObjFileConditionStruct;
import it.finmatica.log4jsuite.LogDb;

import java.util.Vector;

public abstract class AbstractSearch {
	   protected String controlloCompetenzaQuery = Global.ABIL_LETT;
	   
	   protected String lastQueryExecuted = null;
	   
	   protected String sFullTextWarea="";

	   protected int queryServiceLimit=0;

	   public AbstractSearch() {}
			   
	   public AbstractSearch(String newArea, Environment newVu)  {}
	   
	   public AbstractSearch(String newArea, Vector cm, Vector cmArea, Environment newVu) {}
	   
	   public abstract void setEnvironment(Environment newVu);
	   	 
	   public abstract void setCampi(Vector vCampi);	  	  

	   public abstract void setIdDocumento(Vector id);	   
	   
	   public abstract void setObjFileCondition(Vector objFile);

	   public abstract void setCampiOrdinamento(Vector vCampiOrdinamento);
	   
	   public abstract void setCondizioneAnd(String sCondAnd);
	   
	   public abstract void setCondizioneOr(String sCondOr);

	   public abstract void setCondizioneNot(String sCondNot);

	   public abstract void setCondizioneFullText(String sCondFullText);

	   public void setQueryServiceLimit(int queryServiceLimit) {
		 this.queryServiceLimit = queryServiceLimit;
	  }
	   
	   public void setCondizioneFullTextWArea(String sCondFullText,String sAllegati, String sAllegatiOCR,String sCampoFullText) {
		      sFullTextWarea=" F_FILTRO_FULLTEXT_WAREA(id,'"+sCondFullText+"','"+sAllegati+"','"+sAllegatiOCR+"','"+sCampoFullText+"') = '1' ";
	   }
	   
	   public abstract void setRicercaWeb(boolean bIsRicercaWeb);
	   
	   public abstract void setMaster(boolean bMaster);
	   public abstract void setCatMaster(String sCatMaster);
	   
	   public abstract void setCondFiltroWAreaCasoMaster(String cond);

	   public abstract void setIsRicercaPuntuale(boolean isRicercaPuntuale);
	   
	   public abstract void setFetchSize(int newFetchSize);

	   public abstract void setFetchInit(int newFetchInit);
	   
	   public abstract void setTypeModelReturn(String area, String cm);

       public abstract void setTypeModelReturn(String categoria);
       
       public abstract void setTimeOut(int iTime);
       
       public abstract void setSqlSelect(String sel);
       
       public abstract void setSqlCollectionIQuerySelect(String sel);
       
       public abstract void setLog4JSuite(LogDb log4jsuite);

	   public abstract boolean isLastRowFetch();
	   
	   public abstract Vector getDocumentList();
	   
	   public abstract Vector getDocumentListWithIdTipoDoc();
	   
	   public abstract Vector getVAliasCampiReturn();
	   
	   public abstract String getError();
	   
	   public abstract void resetDocumentList();
	   
	   public abstract void ricerca() throws Exception;
	   
	   public abstract String getSQLSelect() throws Exception;
	   
	   public abstract void setEscludiOrdinamento(boolean bFlag);
	   
	   public abstract boolean isBTrovaAnchePreBozza();

	   public abstract void setBTrovaAnchePreBozza(boolean trovaAnchePreBozza);	 
	   
	   public abstract void setControllaPadre(boolean bFlag);
	   
	   public abstract void setExtraConditionSearch(String ext);
	   
	   public void setControlloCompetenzaQuery(String comp) {
		      controlloCompetenzaQuery=comp;
	   }
	   
	   public String getLastQueryExecuted() {
			  return lastQueryExecuted;
	   }	   
}
