package it.finmatica.modulistica.htmltopdf;

import it.finmatica.modulistica.domutility.DomUtility;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import it.finmatica.dmServer.FirmaUnimatica.FirmaUnimatica;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.jfc.authentication.Cryptable;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;

public class MakePdf {
	private Document xmlDocument = null;
	private Element root = null;
	private String utenteDb = null;
	private String passwDb = null;
	private String aliasDb = null;
	private String dsnDb = null;
	private String utente = null;
	private String urlServer = null;
	private String area = null;
	private String cm = null;
	private String cr = null;
	private String contextPath = null;
	private String pathFolderPdf = null;
	private String orientation = "Portrait";
	private String nomePdf = null;
	private String idOggettoFile = null;
	private String dataAggiornamento = "";
	private String messageError = null;
	private String redirect = null;
	private boolean ok = true;

	public MakePdf(String xml) {
		String newxml = xml.substring(xml.indexOf("<FUNCTION_INPUT"), xml.length());
		xmlDocument = DomUtility.xmlToDocument(newxml);
		root = xmlDocument.getRootElement();
		init();
	}

	private void init() {
		Element conTomcat = null, clientGDm = null, connessioneDB = null;

		connessioneDB = DomUtility.leggiElementoXML(root, "CONNESSIONE_DB");
		utenteDb = DomUtility.leggiValoreXML(connessioneDB, "USER");
		passwDb = DomUtility.leggiValoreXML(connessioneDB, "PASSWORD");
		aliasDb = DomUtility.leggiValoreXML(connessioneDB, "ALIAS");
		dsnDb = DomUtility.leggiValoreXML(connessioneDB, "HOST_STRING");

		clientGDm = DomUtility.leggiElementoXML(root, "CLIENT_GDM");
		area = DomUtility.leggiValoreXML(clientGDm, "AREA");
		cm = DomUtility.leggiValoreXML(clientGDm, "CODICE_MODELLO");
		cr = DomUtility.leggiValoreXML(clientGDm, "CODICE_RICHIESTA");

		conTomcat = DomUtility.leggiElementoXML(root, "CONNESSIONE_TOMCAT");
		contextPath = DomUtility.leggiValoreXML(conTomcat, "CONTEXT_PATH");
		urlServer = DomUtility.leggiValoreXML(conTomcat, "URL_SERVER");
		utente = DomUtility.leggiValoreXML(conTomcat, "UTENTE");

		nomePdf = cm + cr;
		pathFolderPdf = Parametri.MKPDF_PATH;
		if (Parametri.MKPDF_LANDSCAPE.equalsIgnoreCase("S")) {
			orientation = "Landscape";
		}
		ok = true;
	}

	public void htmlTopdf() {
		Process p = null;
		if (!ok) {
			return;
		}

		if (pathFolderPdf == null || pathFolderPdf.length() == 0) {
			ok = false;
			messageError = "Impossibile effettuare la conversione. Percorso non specificato! ";
		}
		String sCommand = "";
		String newContextpath = contextPath.substring(0, contextPath.lastIndexOf("/") + 1);
		if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
			sCommand = "cmd.exe /C wkhtmltopdf";
		} else {
			sCommand = "wkhtmltopdf";
		}
		sCommand += " --load-error-handling ignore --disable-internal-links -O " + orientation + " \"" + urlServer
				+ newContextpath + "jgdm/ServletModulistica?" + "area=" + area + "&cm=" + cm + "&cr=" + cr + "&rw=P"
				+ "&us=" + Cryptable.cryptPasswd(utente) + "&visAll=N" + " \"" + " " + nomePdf + ".pdf";

		StringBuffer outputBuffer = new StringBuffer();
		messageError = "";
		try {
			p = Runtime.getRuntime().exec(sCommand, null, new File(pathFolderPdf));
			InputStream is = p.getErrorStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF8"));
			String line = "", oldline = "";
			while ((line = in.readLine()) != null) {
				outputBuffer.append(line);
				outputBuffer.append("\n");
				oldline = line;
			}
			if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
				if (p.exitValue() != 0) {
					System.out.println("-------- Genara PDF --------");
					System.out.println(p.exitValue());
					System.out.println(oldline);
					// System.out.println("Errore");
					throw new Exception("Errore durante la generazione del PDF.\n");
				}
			} else {

				if (oldline.toLowerCase().indexOf("done") < 0) {
					messageError = oldline;
					throw new Exception("Errore durante la generazione del PDF.\n");
				}
			}
			try {
				p.destroy();
			} catch (Exception noerr) {
				System.out.println("Errore: " + noerr.getMessage());
			}
		} catch (Exception e) {
			try {
				p.destroy();
			} catch (Exception noerr) {
				// System.out.println("Errore2: "+noerr.getMessage());
			}
			// System.out.println(e.getMessage()+messageError);
			messageError = e.getMessage() + messageError;
			ok = false;
		}
	}

	public void memorizzaPdf() {
		IDbOperationSQL dbop = null;
		if (!ok) {
			return;
		}
		try {
			LinkedList<String> iddocs = new LinkedList<String>();

			dbop = SessioneDb.getInstance().createIDbOperationSQL(aliasDb, dsnDb, utenteDb, passwDb);
			Profilo p = new Profilo(cm, area, cr);
			p.initVarEnv(utente, "", dbop.getConn());
			if (p.accedi().booleanValue()) {
				p.setFileName(pathFolderPdf + File.separator + nomePdf + ".pdf");
				if (!p.salva()) {
					throw new Exception("Impossibile salvare il documento.\n" + p.getError());
				}
				dataAggiornamento = p.getStringDataUltimoAggiornamento();
				if (p.accedi().booleanValue()) {
					idOggettoFile = p.getIdFile(nomePdf + ".pdf");
				} else {
					throw new Exception("Impossibile accedere al documento");
				}
				dbop.getConn().commit();
				File f = new File(pathFolderPdf + File.separator + nomePdf + ".pdf");
				f.delete();
				iddocs.add(p.getDocNumber());

				FirmaUnimatica fu = new FirmaUnimatica(dbop.getConn(), iddocs, utente, "", urlServer, contextPath);
				redirect = fu.creaURLFirma();
			} else {
				throw new Exception("Impossibile accedere al documento");
			}
			dbop.commit();
		} catch (Exception e) {
			ok = false;
			messageError = e.getMessage();
			try {
				dbop.getConn().rollback();
			} catch (Exception ef) {
			}
		}
		finally {
			try {
				dbop.close();
			} catch (Exception ef) {
			}
		}
	}

	public String terminaAction() {
		String retval = "";
		Element root, elp;

		Document docOut = DocumentHelper.createDocument();
		root = DocumentHelper.createElement("FUNCTION_OUTPUT");
		docOut.setRootElement(root);

		if (ok) {
			DomUtility.aggFiglio(root, "RESULT", "ok");
			DomUtility.aggFiglio(root, "REDIRECT", redirect);
			DomUtility.aggFiglio(root, "FORCE_REDIRECT", "Y");
			elp = DomUtility.aggFiglio(root, "DATI_AGGIORNAMENTO");
			DomUtility.aggFiglio(elp, "DATA", dataAggiornamento);
			DomUtility.aggFiglio(elp, "ID_OGGETTO", idOggettoFile);
		} else {
			DomUtility.aggFiglio(root, "RESULT", "nonok");
			DomUtility.aggFiglio(root, "ERROR", messageError);
			DomUtility.aggFiglio(root, "STACKTRACE", "");
		}
		DomUtility.aggFiglio(root, "DOC");
		// DomUtility.aggFiglio(root, "JSYNC");

		retval = docOut.asXML();
		return retval;
	}

	public static void main(String[] args) {
		try {
			SessioneDb.getInstance().addAlias("oracle.", "oracle.jdbc.driver.OracleDriver");
		} catch (Exception e) {
			e.printStackTrace();
		}
		String xml = "<FUNCTION_INPUT><CONNESSIONE_DB><USER>GDM</USER><PASSWORD>GDM</PASSWORD><HOST_STRING>jdbc:oracle:thin:@localhost:1521:test</HOST_STRING><ALIAS>oracle.</ALIAS></CONNESSIONE_DB><CONNESSIONE_TOMCAT><UTENTE>GDM</UTENTE><NOMINATIVO>GDM</NOMINATIVO><RUOLO/><MODULO/><ISTANZA/><PROPERTIES>inifile</PROPERTIES><URL_SERVER>http://localhost:8088</URL_SERVER><CONTEXT_PATH>/jdms</CONTEXT_PATH></CONNESSIONE_TOMCAT><CLIENT_GDM><AREA>TESTADS</AREA><CODICE_MODELLO>M_ORIZZONTALE</CODICE_MODELLO><CODICE_RICHIESTA>GDCLIENT1</CODICE_RICHIESTA><RW>W</RW><DATA_AGGIORNAMENTO/><GDC_LINK></GDC_LINK><WFATHER></WFATHER><QUERYSTRING></QUERYSTRING><IDCARTPROVENINEZ></IDCARTPROVENINEZ><TIPOWORKSPACE></TIPOWORKSPACE><IDQUERYPROVENINEZ></IDQUERYPROVENINEZ></CLIENT_GDM><DOC></DOC></FUNCTION_INPUT>";
		MakePdf mp = new MakePdf(xml);
		mp.htmlTopdf();
		mp.memorizzaPdf();
		System.out.println(mp.terminaAction());
	}
}
