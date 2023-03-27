package it.finmatica.dmServer;

import it.finmatica.dmServer.SOA.SOAIProfilo;
import it.finmatica.dmServer.SOA.SOAIQuery;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Allegato;
import it.finmatica.dmServer.dbEngine.struct.dbTable.MetadatoSimple;
import it.finmatica.dmServer.dbEngine.struct.searchObject.MetadatoRicerca;
import it.finmatica.dmServer.dbEngine.struct.searchObject.RisultatoRicerca;
import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TestSOA {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		/*MetadatoSimple[] metaInfo = new  MetadatoSimple[1];
		
		metaInfo[0]= new MetadatoSimple("TEST_CHECK","99");
		
		SOAIProfilo soaIProf = new SOAIProfilo("GDM", "jdbc/gdm",
				   "TESTADS","M_ORIZZONTALE","","");

			String idDoc = soaIProf.registra(metaInfo, null);*/
		Class.forName("oracle.jdbc.driver.OracleDriver");
		SessioneDb.getInstance().addAlias("oracle.","oracle.jdbc.driver.OracleDriver");
		IDbOperationSQL dbOperation = SessioneDb.getInstance().createIDbOperationSQL("oracle.",
			"jdbc:oracle:thin:@10.97.11.74:1521/TESTGPS",
			"GDM",
			"GDM");


		/*SOAIQuery soaIq = new SOAIQuery("5470",dbOperation);

		MetadatoRicerca[] metaInfo= new MetadatoRicerca[1];
		metaInfo[0] = new MetadatoRicerca("IDRIF","11189011",1);

		RisultatoRicerca result = soaIq.ricercaForWS("SEGRETERIA", "M_ALLEGATO_PROTOCOLLO", "BO", metaInfo, 100000);

 int k;
 k=0;*/

		String datiXML="<metaDataList>"
			+ "<metaInfo>"
			+ "<codice>FSF_AMMINISTRAZIONE</codice>"
			+ "<valore><![CDATA[ENTE]]></valore>"
			+ "</metaInfo>"
			+ "<metaInfo>"
			+ "<codice>FSF_DATA_PRIMA_ESEC</codice>"
			+ "<valore>01/01/2022</valore>"
			+ "</metaInfo>"
			+ "<metaInfo>"
			+ "<codice>FSF_FATTO</codice>"
			+ "<valore><![CDATA[0]]></valore>"
			+ "</metaInfo>"
			+ "<metaInfo>"
			+ "<codice>FSF_INFO_1</codice>"
			+ "<valore><![CDATA[prova]]></valore>"
			+ "</metaInfo>"
			+ "<metaInfo>"
			+ "<codice>FSF_LANGUAGE</codice>"
			+ "<valore><![CDATA[it]]></valore>"
			+ "</metaInfo>"
			+ "<metaInfo>"
			+ "<codice>FSF_NOME_FILE</codice>"
			+ "<valore><![CDATA[CODICE_INDIVIDUALE#ANNO#.PDF]]></valore>"
			+ "</metaInfo>"
			+ "<metaInfo>"
			+ "<codice>FSF_PERCORSO_FILE</codice>"
			+ "<valore><![CDATA[/c_000]]></valore>"
			+ "</metaInfo>"
			+ "<metaInfo>"
			+ "<codice>FSF_PERIODICITA</codice>"
			+ "<valore><![CDATA[Adesso]]></valore>"
			+ "</metaInfo>"
			+ "<metaInfo>"
			+ "<codice>FSF_STRUTTURA_FASCICOLO</codice>"
			+ "<valore><![CDATA[Fascicolo Economico/Cedolini/#ANNO#]]></valore>"
			+ "</metaInfo>"
			+ "<metaInfo>"
			+ "<codice>FSF_STRUTTURA_ID</codice>"
			+ "<valore><![CDATA[1]]></valore>"
			+ "</metaInfo>"
			+ "<metaInfo>"
			+ "<codice>FSF_UNITA_DOCUMENTALE_ID</codice>"
			+ "<valore><![CDATA[304]]></valore>"
			+ "</metaInfo>"
			+ "</metaDataList>";

		SOAIProfilo soaiProfilo = new SOAIProfilo("GDM","","RISORSE_UMANE","FGP_FASFILE_DDS","","");
		System.out.println(soaiProfilo.registra(datiXML,"%20","%20","BO","Y","%20","N","%20"));
	}

}
