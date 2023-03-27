package it.finmatica.dmServer.jdms;

public class testGestionePulsanti {

	
    public String test(String sXML) 
    {
	
	   String s="";
	   System.out.println("******************** Ciao, come stai? ****************************");
	   System.out.println(""+sXML);
	   /*
	   s+="<FUNCTION_OUTPUT>";
	   s+="<RESULT>ok</RESULT>";
	   s+="<DOC/>";
	   s+="<LISTAID>";
	   s+="<ID>";
	   s+="<IDOGGETTO>118</IDOGGETTO>";
	   s+="<TIPOOGGETTO>D</TIPOOGGETTO>";
	   s+="<MSG></MSG>"; 
	   s+="</ID>";
	   s+="</LISTAID>";
	   s+="<FORCE_REDIRECT>N</FORCE_REDIRECT>";
	   s+="<JSYNC>";
	   s+="<AREA>GDMSYS</AREA>";
	   s+="<CONTROLLO>testGDMSYS</CONTROLLO>";
	   s+="</JSYNC>";
	   s+="</FUNCTION_OUTPUT>";
	   */
	   
	   s="<FUNCTION_OUTPUT>"
		 +"<RESULT>ok</RESULT>"
		 //+"<LISTAID/>"
		 //+"<LISTAID>"
		 /*+"<ID>"
		 +"<IDOGGETTO>118</IDOGGETTO>"
		 +"<TIPOOGGETTO>D</TIPOOGGETTO>"
		 +"<MSG>Chi spacchiu voi??</MSG>" 
		 +"</ID>"
		 +"<ID>"
		 +"<IDOGGETTO>118</IDOGGETTO>"
		 +"<TIPOOGGETTO>D</TIPOOGGETTO>"
		 +"<ERROR>Errore</ERROR>"
		 +"<MSG>Errore non si è verificAato l'operazione di Salva</MSG>" 
		 +"</ID>"
		 +"<ID>"
		 +"<IDOGGETTO>118</IDOGGETTO>"
		 +"<TIPOOGGETTO>D</TIPOOGGETTO>"
		 +"<ERROR/>"
		 +"<MSG>Cosa è successo</MSG>" 
		 +"</ID>"
		 +"<ID>"
		 +"<IDOGGETTO>118</IDOGGETTO>"
		 +"<TIPOOGGETTO>D</TIPOOGGETTO>"
		 +"<MSG>Ma cosa resta nella vita</MSG>" 
		 +"</ID>"*/
		 //+"</LISTAID>"
		 +"<STACKTRACE/>"
		 +"<REDIRECT>../common/ClosePageAndRefresh.do?idQueryProveninez=-1</REDIRECT>"
		 +"<FORCE_REDIRECT>N</FORCE_REDIRECT>"
		 +"<VALUE_REDIRECT/>"
		 +"<DATI_AGGIORNAMENTO/>"
		 +"<DOC/>"
		 +"</FUNCTION_OUTPUT>";
	   
	   
	   
	   return s;
    	
    	
    }
	
	
}
