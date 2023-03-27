package it.finmatica.modulistica.mappaactionstandard;

import it.finmatica.modulistica.htmltopdf.MakePdf;

public class MappaAction {
	public static String getCorpo (String nomeAction) {
		String retval = "";
		
		if (nomeAction.equalsIgnoreCase("_GDM_CREAPDF")) {
			retval = "<invocation class=\"it.finmatica.modulistica.htmltopdf.MakePdf\">"+
							 " <params> <param type=\"String\"><![CDATA[ :XML ]]></param> </params>"+
							 " <method name=\"htmlTopdf\"> </method> <method name=\"memorizzaPdf\"> </method>"+
							 " <method name=\"terminaAction\"/> </invocation>";
		}
		return retval;
	}
	
	public static String eseguiAction(String nomeAction, String xmlInput) throws Exception {
		String retval = "";
		if (nomeAction.equalsIgnoreCase("_GDM_CREAPDF")) {
			MakePdf pdf = new MakePdf(xmlInput);
			pdf.htmlTopdf();
			pdf.memorizzaPdf();
			retval = pdf.terminaAction();
		}
		
		return retval;
	}
}
