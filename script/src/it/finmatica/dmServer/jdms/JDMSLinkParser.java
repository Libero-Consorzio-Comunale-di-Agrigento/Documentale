package it.finmatica.dmServer.jdms;

import it.finmatica.textparser.AbstractParser;
import javax.servlet.http.HttpServletRequest;
import java.util.Properties;


public class JDMSLinkParser extends AbstractParser
{
   /**
	 * Gestione dell'url per icone personalizzate.
	 * Classe di servizio per la gestione del Client
	*/

  HttpServletRequest pRequest = null;
  String idOggetto,tipoOggetto,area,cm,cr,rw,profilo,idCartProvenienza,idQueryProvenienza;
  String mvpg,stato,provenienza,gdc_link;
  
  
  public JDMSLinkParser(HttpServletRequest request,String newidOggetto,String newtipoOggetto,String newarea,String newcm,String newcr,String newprofilo,String newidCartProvenienza,String newidQueryProvenienza,String newrw)
  {
	    pRequest = request;
	    idOggetto=newidOggetto;
	    tipoOggetto=newtipoOggetto;
	    area=newarea;
	    cm=newcm;
	    cr=newcr;
	    rw=newrw;
	    profilo=newprofilo;
	    idCartProvenienza=newidCartProvenienza;
	    idQueryProvenienza=newidQueryProvenienza;
  }
  
  
  public JDMSLinkParser(HttpServletRequest request,String newidOggetto,String newtipoOggetto,String newarea,String newcm,String newcr,
		  				String newprofilo,String newidCartProvenienza,String newidQueryProvenienza,String newrw,String  newmvpg,
		  				String newstato,String newprovenienza,String newgdclink)
  {
	    pRequest = request;
	    idOggetto=newidOggetto;
	    tipoOggetto=newtipoOggetto;
	    area=newarea;
	    cm=newcm;
	    cr=newcr;
	    profilo=newprofilo;
	    idCartProvenienza=newidCartProvenienza;
	    idQueryProvenienza=newidQueryProvenienza;
	    rw=newrw;
	    mvpg=newmvpg;
	    stato=newstato;
	    provenienza=newprovenienza;
	    gdc_link=newgdclink;
  }
   
  
  protected String findParamValue(String nomePar, Properties extraKeys) 
  {
	  		String valore=null;
	  		
	  		boolean bIsNumber=true;
	  		try {
	  			Long.parseLong(nomePar);	
	  		}
	  		catch (Exception e) {
	  			bIsNumber=false;
	  		}
	  		
	  		if (bIsNumber) return ":"+nomePar;
	  		
	  		//if (nomePar.equals("all_parametri")) return ":"+nomePar;
	  		
	  		if((valore==null) && (nomePar.equals("idOggetto")))
	  		 valore=idOggetto;
	  		
	  		if((valore==null) && (nomePar.equals("tipoOggetto")))
		  	 valore=tipoOggetto;
	  		
	  		if((valore==null) && (nomePar.equals("area")))
		  	 valore=area;
		  	
		  	if((valore==null) && (nomePar.equals("cm")))
			 valore=cm;
	  		
		  	if((valore==null) && (nomePar.equals("cr")))
		  	 valore=cr;
		  	
		  	if((valore==null) && (nomePar.equals("profilo")))
			 valore=profilo;
		  		
		  	if((valore==null) && (nomePar.equals("idCartProvenienza")))
			 valore=idCartProvenienza;
			  	
			if((valore==null) && (nomePar.equals("idQueryProvenienza")))
			 valore=idQueryProvenienza;
			
	  		if((valore==null) && (nomePar.equals("rw")))
			  valore=rw;
		  	
	  		if((valore==null) && (nomePar.equals("MVPG")))
			  valore=mvpg;
	  		
	  		if((valore==null) && (nomePar.equals("stato")))
			  valore=stato;
	  		
	  		if((valore==null) && (nomePar.equals("Provenienza")))
			  valore=provenienza;
		  		
		  	if((valore==null) && (nomePar.equals("GDC_Link")))
			  valore=gdc_link;
		  	
		  	if((valore==null) || (valore.equals("")))
	          valore=(String)pRequest.getSession().getAttribute(nomePar);
		  	
		  	
		  		  		
	  	    return valore;
  }
  
}
