package it.finmatica.dmServer.FirmaUnimatica;

import it.finmatica.dmServer.management.Profilo;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.StringTokenizer;


public class FirmaUnimatica {
	public static final String CODICE_MODELLO = "FIRMA_LOG";
	public static final String CODICE_AREA = "GDMSYS";
	public static final String CODICE_ERRORE_FIRMA = "CODICE_ERRORE_FIRMA";
	public static final String DATA_FIRMA = "DATA_FIRMA";
	public static final String DATA_RICHIESTA = "DATA_RICHIESTA";
	public static final String DESCR_ERRORE_FIRMA = "DESCR_ERRORE_FIRMA";
	public static final String IDRIF = "IDRIF";
	public static final String ID_TRANSACTION_FIRMA = "ID_TRANSACTION_FIRMA";
	public static final String LOG_FIRMA = "LOG_FIRMA";
	public static final String STATO_FIRMA = "STATO_FIRMA";
	public static final String UTENTE_FIRMA = "UTENTE_FIRMA";
	
	public static final String ID_TRANSACTION_SEQUENCE_NAME = "UNIMATICA_TRANSACTION_ID";
	public static final String CODICE_MODELLO_REPORT = "FIRMA_REPORT";
	public static final String STATO_DA_FIRMARE = "DF";
	public static final String STATO_FIRMATO = "F";
	public static final String STATO_ERRORE = "ER";

	private IDbOperationSQL dbOp = null;
	private String 							idTransazione = "";
	private Connection 					cn = null;
	private LinkedList<String> 	iddocs = null;
	private String 							utente = "";
	private String 							passwd = "";
	private String 							urlServer = "";
	private String 							contextPath = "";
	private boolean 						collegati = false;
	
	
	public FirmaUnimatica(Connection conn, 
												LinkedList<String> docNumbers, 
												String userName, 
												String userPasswd,
												String urlServer,
												String contextPath,
												boolean collegati) throws Exception {
		this.cn = conn;
		this.iddocs = docNumbers;
		this.utente = userName;
		this.passwd = userPasswd;
		this.urlServer = urlServer;
		this.contextPath = contextPath;
		this.collegati = collegati;
	}
	
	public FirmaUnimatica(Connection conn, 
												LinkedList<String> docNumbers, 
												String userName, 
												String userPasswd,
												String urlServer,
												String contextPath) throws Exception {
									this.cn = conn;
									this.iddocs = docNumbers;
									this.utente = userName;
									this.passwd = userPasswd;
									this.urlServer = urlServer;
									this.contextPath = contextPath;
									this.collegati = false;
	}

	public String creaURLFirma() throws Exception {
		String retval = "";
		String listaIdDocs = "";
		String separatore = "";
		Profilo pfl = null;
		Date dataRichiesta = null;
		String query = "";
		ResultSet rst = null;
		String iddoc = null;
		String riferimenti = "";
		String codiceFiscale = "";
		try {
			if (Parametri.PARAM_CARICATI == 0) {
				Parametri.leggiParametriConnection(cn);
			}
			dbOp = SessioneDb.getInstance().createIDbOperationSQL(cn);
			query = "select "+ID_TRANSACTION_SEQUENCE_NAME+".nextval, sysdate from dual";
			dbOp.setStatement(query);
			dbOp.execute();
			rst = dbOp.getRstSet();
			if (rst.next()) {
				idTransazione = rst.getString(1);
				dataRichiesta = new Date(rst.getTimestamp(2).getTime());
			}
			if (Parametri.FIRMA_VERIFICA_CF.equalsIgnoreCase("S")) {
				query = "select AD4_SOGGETTO.GET_CODICE_FISCALE(ad4_utente.GET_SOGGETTO('"+utente+"')) from dual";
				dbOp.setStatement(query);
				dbOp.execute();
				rst = dbOp.getRstSet();
				if (rst.next()) {
					codiceFiscale = rst.getString(1);
					if (codiceFiscale == null) {
						codiceFiscale = "";
					}
				} else {
					codiceFiscale = "";
				}
			} else {
				codiceFiscale = "";
			}
			dbOp.close();
			for (int i=0; i< iddocs.size(); i++) {
				iddoc = iddocs.get(i);
				//Creo i documenti di tipo FIRMA_LOG
				creaFirmaLog(dataRichiesta, iddoc);
  			listaIdDocs += separatore+iddoc;
  			separatore = "@";
  			//Eleabor gli eventuali documenti collegati
  			if (collegati) {
  				pfl = new Profilo(iddoc,utente,passwd,"",cn);
  				pfl.accedi();
  				riferimenti = pfl.getRiferimenti();
  				if (riferimenti != null || riferimenti.length() <= 0) {
  					StringTokenizer st = new StringTokenizer(riferimenti);
  					while (st.hasMoreTokens()) {
  						iddoc = st.nextToken();
  						iddoc = iddoc.substring(0, iddoc.indexOf(","));
  						creaFirmaLog(dataRichiesta, iddoc);
  		  			listaIdDocs += separatore+iddoc;
  					}
  				}
  			}
			}
			String urlDiRitorno = urlServer + contextPath
			+ "/common/DocumentoView.do?rw=W&cm="
			+ CODICE_MODELLO_REPORT + "&area="
			+ CODICE_AREA
			+ "&stato=BO&MVPG=ServletModulisticaDocumento&"
			+ ID_TRANSACTION_FIRMA + "=" + idTransazione;

			String urlServlet = Parametri.FIRMA_URL;
			String parametri = "idDocs="
					+ listaIdDocs
					+ "&idTransazioneFirma="
					+ idTransazione
					+ "&idUtente="
					+ utente
					+ "&pwdUtente="
					+ urlencode(passwd)
					+ "&CF="
					+ codiceFiscale
					+ "&responseLinkOK=" 
					+ urlencode(urlDiRitorno)
					+ "&firmaTipo="
					+ Parametri.FIRMA_TIPO
					+ "&firmaPDFVisibile="
					+ Parametri.FIRMA_PDF_VISIBILE
					+ "&firmaPDFPagina="
					+ Parametri.FIRMA_PDF_PAGINA
					+ "&firmaPDFPosizione="
					+ Parametri.FIRMA_PDF_POSIZIONE;
			
			
			String url = new String(urlServlet);
			if (urlServlet.indexOf('?') < 0)
				url += "?"+parametri;
			else
				url += "&"+parametri;
			
			retval = url;
		} catch (Exception e) {
			try {
				dbOp.close();
			} catch (Exception edbop) {}
			throw new Exception ("Errore in fase creazione URL per firma Unimatica. \n"+ e.getMessage());
		}
		
		return retval;
	}

	/**
	 * Codifica la stringa in formato URL.
	 * 
	 * @param string la stringa da codificare
	 * @return la stringa codificata in formato URL.
	 */
	private String urlencode(String string) {
		if (string == null)
			return "";
		
		try {
			return URLEncoder.encode(string, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(string);
		}
	}
	
	private void creaFirmaLog(Date dataRichiesta, String iddoc) throws Exception {
		Profilo pfl = new Profilo(CODICE_MODELLO, CODICE_AREA, utente, passwd, "", cn);
		pfl.settaValore(DATA_RICHIESTA, dataRichiesta);
		pfl.settaValore(ID_TRANSACTION_FIRMA, idTransazione);
		pfl.settaValore(IDRIF, iddoc);
		pfl.settaValore(UTENTE_FIRMA, utente);
		pfl.settaValore(STATO_FIRMA, STATO_DA_FIRMARE);
   	if (!pfl.salva().booleanValue()) {
   		throw new Exception("Errore nel salvare il log. \n"
			+ pfl.getError());
   	}		
	}

/*  public static void main(String[] args) throws Exception {
  	LinkedList<String> iddocs = new LinkedList<String>();
  	iddocs.add("1");
  	 Connection conn=null;
  	 Class.forName("oracle.jdbc.driver.OracleDriver");
  	 conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.11:1521:ORCL","GDM","GDM");
  	 conn.setAutoCommit(false);
  	 FirmaUnimatica fu = new FirmaUnimatica(conn,iddocs,"GDM","GDM","urls","pippo");
  	 System.out.println(fu.creaURLFirma());
  	 conn.close();
  }*/
}
