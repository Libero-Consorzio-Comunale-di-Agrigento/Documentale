package it.finmatica.dmServer.testColleghi;

import it.finmatica.alfresco.ws.finmaticaDmComponent.AlfrescoProfilo;

public class TestAlfresco {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	AlfrescoProfilo ap;
		
		ap = new AlfrescoProfilo("http://10.97.31.51:8080/alfresco/api",
				                 "admin",
				                 "admin",
				                 "/app:company_home",
				                 "99897",
				                 "Ciccio Pasticcio",
				                 "<html><head><body>ciccio pasticcio</body></head></html>",//"D:\\Gas2301.JPG",
				                 AlfrescoProfilo._TYPECONTENT_HTML,//AlfrescoProfilo._TYPECONTENT_JPG,
				                 "ads.customSocietaModel.model",//"ads.customProtocolModel.model",
				                 "csm",//"cpm",
				                 "campiSocieta");//"testataProtocollo");

		/*ap.settaValore("anno","2009");
		ap.settaValore("numero","1");
		ap.settaValore("tipoRegistro","L");
		ap.settaValore("note","e ca semu");*/
		
		ap.settaValore("nome","ciccio");
		ap.settaValore("cognome","pasticcio");
		
		
		ap.settaValore("datanascita","2009-10-20T10:05:19.000Z");
		ap.settaValore("piva","5435");
		
		 
		//ap.setFileName("Bolletta2.JPG","D:\\Bolletta2.JPG","allegati",AlfrescoProfilo._TYPECONTENT_JPG);
		//ap.setFileName("ERROREMAUGERI.JPG","D:\\ERROREMAUGERI.JPG","allegati",AlfrescoProfilo._TYPECONTENT_JPG);
		//ap.setFileName("m2.DOC","D:\\m2.doc","allegati",AlfrescoProfilo._TYPECONTENT_DOC);
		//ap.setFileName("m1.DOC","D:\\m1.doc","allegati",AlfrescoProfilo._TYPECONTENT_DOC);
		
		ap.setFileName("Bolletta2.JPG","D:\\Bolletta2.JPG","allegati_societa",AlfrescoProfilo._TYPECONTENT_JPG);
		ap.setFileName("ERROREMAUGERI.JPG","D:\\ERROREMAUGERI.JPG","allegati_societa",AlfrescoProfilo._TYPECONTENT_JPG);
		
		//ap.setDeleteFileName("m1.Doc","allegati");
		//ap.setDeleteFileName("m2.Doc","allegati");
		
		System.out.println(ap.salva());
	}

}
