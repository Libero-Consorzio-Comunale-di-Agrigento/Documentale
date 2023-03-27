package it.finmatica.dmServer.testColleghi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;
import java.util.Vector;

import org.dom4j.Element;

import it.finmatica.dmServer.ACL;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.competenze.Abilitazioni;
import it.finmatica.dmServer.competenze.GDM_Competenze;
import it.finmatica.dmServer.sysIntegration.SysIntegrationModel;
import it.finmatica.dmServer.sysIntegration.SysIntegrationPending;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.HashMapSet;
import it.finmatica.dmServer.util.XMLUtilDom4j;

public class TestSysIntegration {

	public static void main(String[] args) {			
		   try {
			  Class.forName("oracle.jdbc.driver.OracleDriver");
			  Connection conn=DriverManager.getConnection("jdbc:oracle:thin:@jvm-efesto:1521:ORCL","GDM","GDM");
			  conn.setAutoCommit(false);
			  
			  Environment newEn = new Environment("GDM","","","","",conn /*"c:\\gd4dm.properties"*/);
			  
			/*  GDM_Competenze gdmCom = new GDM_Competenze(newEn);
			  
			  HashMapSet hms = gdmCom.getElencoCompetenzeDocumento(new Abilitazioni(Global.ABIL_DOC,"421",Global.ABIL_LETT+";"+Global.ABIL_MODI),true);*/
			  
			/*  Iterator i =  hms.getHashSet("GDM");

              if (i==null) return;

              while (i.hasNext())
            	   System.out.println("-->"+((ACL)i.next()).getNominativo());*/
			  
	/*		 Vector v = hms.getAllHashSet();
			 for(int i=0;i<v.size();i++) System.out.println("-->"+((ACL)v.get(i)).getNominativo());*/
			 
			  
			  //Serve solo se devo usare l'env. connesso e fare altro....se devo solo lanciare la checkPending o la insert
			  //allora non serve proprio perché a connettere e sconnettere ci pensa la SysIntegrationPending
			  //newEn.connect();
			  
			  /*SysIntegrationModel sym = new SysIntegrationModel("TESTADS","M_ORIZZONTALE","CRV-DOCS",newEn);
			  
			  System.out.println(sym.toString());*/
			  
			  //36,43,405
			  //CODICE CHE E' STATO MESSO NEL DMSERVER ALLA FINE DEL SALVATAGGIO (PRIMA DEL COMMIT OVVIAMENTE)
			  //COSì SE IL MODELLO DI QUESTO DOC HA PREVISTA UN INTEGRAZIONE
			  //VERRà INSERITO UN RECORD NELLA SYSPENDING E QUESTO POI VERRA'
			  //TRATTATO SUBITO SE E' SINCRONO, ALTRIMENTI RESTA IN PENDING
			  //IN ATTESA DELLA CHECK PENDING SOTTO CHE PARTE DA UN FLUSSO O ALTRO
			  //SysIntegrationPending syp = new SysIntegrationPending(405/*"CRV-DOCS"*/,newEn);			  
			  //System.out.println(syp.toString());
			  //syp.insertPending();
			  
			  //CODICE DA METTERE DIETRO UN FLUSSO PER FAR PARTIRE L'INTEGRAZIONE
			  //PER OGNI RECORD IN PENDING
			 // SysIntegrationPending syp = new SysIntegrationPending("CRV-DOCS",newEn);
			//  syp.checkPending();
			  
			 // parseXmlDecodeModel();
			  
			  //newEn.disconnectClose();
			 
			  String s="AND conservazioni.tipo_documento = '@NOME'";
			 System.out.println( "-->"+s.substring(s.length() -1)+"<---");
			  
			  conn.close();
			  
			  
			  
		   }
		   catch (Exception e ){
			 
			  e.printStackTrace();
		   }
	}
	
	   private static void parseXmlDecodeModel() throws Exception {
		   String crv_application;
	   	   try {
	   		  XMLUtilDom4j xmlDom4j = new XMLUtilDom4j("<root><objectdecode type=\"NUMBER\">897</objectdecode><extraPar><par name=\"applicazione\">atti</par></extraPar></root>");
	   		  
	   		  Vector<Element> vEl = xmlDom4j.leggiChildElementXML(xmlDom4j.getRoot());
	   		  
	   		  if (vEl.size()!=1 && vEl.size()!=2) throw new Exception("XML MAL FORMATO!! Seguire le istruzioni per crearlo");
	   		
	   		  String crv_identifierModel=vEl.get(0).getText();		  
	   		  
	   		  //Esistono gli extraPar....li tratto
	   		  if (vEl.size()==2) {
	   			  Vector<Element> vElPar = xmlDom4j.leggiChildElementXML(vEl.get(1));
	   			  
	   			  if (vElPar.size()>0)  {
	   				 if (!vElPar.get(0).attribute("name").getText().toLowerCase().equals("applicazione"))
	   					throw new Exception("XML MAL FORMATO!! Manca il tag <par> con name=”"+""+"” per la definizione dell'applicazione-crv");
	   				 else		   					 
	   					crv_application=vElPar.get(0).getText();
	   			  }		   				
	   			  else
	   				 throw new Exception("XML MAL FORMATO!! Manca il tag <par> per la definizione dell'applicazione-crv");
	   		  }
	   		  
	       }
	   	   catch (Exception e) {
	   		  throw new Exception("crv-SyncroExecute::parseXmlDecodeModel - Errore nel parsing dell'XML_DECODEREMOTEMODEL (per estrarre la decodifica del modello gdm-crv e l'applicazione crv).\n"+e.getMessage());
	   	   }
   }

}
