package it.finmatica.dmServer.jdms.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import it.finmatica.dmServer.jdms.CCS_Common;
import it.finmatica.dmServer.jdms.DMServer4j;
import it.finmatica.dmServer.jdms.XSS_Encoder;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.Modulistica;

import javax.servlet.http.HttpServletRequest;

public class ServiceImportAllegatiDoc {
	
	
	private HttpServletRequest request;
	private CCS_Common CCS_common; 
	private String user;
	private DMServer4j log;
	private HashMap<String,String> mapCampi; 
	private XSS_Encoder xss=null;
	
	public ServiceImportAllegatiDoc(HttpServletRequest req,CCS_Common newCommon) throws Exception{
		request = req;
		user = request.getSession().getAttribute("Utente").toString();
		CCS_common = newCommon;
		init();
		xss = new XSS_Encoder(request,CCS_common);
	}
	
	private void init() throws Exception {
		mapCampi = new HashMap<String,String>(); 
		log= new DMServer4j(ServiceImportAllegatiDoc.class,CCS_common);
	}
	
	private String verificaParametroGet(String parametro, String valore) throws Exception {
		   if (valore == null) {
		    return null;
		   }
		   String newVal=valore;
		   
		   if(xss!=null){
			   newVal = xss.encodeHtmlAttribute(parametro,valore);
			   if (!newVal.equals(valore)) {
			    throw new Exception("Parametro "+parametro+" non valido!");
			   }
		   }
		   return newVal;
	}
	
	public String build() throws Exception
	{
           String corpo="",modello="";
			try {
        	   
        	   String area="",cm="",tipodoc="",eseguiRicerca, seleziona=""; 
        	  
        	   if(request.getParameter("area")!=null && !request.getParameter("area").equals(""))
        		   area = verificaParametroGet("area",request.getParameter("area"));
        	   
        	   if(request.getParameter("cm")!=null && !request.getParameter("cm").equals(""))
        		   cm = verificaParametroGet("cm",request.getParameter("cm"));
        	  
        	   if(request.getParameter("tipodoc")!=null && !request.getParameter("tipodoc").equals(""))
        		   tipodoc = verificaParametroGet("tipodoc",request.getParameter("tipodoc"));
        	   
        	   if(request.getParameter("seleziona")!=null && !request.getParameter("seleziona").equals(""))
        		   seleziona = verificaParametroGet("seleziona",request.getParameter("seleziona"));
        	   
        	   eseguiRicerca = request.getParameter("Ricerca");
        	   
        	   corpo = "<html>";
        	   corpo+="<head>";
        	   corpo+=" <title>Import dei Documenti</title>";
        	   corpo+=" <link rel=\"stylesheet\" type=\"text/css\" href=\"../Themes/AFC/Style.css\">";
        	   corpo+=" <link rel=\"stylesheet\" type=\"text/css\" href=\"../Themes/Atti/Style.css\">";
        	   corpo+=getScript();
        	   corpo+="</head>";
        	   corpo+="<body class=\"AFCPageBODY\">";
        	   corpo+="<form method='post' name=\"submitForm\" id=\"submitForm\" action=\"ServletImportAllegatiDoc?rw=Q\">";
        	   corpo+=" 	<input id=\"area\" type=\"hidden\" value=\""+area+"\" name=\"area\"/>";
        	   corpo+=" 	<input id=\"cm\" type=\"hidden\" value=\""+cm+"\" name=\"cm\"/>";
        	   corpo+=" 	<input id=\"tipodoc\" type=\"hidden\" value=\""+tipodoc+"\" name=\"tipodoc\"/>";
        	   corpo+=" 	<input id=\"cr\" type=\"hidden\" value=\"RICERCA\" name=\"cr\"/>";
        	   corpo+=" 	<input id=\"rw\" type=\"hidden\" value=\"Q\" name=\"rw\"/>";
        	   corpo+=" 	<input id=\"seleziona\" type=\"hidden\" value=\"\" name=\"seleziona\"/>";
        	   corpo+="<div class=\"zdiv\">"+getListaModelloRicerca(tipodoc)+"</div>";
        	   if(area!=null && area.length()>0 && cm!=null && cm.length()>0){
        		  log.log_info("Costruzione del modello di ricerca con area="+area+" e cm="+cm);  	 
 	        	 
        		  Modulistica servlet = new Modulistica(request.getRealPath("/"));
 	        	  servlet.setReturnSelect(true);
 	        	  if(seleziona.equals("1"))
 	        		  servlet.disabilitaReload(true);
	        	  servlet.genera(request,"CC","ServletImportAllegatiDoc");
 	        	  modello = servlet.getValue();
 	        	  log.log_info("Modello - Ricostruzione del modello="+modello);  	
 	        	  if(modello.indexOf("<head>")!=-1 && modello.indexOf("<script")!=-1){
	 	        	  String tagHead1 = modello.substring(modello.indexOf("<head>"),modello.indexOf("<script"));
	 	        	  modello = modello.replaceAll(tagHead1, ""); 
	 	        	  String tagHead2 = modello.substring(modello.indexOf("</head"),modello.indexOf("<div"));
	 	        	  modello = modello.substring(0,modello.indexOf(tagHead2))+modello.substring(modello.indexOf(tagHead2)+tagHead2.length(),modello.length());
	 	        	  modello = modello.replaceAll("<html>", "");
	 	        	  modello = modello.replaceAll("</form>", ""); 
	 	        	  modello = modello.replaceAll("</head>", ""); 
	 	        	  modello = modello.replaceAll("</body>", ""); 
 	        	  }
 	        	  
 	        	 if(modello.indexOf("<form")!=-1 && modello.indexOf("<div")!=-1){
	 	        	  String tag = modello.substring(modello.indexOf("<form"),modello.indexOf("<div"));
	 	        	  modello = modello.substring(0,modello.indexOf(tag))+modello.substring(modello.indexOf(tag)+tag.length(),modello.length()); 
	 	        	  modello = modello.replaceAll("</form>", ""); 
	        	  }
 	        	  
 	        	  
 	         	  String tagFunctionResize ="gdmResize() {";
 	        	  if(modello.indexOf(tagFunctionResize)!=-1)
 	        		  modello = modello.replace(tagFunctionResize, tagFunctionResize+" return;");

				   //Nel caso di docuementale versione 3.6 in poi elimino la parte in jquery definita dalla servlet di modulistica
				   String tagScriptInizio="src=\"/appsjsuite/Documentale/";
				   String tagScriptFine="$(document).ready(function () { $('body').layout(); });";
				   if(modello.indexOf(tagScriptInizio)!=-1){
					   String parte = modello.substring(modello.indexOf(tagScriptInizio),modello.indexOf(tagScriptFine)+tagScriptFine.length());
					   log.log_info("Integrazione jquery da eliminare--> "+parte);
					   modello = modello.replace(parte,">");
				   }

 	        	  corpo+="<div class=\"zdiv\">";
 	        	  corpo+="<div id=\"divFiltri\" class=\"zgroup\">";
 	        	  corpo+=modello;
 	        	  corpo+="</div><div id=\"separatore\" class=\"zSeparatore\" title=\"Espandi/Comprimi i filtri di ricerca\" onclick=\"setDisplay('divFiltri');\"></div>";
 	        	  corpo+="</div>";
 	        	  log.log_debug("Modello - Concatenazione del modello di ricerca modello = "+modello);  	
 	        	  
 	        	  if(eseguiRicerca!=null && eseguiRicerca.equals("1")){
 	        		 String select = servlet.getSelectQuery();
 	        		 log.log_info("Ricerca - Recupero della select del modello di ricerca SQL="+select);  	
 	        		 if(select!=null && !select.equals(""))
 	        			 corpo+=getListaDocumentiRicerca(select,tipodoc,area,cm); 	        		
 	        	 }  
 	           }     
        	   corpo+=" </form>";
   	           corpo+="</body>";
	           corpo+="</html>";
           }
           catch (Exception e) {    
        	log.log_error("ServiceImportAllegatiDoc::build -- Costruzione maschera - Errore: "+e.getMessage());
        	throw e;
           }  
		   return corpo;	
	} 
	
	private String getScript(){
			String js="";
			js+="<script language=\"JavaScript\" src=\"include/importDocumenti.js\"></script>";
			return js;
	}
		
	private String getListaModelloRicerca(String tipoDocSelezionato) throws Exception
	{
		    String lista="",listaOption="";
		    int tagSelected = 0;
		    IDbOperationSQL dbOpSQL=null;
		    StringBuffer sql = new StringBuffer();
	        try
	        {	
	           lista+="<table class=\"ztable\">";
	           lista+="<tbody class=\"ztbody\">";
	           lista+="	<tr class=\"ztr\">";
	           lista+="	<td class=\"ztd\" align=\"right\"><span class=\"z-label\">Tipi&nbsp;Documento:</span></td>";
	           lista+=" <td class=\"ztd\"  colspan=\"2\">";
	           lista+="  <select id=\"modelliRicerca\" name=\"modelliRicerca\" style=\"width:400px;\" onchange=\"changeTipo();\">";        	   
        	   dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(CCS_common.getDataSource(),0);
	           sql.append("select decode( nvl(mi.descrizione,''),'',DECODE (NVL (m.oggetto, ''), '', m.codice_modello, m.oggetto),mi.descrizione) descrizione, ");
	           sql.append("m.area, ");
	           sql.append("m.codice_modello, ");
	           sql.append("m.id_tipodoc, ");
	           sql.append("mi.campi ");
	           sql.append("FROM modelli m, modelli_import mi ");
	           sql.append("WHERE m.area = mi.area and m.codice_modello = mi.codice_modello ");
	           sql.append("      AND gdm_competenza.gdm_verifica ('TIPI_DOCUMENTO', ");
	           sql.append("                     				   M.ID_TIPODOC, ");
	           sql.append("                  					  'C', ");
	           sql.append("                  					  :UTENTE, ");
	           sql.append("                  					  f_trasla_ruolo (:UTENTE, 'GDMWEB', 'GDMWEB'), ");
	           sql.append("                  					  TO_CHAR (SYSDATE, 'dd/mm/yyyy')) = 1 ");
	           sql.append("order by 1, 2");	 
	           log.log_info("Lista dei modelli di ricerca - sql="+sql);  
	           dbOpSQL.setStatement(sql.toString());
	           dbOpSQL.setParameter(":UTENTE",user);
	           dbOpSQL.execute();
			   ResultSet rs = dbOpSQL.getRstSet();
			   while(rs.next()){ 
				   if(tipoDocSelezionato!=null && tipoDocSelezionato.equals(rs.getString("id_tipodoc"))) {
					   tagSelected++; 
					   listaOption+="<option value=\""+rs.getString("area")+"@"+rs.getString("codice_modello")+"@"+rs.getString("id_tipodoc")+"\" selected>"+rs.getString("descrizione")+"</option>";
				   }	   
				   else {
					   listaOption+="<option value=\""+rs.getString("area")+"@"+rs.getString("codice_modello")+"@"+rs.getString("id_tipodoc")+"\" >"+rs.getString("descrizione")+"</option>";
				   }
				   
				   if(rs.getString("campi")!=null && !rs.getString("campi").equals(""))
					   mapCampi.put(rs.getString("id_tipodoc"),rs.getString("campi"));
			   }
			   
			   if(tagSelected==0)
				   lista+="<option value=\"\" selected>Seleziona&nbsp;valore</option>";
			   else
				   lista+="<option value=\"\">Seleziona&nbsp;valore</option>";
	           lista+=listaOption;
			   lista+=" </select>";
			   lista+="	</td></tr></tbody></table>";
			   dbOpSQL.close();
	        }
	        catch (SQLException e) {
				dbOpSQL.close();
	        	log.log_error("ServiceImportAllegatiDoc::getListaModelli() - SQL: "+sql+"- Errore: "+e.getMessage());
	        	throw e;
	        }
	        return lista;
	}
	
	private String getListaDocumentiRicerca(String selectModulistica,String tipodoc,String area,String cm) throws Exception
	{
		    String lista="",allegati="";
		    int count=0;
		    IDbOperationSQL dbOpSQL=null;
		    StringBuffer sql = new StringBuffer();
		    
	        try
	        {			    
	           String sqlCampi ="";
	           String listaCampi = mapCampi.get(tipodoc);   
	          	           
	           if(listaCampi!=null && listaCampi.length()>0)
	           {	        	   
	        	   CampiParser cp = new CampiParser();
	      		   cp.bindingDeiParametri(listaCampi);
	      		   sqlCampi = cp.getSqlCampi();	    
	           }	
	           
	           lista+="<div id=\"divRicerca\" style=\"height:400px; overflow-y: scroll;\">";
		       lista+="<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"table-layout:fixed;\">";
	           dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(CCS_common.getDataSource(),0);
	           sql.append("SELECT ");
	           sql.append(sqlCampi);       
	           sql.append(" a.id_documento, a.id_documento_figlio, a.ID_OGGETTO_FILE id_allegato, a.FILENAME nome_allegato, d.cr, a.icona");
               sql.append("  FROM ");
        	   sql.append("       ( "+selectModulistica+" ) d,");
        	   sql.append("       (SELECT ogfi.id_documento,");
        	   sql.append("               0 id_documento_figlio,");
        	   sql.append("               ogfi.id_oggetto_file,");
        	   sql.append("               ogfi.filename,");
        	   sql.append("               '../common/images/gdm/formati/'||fofi.icona icona");
        	   sql.append("          FROM oggetti_file ogfi,");
        	   sql.append("               formati_file fofi,");
        	   sql.append("               documenti d,");
        	   sql.append("               modelli m");
        	   sql.append("         WHERE     ogfi.id_formato = fofi.id_formato");
        	   sql.append("               AND fofi.visibile = 'S'");
        	   sql.append("               AND d.id_documento = ogfi.id_documento");
        	   sql.append("               AND d.id_tipodoc = m.id_tipodoc");
        	   sql.append("        UNION ALL");
        	   sql.append("        SELECT d.id_documento_padre,");
        	   sql.append("               d.id_documento id_documento_figlio,");
        	   sql.append("               ogfi.id_oggetto_file,");
        	   sql.append("               ogfi.filename,");
        	   sql.append("               '../common/images/allegati_doc_figli.png' icona");
        	   sql.append("          FROM documenti d,");
        	   sql.append("               oggetti_file ogfi,");
        	   sql.append("               formati_file fofi,");
        	   sql.append("               modelli m,");
        	   sql.append("              documenti dpadre");
        	   sql.append("         WHERE     ogfi.id_formato = fofi.id_formato");
        	   sql.append("               AND fofi.visibile = 'S'");
        	   sql.append("               AND d.id_documento = ogfi.id_documento");
        	   sql.append("               AND d.stato_documento NOT IN ('CA', 'RE', 'PB')");
        	   sql.append("               AND dpadre.id_documento = d.id_documento_padre");
        	   sql.append("	           AND d.id_tipodoc = m.id_tipodoc");
        	   sql.append("               AND d.id_documento_padre IS NOT NULL");
        	   sql.append("               AND m.view_allegati_padre = 'Y'");
        	   sql.append("        ORDER BY 1, 2, 3) a");
        	   sql.append(" WHERE a.ID_DOCUMENTO = d.ID");    
        	   log.log_info("Lista degli allegati - sql="+sql);  
	           dbOpSQL.setStatement(sql.toString());
	           dbOpSQL.execute();
			   ResultSet rs = dbOpSQL.getRstSet();
			   String idDocumentoPadre="";	
			   String descrPadre = "";
			   String descrizione="";
			   
			   while(rs.next()){ 	
				   
				   if(rs.getString("id_documento_figlio").equals("0")){
					   
					   if(listaCampi!=null){
						  if(!idDocumentoPadre.equals(rs.getString("id_documento"))){
							   EtichetteParser ep = new EtichetteParser(rs);
						   	   descrizione = ep.bindingDeiParametri(listaCampi);
						   							 
							   allegati +="<tr class=\"zlistitem\" onmouseover=\"this.className = 'ztrover'\" onmouseout=\"this.className = 'zlistitem'\" >";
							   if (count%2==0)
								 allegati+="<td class=\"zlistcell\"><div class=\"zlistcellcnt\">";
							   else
								 allegati+="<td class=\"zlistcell zlistitemalt\"><div class=\"zlistcellcnt\">";
							   
							   allegati+="<label>"+descrizione+"</label></div></td></tr>";
							   count++;							   
						   }
					   }
				   }		
				   else {
					   
				       if(!descrPadre.equals(rs.getString("id_documento"))){
						   EtichetteParser ep = new EtichetteParser(rs);
					   	   descrizione = ep.bindingDeiParametri(listaCampi);
					   							 
						   allegati +="<tr class=\"zlistitem\" onmouseover=\"this.className = 'ztrover'\" onmouseout=\"this.className = 'zlistitem'\" >";
						   if (count%2==0)
							 allegati+="<td class=\"zlistcell\"><div class=\"zlistcellcnt\">";
						   else
							 allegati+="<td class=\"zlistcell zlistitemalt\"><div class=\"zlistcellcnt\">";
						   
						   allegati+="<label>"+descrizione+"</label></div></td></tr>";
						   count++;
						   descrPadre = rs.getString("id_documento");
					   }
				   }
				   
				   
				   allegati +="<tr class=\"zlistitem\" onmouseover=\"this.className = 'ztrover'\" onmouseout=\"this.className = 'zlistitem'\" >";
				   if (count%2==0)
					 allegati+="<td class=\"zlistcell\"><div class=\"zlistcellcnt\">";
				   else
					 allegati+="<td class=\"zlistcell zlistitemalt\"><div class=\"zlistcellcnt\">";
				   
				   String id;
				   if(rs.getString("id_documento_figlio").equals("0"))
					   id = rs.getString("id_documento");
				   else
					   id = rs.getString("id_documento_figlio");
				   
				   String url = "../common/ServletVisualizza.do?ar="+area+"&cm="+cm+"&cr="+rs.getString("cr")+"&ca="+rs.getString("id_allegato")+"&iddoc="+id+"&firma=";
				   if(rs.getString("nome_allegato").toLowerCase().indexOf(".p7m")!=-1) url+="S";
					 
				   String anteprima = "";
				   anteprima +="<a style=\"text-decoration:none\" href=\"#\" onclick=\"window.open('"+url+"','servletvisualizza','toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 1,resizable= 1,copyhistory= 0,width=500,height=300,top=50,left=50');\" >";
				   anteprima +="<img title=\"Anteprima Documento\" style=\"cursor:hand;\" src=\""+rs.getString("icona")+"\" width=\"16\" height=\"16\" />";
				   anteprima +="</a>"; 
				   
				   allegati+="&emsp;<label><input type=\"checkbox\" name=\"cb\" value=\""+rs.getString("id_allegato")+"@"+id+"\" />&nbsp;"+anteprima+"&nbsp;"+rs.getString("nome_allegato")+"</label></div></td></tr>";
					
				   idDocumentoPadre = rs.getString("id_documento");
				   count++;
			   }
				   				   
			   if(count==0) {
				  lista+="<tbody class=\"zlistemptybody\">";
				  lista+="<tr class=\"z-listhead\" align=\"left\"><th><div class=\"zlistheadercnt\">Elenco</div></th></tr>";
				  lista+="<tr><td colspan=\"1\">Nessun documento presente.</td></tr></tbody>";
			   }
			   else {
				  lista+="<tbody>";
				  lista+="<tr class=\"z-listhead\" align=\"left\"><th><div class=\"zlistheadercnt\">Elenco</div></th></tr>";
				  lista+=allegati;				   
				  lista+="</tbody>";
			   }
				   
			   lista+="</table>";
			   lista+="</div>";
			   dbOpSQL.close();
	        }
	        catch (Exception e) {
				dbOpSQL.close();
	        	log.log_error("ServiceImportAllegatiDoc::getListaDocumentiRicerca() - SQL: "+sql+"- Errore: "+e.getMessage());
	        	throw e;
	        }
	        return lista;
	}
	
	

}
