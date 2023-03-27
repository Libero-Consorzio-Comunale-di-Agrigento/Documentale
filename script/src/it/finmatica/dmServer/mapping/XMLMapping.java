package it.finmatica.dmServer.mapping;

import it.finmatica.dmServer.util.HashMapSet;

import java.io.*;
import java.util.Iterator;
import java.util.HashMap;

import org.w3c.dom.*;       

import javax.xml.transform.*;  
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamSource;

/**
 * Classe che gestisce il parsing del file GDMAPPING.XML
 * 
 * @author  G. Mannella
 * @version 2.8
 *
*/
public class XMLMapping {
	  
	   /**
	    * Document XML del file GDMAPPING.XML
	   */
	   Document XMLMapping;
	   
	   /**
	    * Struttura Hash che contiene la struttura XML
	    * del mapping per i soli tag ente/applicativo
	    * passati nel cotruttore
	   */
	   private HashMapSet hmsXMLMappingCampi   = new HashMapSet();
	   private HashMap    hmXMLMappingTipoDoc  = new HashMap();

	   /**
	    * ente e applicativo dai quali cercare il DM
	   */	   
	   private String ente, applicativo;
	
	   public final static String TAG_ROW           = "ROW";  
	   public final static String TAG_ROWITEM       = "ROWITEM";
	   public final static String TAG_TIPODOC       = "ROW_TIPODOC";  
	   public final static String TAG_CAMPO         = "ROW_CAMPO";  
	  
	   public final static String ATTR_NOME         = "nome";  
	   public final static String ATTR_NOME_DM      = "nomeDM"; 
	   public final static String ATTR_APPLICATIVO  = "applicativo";  
	   public final static String ATTR_ENTE         = "ente";  
	   public final static String ATTR_NOME_TIPODOC = "nome";  
	  
	   public final static String VALUE_NOME_APPL   = "APPLICATIVO";  
	   public final static String VALUE_NOME_ENTE   = "ENTE";  
	   public final static String VALUE_NOME_DM     = "DM";  
	   public final static String VALUE_REVISIONE   = "REVISIONE";  

	   /**
	    * Costruttore
	    * 
	    * @param XMLMappingFile percorso del file GDMAPPING.XML
	   */	 	   
	   public XMLMapping( String XMLMappingFile, String ente, String appl ) throws Exception  {
		      this.ente=ente;
		   	  this.applicativo=appl;
		   	  
		      //Leggo il file XML e lo metto nel document
		   	  readXMLMapping(XMLMappingFile);
		   	  
		   	  loadHashMap();
	   }
    
	   /**
	    * Restituisce il tipoDoc mappato sul parametro
	    * tipoDoc input a partire da ente/applicativo
	    * passati nel costruttore.
	    * Restituisce se stesso se non trova corrispondenza
	    * 
	    * @param tipoDoc tipoDoc da mappare
	    * @return tipoDoc mappato
	   */	 	   
	   public String getMappingTipoDoc(String tipoDoc) throws Exception {
	          String tipo=""+hmXMLMappingTipoDoc.get(applicativo+"@"+ente+"@"+tipoDoc);
	          
	          if (tipo.equals("null"))
	             return tipoDoc;
	          else
	        	 return tipo;
	   }
	   
	   /**
	    * Restituisce il campo mappato sul tipo 
	    * documento passato in input
	    * Restituisce se stesso se non trova corrispondenza
	    * 
	    * @param tipoDoc       Tipo documento sul quale fare mapping
	    * @param bIsTipoDocDM  se true allora tipoDoc si riferisce al tipoDoc già mappato
	    *                      altrimenti si riferisce al tipoDoc da mappare 
	    * @param campo         Campo sul quale fare mapping
	    * @return campo mappato
	   */	    
	   public String getMappingCampo(String tipoDoc, boolean bIsTipoDocDM, String campo) throws Exception {
		       String keyRicerca=null;
		    	   
		       if (bIsTipoDocDM) {
		    	   Iterator i = hmXMLMappingTipoDoc.keySet().iterator();
		    	   
		    	   if (i==null) return campo;
		    	   
		    	   while (i.hasNext()) {
		    		      String sKey=""+i.next();
		    		      if (tipoDoc.equals(hmXMLMappingTipoDoc.get(sKey))) {
		    		      	  keyRicerca=sKey;
		    		      	  break;
		    		      }
		    	   }
		    		   
		    	   if (keyRicerca==null) return campo;
		       }
		       else
		    	   keyRicerca=applicativo+"@"+ente+"@"+tipoDoc;
		   
	           Iterator i =  hmsXMLMappingCampi.getHashSet(keyRicerca);
	           
	           if (i==null) return campo;

	           while (i.hasNext()) {
            	     String sCompleta=""+i.next();
            	     String sCampo, sCampoDM;
            	     
            	     sCampo=sCompleta.substring(0,sCompleta.indexOf("@"));
            	     sCampoDM=sCompleta.substring(sCompleta.indexOf("@")+1,sCompleta.length());
            	     
            	     if (sCampo.equals(campo)) return sCampoDM;
	           }
	           
	           return campo;
	   }	    
   
	   private void readXMLMapping(String file) throws Exception {
		       DOMResult       domResult;
		       FileInputStream fis;
		       
		       try {
		        	 fis = new FileInputStream(file);
		        	 TransformerFactory tFactory = TransformerFactory.newInstance();
		        	 Transformer transformer = tFactory.newTransformer();
		        	 domResult = new DOMResult();
		        	 transformer.transform(new StreamSource(fis), domResult);
		        	 XMLMapping= (Document)domResult.getNode();
		        	 fis.close();
		       } catch (Exception e) {
		    	  throw new Exception("XMLMapping:readXMLMapping - Errore in fase di lettura del file "+file+"\n Errore esteso: "+e.getMessage());
		       }
		      
	   }
	   
	   private static String getNodeValueByAttributeValue( NodeList nodeList, String attr, String valueAttr) {
               int       n = nodeList.getLength();
               Element elem;
               
               for (int i= 0; i<n; i++) {
                    elem =(Element)nodeList.item(i);
                    if (elem.getAttribute(attr).toUpperCase().compareTo(valueAttr)==0) {
                        return nodeList.item(i).getFirstChild().getNodeValue();
                    }
               }
              
               return "";
       } 
  
       private static String getAttributeValue( Node node, String attr) {
               return ((Element)node).getAttribute(attr);
       }

       private Node getNodeTipoDoc(String tipoDoc) throws Exception {
		   	   NodeList nodeList = XMLMapping.getElementsByTagName(TAG_TIPODOC); 
               int       n=0;
               String e, a, t;
              
               if (nodeList != null)
                   n = nodeList.getLength();
              
               for (int i= 0; i<n; i++) {
               	   a  = getAttributeValue(nodeList.item(i),ATTR_APPLICATIVO);
                   e  = getAttributeValue(nodeList.item(i),ATTR_ENTE);           
                   t  = getAttributeValue(nodeList.item(i),ATTR_NOME_TIPODOC);     
                   if ( (a.compareTo(applicativo) == 0) && (e.compareTo(ente) == 0)  && (t.compareTo(tipoDoc) == 0) )
                      return nodeList.item(i);    
               }
              
               return null;
       }
       
	   private void loadHashMap() {
		       NodeList nodeList = XMLMapping.getElementsByTagName(TAG_TIPODOC); 
               int       n=0;
               String e, a, t, tm;
              
               if (nodeList != null)
                   n = nodeList.getLength();
              
               for (int i= 0; i<n; i++) {
               	   a  = getAttributeValue(nodeList.item(i),ATTR_APPLICATIVO);
                   e  = getAttributeValue(nodeList.item(i),ATTR_ENTE);            
                   t  = getAttributeValue(nodeList.item(i),ATTR_NOME_TIPODOC);
                   tm = getAttributeValue(nodeList.item(i),ATTR_NOME_DM);  

                   if ( (a.compareTo(applicativo) == 0) && (e.compareTo(ente) == 0) ) {
                	   
                	   if (!hmXMLMappingTipoDoc.containsKey(a+"@"+e+"@"+t))
                	   		hmXMLMappingTipoDoc.put(a+"@"+e+"@"+t,tm);

                	   NodeList nInterno=nodeList.item(i).getChildNodes();

                	   for(int j=0;j<nInterno.getLength();j++ ) {    
                		   
                		   hmsXMLMappingCampi.add(a+"@"+e+"@"+t,nInterno.item(j).getAttributes().item(0).getNodeValue()+"@"+nInterno.item(j).getAttributes().item(1).getNodeValue());
                	   }                     
                   }
               }                   
	   }       
}