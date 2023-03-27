package it.finmatica.dmServer.motoreRicerca;

/****************************************************************/
/*                  NUOVA GESTIONE RICERCA		                */
/****************************************************************/

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.ElapsedTime;
import it.finmatica.dmServer.util.HashMapSet;
import it.finmatica.dmServer.util.ManageConnection;
import it.finmatica.dmServer.util.ModelInformation;
import it.finmatica.dmServer.util.keyval;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.LookUpDMTable;
import it.finmatica.dmServer.util.DocUtil;
import it.finmatica.dmServer.util.FieldInformation;
import it.finmatica.dmServer.util.UtilityDate;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.utility.DateUtility;
import it.finmatica.log4jsuite.LogDb;

public class RicercaFinmatica extends AbstractSearch {
	private Environment vu;
	private IDbOperationSQL dbOp;
	private boolean bIsNew = false;

	private boolean bIsForUpdate = false;

	private boolean bIsMotoreStandard = true;

	private boolean bIsRicercaWeb = false;

	private String sFormatoData = "dd/mm/yyyy";
	private String error = null;
	private boolean bIsRicercaPuntualeGlobale = false;
	private String[] reserveWord = { "\\", "&", "?", "{", "}", ",", "(", ")",
			"[", "]", "-", ";", "~", "|", "$", "!", ">", "*", "_" };
	private String escapeCaracter = "\\";

	private ElapsedTime elpsTime;

	/**
	 * Area sulla quale impostare la ricerca
	 */
	private String area;

	private int timeout = 60;

	/**
	 * Vettore di CodiciModello per la ricerca
	 */
	private Vector vCm;
	private Vector vCmArea; // Area del CM (se impostata)
	private HashMap hMapCmCampo = new HashMap(); // HashMap per dei Cm per campo
	private HashMap hMapCategorie = new HashMap(); // HashMap per le categorie
	private Vector OjfiCondition = new Vector(); // Vector per gli oggetti file

	/**
	 * Vettore di campi (keyVal) sui quali impostare la ricerca
	 */
	private Vector campi;

	/**
	 * Vettore di ordinamento (String) sui quali impostare l'ordinamento Il
	 * vettore sarà formato da Campo@ASC/DESC
	 */
	private Vector campiOrdinamento;

	/**
	 * CONDIZIONI AND-OR-NOT-FULLTEXT
	 */
	private String condAnd;
	private String condOr;
	private String condNot;
	private String condFullText;
	private String condFiltroWAreaCasoMaster = null;

	/**
	 * documentList -> Vettore di ID documento trovati sLista -> lista di id
	 * sotto forma di "IN" es: (12,13,78)
	 */
	private Vector documentList;
	private Vector documentListWithIdTipoDoc;
	private String sLista;

	private int iNumCondizione = 1;

	/**
	 * Record dal quale partire a fare la fetch dei risultati sul vettore
	 * restituito dalla ricerca
	 */
	private int fetchInit = 0;

	/**
	 * Dimensione di fetch dei risultati sul vettore restituito dalla ricerca
	 * (-1=TUTTI)
	 */
	private int fetchSize = -1;

	/**
	 * True - Resultset esaurito False - Esiste ancora almeno un record
	 */
	private boolean bIsLastRow = false;

	private boolean bIsMaster = false;
	
	private String sCatMaster = ""; 

	private boolean bEscludiOrdinamento = false;
	private boolean bControllaPadre = false;

	/**
	 * Se siamo in presenza di ricerca con più modelli o categorie divise su più
	 * campi. Questi parametri mi aiutano a restituire gli idDocumento del tipo
	 * che l'utente preferisce.
	 */
	private String IdTipoDocIdRicercaReturn = null;
	private String categoriaIdRicercaReturn = null;
	private String indexaliasIdReturn = null;
	private String aliasReturn = null;

	private String extraConditionSearch = "";

	/**
	 * Se vengo da un caso IBRIDO di ricerca, la select mi viene passara
	 * direttamente dal modello di ricerca di modulistica
	 */
	private String sSelect = null;

	/**
	 * Se vengo da un caso di una IQueryCollection di ricerca, la select mi
	 * viene passara direttamente dalla Classe IQuery
	 */
	private String sSelectByIQueryCollection = null;

	private String hintOnlyOneField = "";
	private static String hintMasterWorkareaFiltro = "/*+ INDEX(valori VALO_DOCA_AK) */";

	/**
	 * Vettore di keyval che contiene i campi restituiti dalla ricerca. I campi
	 * sono stati impostati sulla IQuery dal metodo addCampoReturn
	 * 
	 * La struttura keyval sarà riempita come segue k.key = campo k.value = nome
	 * alias campo Query k.area = area del campo k.cm = cm del campo k.categoria
	 * = categoria del campo k.valoriCursore = valori dei campi
	 */
	private Vector vAliasCampiReturn = new Vector();

	/* SERVE PER SETTARE IDDOC DIRETTO */
	private Vector idDoc = null;

	/**
	 * Variabile per la gestione dei log su DB
	 */
	private LogDb log4jSuiteDb = null;

	private boolean bTrovaAnchePreBozza = false;

	public RicercaFinmatica(String newArea, Environment newVu) {
		area = newArea;
		vu = newVu;
		elpsTime = new ElapsedTime("RicercaFinmatica", vu);
	}

	public RicercaFinmatica(String newArea, Vector cm, Vector cmArea,
			Environment newVu) {
		this(newArea, newVu);
		vCm = cm;
		vCmArea = cmArea;
	}

	public RicercaFinmatica() {
	}

	/*******************************************************/

	public void setEnvironment(Environment newVu) {
		vu = newVu;
	}

	/**
	 * @param vCampi
	 *            Metodo che setta il vettore di campi di classe keyval
	 */
	public void setCampi(Vector vCampi) {
		campi = vCampi;
	}

	public void setIdDocumento(Vector id) {
		idDoc = id;
	}

	public void setObjFileCondition(Vector objFile) {
		OjfiCondition = objFile;
	}

	public void setExtraConditionSearch(String extraConditionSearch) {
		this.extraConditionSearch = extraConditionSearch;
	}

	/**
	 * @param vCampiOrdinamento
	 *            Metodo che setta il vettore di campi di classe String per gli
	 *            ordinamenti
	 */
	public void setCampiOrdinamento(Vector vCampiOrdinamento) {
		campiOrdinamento = vCampiOrdinamento;
	}

	public void setEscludiOrdinamento(boolean bFlag) {
		bEscludiOrdinamento = bFlag;
	}

	public void setCondizioneAnd(String sCondAnd) {
		condAnd = sCondAnd;
	}

	public void setCondizioneOr(String sCondOr) {
		condOr = sCondOr;
	}

	public void setCondizioneNot(String sCondNot) {
		condNot = sCondNot;
	}

	public void setCondizioneFullText(String sCondFullText) {
		condFullText = sCondFullText;
	}

	public void setRicercaWeb(boolean bIsRicercaWeb) {
		this.bIsRicercaWeb = bIsRicercaWeb;
	}

	public void setMaster(boolean bMaster) {
		bIsMaster = bMaster;
	}
	
	public void setCatMaster(String newSCatMaster) {
	   	  sCatMaster=newSCatMaster;
	}

	public void setCondFiltroWAreaCasoMaster(String cond) {
		condFiltroWAreaCasoMaster = cond;
	}

	public void setIsRicercaPuntuale(boolean isRicercaPuntuale) {
		bIsRicercaPuntualeGlobale = isRicercaPuntuale;
	}

	public void setFetchSize(int newFetchSize) {
		fetchSize = newFetchSize;
	}

	public void setFetchInit(int newFetchInit) {
		fetchInit = newFetchInit;
	}

	public void setControllaPadre(boolean bFlag) {
		bControllaPadre = bFlag;
	}

	public void setTypeModelReturn(String area, String cm) {
		if (area == null)
			return;

		ModelInformation mi = null;
		try {
			mi = (new LookUpDMTable(vu)).lookUpTipoDoc(cm, area);
			IdTipoDocIdRicercaReturn = mi.getIdTipoDoc();
			aliasReturn = cm;
		} catch (Exception e) {
			IdTipoDocIdRicercaReturn = null;
		}
	}

	public void setTypeModelReturn(String categoria) {
		categoriaIdRicercaReturn = categoria;
	}

	public void setTimeOut(int iTime) {
		timeout = iTime;
	}

	public void setSqlSelect(String sel) {
		sSelect = sel;
	}

	public void setSqlCollectionIQuerySelect(String sel) {
		sSelectByIQueryCollection = sel;
	}

	public void setLog4JSuite(LogDb log4jsuite) {
		log4jSuiteDb = log4jsuite;
	}

	public boolean isLastRowFetch() {
		return bIsLastRow;
	}

	public Vector getDocumentList() {
		return documentList;
	}

	public Vector getDocumentListWithIdTipoDoc() {
		return documentListWithIdTipoDoc;
	}

	public Vector getVAliasCampiReturn() {
		return vAliasCampiReturn;
	}

	public String getError() {
		return error;
	}

	public boolean isBTrovaAnchePreBozza() {
		return bTrovaAnchePreBozza;
	}

	public void setBTrovaAnchePreBozza(boolean trovaAnchePreBozza) {
		bTrovaAnchePreBozza = trovaAnchePreBozza;
	}

	public void resetDocumentList() {
		documentList.removeAllElements();
		documentListWithIdTipoDoc.removeAllElements();
	}

	public String getSQLSelect() throws Exception {
		sLista = costruzioneListaTipiDoc();

		String sql = "";

		if (sSelect == null) {
			try {
				sql = createSQLSelect();
			} catch (Exception e) {
				throw new Exception(
						"Ricerca:getSQLSelect() - createSQLSelect. Costruzione della ricerca. "
								+ e.getMessage());
			}
		} else
			throw new Exception(
					"Ricerca:getSQLSelect(). Impossibile tornare la select: TRATTASI DI SELECT ESTERNA!!!");

		return sql;
	}

	/**
	 * @throws Exception
	 *             Metodo che effettuta la ricerca riempiendo il vettore di ID
	 *             di documento trovati
	 */
	public void ricerca() throws Exception {

		documentList = new Vector();
		documentListWithIdTipoDoc = new Vector();
		String sql;

		// Controllo se impostato
		// almeno un criterio

		/*
		 * if (!testVariable()) {
		 * 
		 * vu.Global.bExitThreadRicerca=true; throw new
		 * Exception("E' necessario specificare almeno un criterio di ricerca");
		 * }
		 */

		if (sSelectByIQueryCollection != null) {
			createSQLSelect();
			sql = sSelectByIQueryCollection;
		} else {
			sLista = costruzioneListaTipiDoc();

			if (sSelect == null) {
				try {
					sql = createSQLSelect();
				} catch (Exception e) {
					vu.Global.bExitThreadRicerca = true;
					throw new Exception(
							"Ricerca:ricerca() - createSQLSelect. Costruzione della ricerca. "
									+ e.getMessage());
				}
			}
			// CASO DI MARCO MODELLO MISTO
			else {
				String sOpt = "/*+ OPT_PARAM('_optimizer_cost_based_transformation' 'off') */";
				// Se sono nel caso del full_text, devo togliere l'optimizer a
				// off
				if (condAnd != null)
					if (sSelect.indexOf(sOpt) != -1) {
						String sPrimaParte, sSecondaParte;

						int len = sOpt.length();
						sPrimaParte = sSelect.substring(0,
								sSelect.indexOf(sOpt));
						sSecondaParte = sSelect.substring(sSelect.indexOf(sOpt)
								+ len + 1);

						sSelect = sPrimaParte + " " + sSecondaParte;
					}

				sql = sSelect;
				bIsMotoreStandard = false;

				// Controllo il filtro della warea (full text)
				if (condAnd != null) {
					int indexUnion = sql.indexOf("UNION");
					String sSelect;
					String sUnion;
					// String cmAlias="";
					String condizione = "";
					/*
					 * if (IdTipoDocIdRicercaReturn!=null) { cmAlias=""+(new
					 * LookUpDMTable(vu)).lookUpNomeTabellaOrizontaleByIdTipdoc(
					 * IdTipoDocIdRicercaReturn);
					 * cmAlias=cmAlias.substring(4,cmAlias.length()); }
					 */

					condizione = Global.replaceAll(
							protectReserveWord(calcolaCondizoneFullText()),
							"'", "''");

					if (indexUnion != -1) {
						sSelect = sql.substring(0, indexUnion);
						sUnion = sql.substring(indexUnion, sql.length());

						if (aliasReturn == null)
							sql = sSelect + " and CONTAINS(FULL_TEXT,'"
									+ condizione + "')>0 " + sUnion;
						else
							sql = sSelect + " and CONTAINS(" + aliasReturn
									+ ".FULL_TEXT,'" + condizione + "')>0 "
									+ sUnion;
					} else {
						sql += "and CONTAINS(" + aliasReturn + ".FULL_TEXT,'"
								+ condizione + "')";
					}
				}
			}
		}

		try {
			execRicerca(sql);
		} catch (Exception e) {
			vu.Global.bExitThreadRicerca = true;
			throw new Exception("Ricerca:ricerca(). Esecuzione della ricerca. "
					+ e.getMessage());
		}

	}

	// This method is called when the thread runs
	/*
	 * public void run() { try {
	 * 
	 * ricerca();
	 * 
	 * vu.Global.bExitThreadRicerca=true;
	 * 
	 * } catch (Exception e) {
	 * 
	 * vu.Global.bExitThreadRicerca=true; error=e.getMessage(); } }
	 */

	/********************* PRIVATE **********************************/

	/**
	 * @return Verifica se è stata impostata almeno una condizione
	 */
	private boolean testVariable() {
		if (vCm.size() == 0 && area == null && campi.size() == 0
				&& condAnd == null && condOr == null && condNot == null
				&& condFullText == null && OjfiCondition.size() == 0)
			return false;

		return true;
	}

	/**
	 * Riempie la lista dei tipi documento a partire dai codici modello
	 */
	private String costruzioneListaTipiDoc() throws Exception {
		String sSql = getSQLListaModelli();
		String sLista = "";
		IDbOperationSQL dbOp = null;

		if (sSql.equals(""))
			return sLista;

		try {
			dbOp = connect();

			dbOp.setStatement(sSql.toString());

			dbOp.execute();

			ResultSet rst = dbOp.getRstSet();

			sLista = "(";

			int i = 0;
			while (rst.next()) {
				if (i++ != 0)
					sLista += ",";
				sLista += rst.getLong(1);
			}

			// Non c'è nessun TipoDoc
			if (sLista.equals("("))
				sLista = "(-1)";
			else
				sLista += ")";

			disconnect();
		} catch (Exception e) {

			disconnect();
			throw new Exception("Costruzione della ListaTipiDoc "
					+ e.getMessage());
		}

		return sLista;
	}

	/**
	 * Riempie la lista degli idCampo a partire dall'sql del nomecampo/tipodoc
	 */
	private String costruzioneListaIdCampo(String sql) throws Exception {
		String sLista = "";
		IDbOperationSQL dbOp = null;

		try {
			dbOp = connect();

			dbOp.setStatement(sql.toString());

			dbOp.execute();

			ResultSet rst = dbOp.getRstSet();

			sLista = "(";

			int i = 0;
			while (rst.next()) {
				if (i++ != 0)
					sLista += ",";
				sLista += rst.getLong(1);
			}

			// Non c'è nessun ID Campo
			if (sLista.equals("("))
				sLista = "(-1)";
			else
				sLista += ")";

			dbOp.close();

			disconnect();
		} catch (Exception e) {
			dbOp.close();
			disconnect();
			throw new Exception("Costruzione della Lista Id Campo "
					+ e.getMessage());
		}

		return sLista;
	}

	/**
	 * Costruisce la select principale della ricerca
	 * 
	 * @throws Exception
	 */
	private String createSQLSelect() throws Exception {
		StringBuffer sSql = new StringBuffer("");
		StringBuffer sFrom = new StringBuffer("");
		StringBuffer sFromTipoDoc = new StringBuffer("");
		StringBuffer sWhereTipoDoc = new StringBuffer("");
		StringBuffer sWhere = new StringBuffer("");
		StringBuffer sOrdinamentiDualSelectClause = new StringBuffer("");
		StringBuffer sReturnSelectClause = new StringBuffer("");
		StringBuffer sOrdinamentiSelectClause = new StringBuffer("");
		StringBuffer sOrdinamentiOrderByClause = new StringBuffer("");
		IDbOperationSQL dbOp = null;
		iNumCondizione = 1;
		boolean bExistsDocAliasInFrom = false;

		// ***********************************Esiste almeno una condizione di
		// tipo CAMPO
		if (campi.size() == 1)
			hintOnlyOneField = "";// "/*+ INDEX (valo1 valo_tida_fk) */";

		for (int i = 0; i < campi.size(); i++) {
			keyval k = (keyval) campi.get(i);

			if (k.getVal().indexOf(":@") != -1)
				continue;

			// Si tratta di un campo di join, verrà trattato
			// con un ciclo a parte
			if (k.getIndexJoin() != 0)
				continue;

			DoubleString dbs;
			try {

				// GESTIONE AREA E CM / CATEGORIA PER SINGOLO CAMPO
				String listaTipiDoc;
				String indexDoc;

				if (k.getArea() != null || k.getCategoria() != null) {
					String sTipoDoc;

					if (k.getArea() != null) {
						// Mapping su tipoDoc
						k.setCm(vu.getGDMapping().getMappingTipoDoc(k.getCm()));

						ModelInformation mi = null;
						mi = (new LookUpDMTable(vu)).lookUpTipoDoc(k.getCm(),
								k.getArea());
						sTipoDoc = mi.getIdTipoDoc();

						// Mapping su campo
						k.setKey(vu.getGDMapping().getMappingCampo(k.getCm(),
								true, k.getKey()));
						// Fine Mapping

						listaTipiDoc = "(" + sTipoDoc + ")";
					} else {
						sTipoDoc = k.getCategoria();
						listaTipiDoc = k.getCategoria();
					}

					indexDoc = getIndexDoc(sTipoDoc);

				} else {
					// Significa che devo mettere anche doc (senza numero)
					// come alias di documento
					bExistsDocAliasInFrom = true;
					indexDoc = "";

					listaTipiDoc = sLista;
				}
				// FINE GESTIONE

				if (bIsRicercaWeb) {
					boolean bRicPuntuale = false;

					if (condFiltroWAreaCasoMaster == null)
						bRicPuntuale = true;

					dbs = getSQLCampo_Condizione(k.getKey(), k.getVal(),
							k.getOperator(), null, bRicPuntuale, listaTipiDoc,
							indexDoc, k.getArea(), k.getCm(), k.getCategoria(),
							k.getTipoUguaglianza(), k.getValueNvl());
				} else
					dbs = getSQLCampo_Condizione(k.getKey(), k.getVal(),
							k.getOperator(), null, k.getIsRicercaPuntuale(),
							listaTipiDoc, indexDoc, k.getArea(), k.getCm(),
							k.getCategoria(), k.getTipoUguaglianza(),
							k.getValueNvl());

				if (!bIsMaster) {

					Vector vRet = removeCampoOrdinamentoFromCampo(k.getKey(),
							k.getArea(), k.getCm(), k.getCategoria());

					// L'ho rimosso -> è una condizione di filtro e anche di
					// ordinamento
					if (vRet.size() != 0) {
						String sCampoOrdinamento = "" + vRet.get(0);
						String sOrdinamento = estraiAscDescDaCampoOrdinamento(sCampoOrdinamento);

						String sCampoRetrun = "X";

						if (dbs.tipoCampo.equals("S")) {
							sOrdinamentiDualSelectClause
									.append(",TO_CHAR(NULL)");
							sOrdinamentiSelectClause
									.append(",DBMS_LOB.SUBSTR(valo"
											+ (iNumCondizione - 1)
											+ ".valore_clob,4000) s"
											+ (iNumCondizione - 1));

							// Controllo prima che non sia un campo di ritorno
							if (!(("" + vRet.get(1)).equals(k.ISCAMPO_RETURN))) {
								// sOrdinamentiOrderByClause.append("s"+(iNumCondizione-1)+" "+sOrdinamento+",");
								// Setto sulla chiave la frase che andrà
								// nell'order by
								if (campiOrdinamento.get(Integer.parseInt(""
										+ vRet.get(2))) instanceof keyval) {
									((keyval) campiOrdinamento.get(Integer
											.parseInt("" + vRet.get(2))))
											.setKey("ORDERBY@" + "s"
													+ (iNumCondizione - 1)
													+ " " + sOrdinamento + ",");
								} else {
									campiOrdinamento.set(
											Integer.parseInt("" + vRet.get(2)),
											"ORDERBY@" + "s"
													+ (iNumCondizione - 1)
													+ " " + sOrdinamento + ",");
								}
							}
							// Altrimenti non è un ordinamento....era un campo
							// di return....lo elimino
							else {
								campiOrdinamento.remove(Integer.parseInt(""
										+ vRet.get(2)));
							}

							if ((("" + vRet.get(1)).equals(k.ISCAMPO_RETURN))
									|| (("" + vRet.get(1))
											.equals(k.ISCAMPO_ORDINAMENTO_AND_RETURN))) {
								sReturnSelectClause.append(",s"
										+ (iNumCondizione - 1) + ",CM_s"
										+ (iNumCondizione - 1));
								sCampoRetrun = "s" + (iNumCondizione - 1);
								if (sFromTipoDoc.toString().indexOf(
										",TIPI_DOCUMENTO TIDO" + indexDoc) == -1) {
									sFromTipoDoc.append(",TIPI_DOCUMENTO TIDO"
											+ indexDoc + " ");
									sWhereTipoDoc.append(" AND TIDO" + indexDoc
											+ ".ID_TIPODOC=" + "DOCU"
											+ indexDoc + ".ID_TIPODOC ");
								}

								sOrdinamentiDualSelectClause
										.append(",TO_CHAR(NULL)");
								sOrdinamentiSelectClause.append(", TIDO"
										+ indexDoc + ".NOME as CM_s"
										+ (iNumCondizione - 1));
							}
						} else if (dbs.tipoCampo.equals("N")) {
							sOrdinamentiDualSelectClause
									.append(",TO_NUMBER(NULL)");
							sOrdinamentiSelectClause.append(",valo"
									+ (iNumCondizione - 1) + ".valore_numero n"
									+ (iNumCondizione - 1));
							// Controllo prima che non sia un campo di ritorno
							if (!(("" + vRet.get(1)).equals(k.ISCAMPO_RETURN))) {
								// sOrdinamentiOrderByClause.append("n"+(iNumCondizione-1)+" "+sOrdinamento+",");
								// Setto sulla chiave la frase che andrà
								// nell'order by
								if (campiOrdinamento.get(Integer.parseInt(""
										+ vRet.get(2))) instanceof keyval) {
									((keyval) campiOrdinamento.get(Integer
											.parseInt("" + vRet.get(2))))
											.setKey("ORDERBY@" + "TO_NUMBER(n"
													+ (iNumCondizione - 1)
													+ ") " + sOrdinamento + ",");
								} else {
									campiOrdinamento
											.set(Integer.parseInt(""
													+ vRet.get(2)), "ORDERBY@"
													+ "TO_NUMBER(n"
													+ (iNumCondizione - 1)
													+ ") " + sOrdinamento + ",");
								}
							}
							// Altrimenti non è un ordinamento....era un campo
							// di return....lo elimino
							else {
								campiOrdinamento.remove(Integer.parseInt(""
										+ vRet.get(2)));
							}

							if ((("" + vRet.get(1)).equals(k.ISCAMPO_RETURN))
									|| (("" + vRet.get(1))
											.equals(k.ISCAMPO_ORDINAMENTO_AND_RETURN))) {
								sReturnSelectClause.append(",n"
										+ (iNumCondizione - 1) + ",CM_n"
										+ (iNumCondizione - 1));
								sCampoRetrun = "n" + (iNumCondizione - 1);
								if (sFromTipoDoc.toString().indexOf(
										",TIPI_DOCUMENTO TIDO" + indexDoc) == -1) {
									sFromTipoDoc.append(",TIPI_DOCUMENTO TIDO"
											+ indexDoc + " ");
									sWhereTipoDoc.append(" AND TIDO" + indexDoc
											+ ".ID_TIPODOC=" + "DOCU"
											+ indexDoc + ".ID_TIPODOC ");
								}

								sOrdinamentiDualSelectClause
										.append(",TO_CHAR(NULL)");
								sOrdinamentiSelectClause.append(", TIDO"
										+ indexDoc + ".NOME as CM_n"
										+ (iNumCondizione - 1));
							}
						} else {
							sOrdinamentiDualSelectClause
									.append(",TO_DATE(NULL)");
							sOrdinamentiSelectClause.append(",valo"
									+ (iNumCondizione - 1) + ".valore_data d"
									+ (iNumCondizione - 1));
							// Controllo prima che non sia un campo di ritorno
							if (!(("" + vRet.get(1)).equals(k.ISCAMPO_RETURN))) {
								// sOrdinamentiOrderByClause.append("d"+(iNumCondizione-1)+" "+sOrdinamento+",");
								// Setto sulla chiave la frase che andrà
								// nell'order by
								if (campiOrdinamento.get(Integer.parseInt(""
										+ vRet.get(2))) instanceof keyval) {
									((keyval) campiOrdinamento.get(Integer
											.parseInt("" + vRet.get(2))))
											.setKey("ORDERBY@" + "TO_DATE(d"
													+ (iNumCondizione - 1)
													+ ",'" + dbs.sFormatoData
													+ "') " + sOrdinamento
													+ ",");
								} else {
									campiOrdinamento.set(
											Integer.parseInt("" + vRet.get(2)),
											"ORDERBY@" + "TO_DATE(d"
													+ (iNumCondizione - 1)
													+ ",'" + dbs.sFormatoData
													+ "') " + sOrdinamento
													+ ",");
								}
							}
							// Altrimenti non è un ordinamento....era un campo
							// di return....lo elimino
							else {
								campiOrdinamento.remove(Integer.parseInt(""
										+ vRet.get(2)));
							}

							if ((("" + vRet.get(1)).equals(k.ISCAMPO_RETURN))
									|| (("" + vRet.get(1))
											.equals(k.ISCAMPO_ORDINAMENTO_AND_RETURN))) {
								sReturnSelectClause.append(",d"
										+ (iNumCondizione - 1) + ",CM_d"
										+ (iNumCondizione - 1));
								sCampoRetrun = "d" + (iNumCondizione - 1);
								if (sFromTipoDoc.toString().indexOf(
										",TIPI_DOCUMENTO TIDO" + indexDoc) == -1) {
									sFromTipoDoc.append(",TIPI_DOCUMENTO TIDO"
											+ indexDoc + " ");
									sWhereTipoDoc.append(" AND TIDO" + indexDoc
											+ ".ID_TIPODOC=" + "DOCU"
											+ indexDoc + ".ID_TIPODOC ");
								}

								sOrdinamentiDualSelectClause
										.append(",TO_CHAR(NULL)");
								sOrdinamentiSelectClause.append(", TIDO"
										+ indexDoc + ".NOME as CM_d"
										+ (iNumCondizione - 1));
							}
						}

						if (!sCampoRetrun.equals("X")) {
							// Inserisco il campo fra quelli da restituire
							keyval kCampiReturn = new keyval();
							kCampiReturn.setKey(k.getKey());
							kCampiReturn.setValue(sCampoRetrun);
							kCampiReturn.setArea(k.getArea());
							kCampiReturn.setCm(k.getCm());
							kCampiReturn.setCategoria(k.getCategoria());
							// Azzero il cursore che conterrà i valori
							// restituiti
							kCampiReturn.valoriCursore = new Vector();

							vAliasCampiReturn.add(kCampiReturn);

							// Inserisco il nome CM del campo fra quelli da
							// restituire
							kCampiReturn = new keyval();
							kCampiReturn.setKey("CM_" + k.getKey());
							kCampiReturn.setValue("CM_" + sCampoRetrun);
							kCampiReturn.setArea(k.getArea());
							kCampiReturn.setCm(k.getCm());
							kCampiReturn.setCategoria(k.getCategoria());
							// Azzero il cursore che conterrà i valori
							// restituiti
							kCampiReturn.valoriCursore = new Vector();
						}
					}
				}

				sFrom.append(dbs.sFrom);
				sWhere.append(dbs.sWhere);
			} catch (Exception e) {
				throw new Exception("Aggiunta condizione Campi. "
						+ e.getMessage());
			}
		}

		// ***********************************CONTROLLO SE ESISTONO JOIN SUI
		// CAMPI
		int joinAttuale = 0;
		for (int i = 0; i < campi.size(); i++) {
			keyval k = (keyval) campi.get(i);
			DoubleString dbs, dbs2;

			int indexJoin = k.getIndexJoin();

			// Se non si tratta di un campo di join skippo
			if (indexJoin == 0 || joinAttuale >= indexJoin)
				continue;

			// Se ho trovato un campo di join sicuramente il campo
			// successivo sarà la chiave di join con quello trovato
			keyval k2 = (keyval) campi.get(++i);

			// Qui setto i valori per il prox ciclo (inizio
			// nuovamente dal primo campo a cercare altri join
			// ma con indice maggiore dell'attuale)
			i = 0;
			joinAttuale = indexJoin;

			/*----------------------Ora setto la condizione di JOIN*/

			// Mi recupero i tipoDoc e gli indici dell'aliasdoc
			String sTipoDoc, listaTipiDoc, indexDoc;
			String sTipoDoc2, listaTipiDoc2, indexDoc2;

			if (k.getArea() != null) {
				// Mapping su tipoDoc
				k.setCm(vu.getGDMapping().getMappingTipoDoc(k.getCm()));

				ModelInformation mi = null;
				mi = (new LookUpDMTable(vu)).lookUpTipoDoc(k.getCm(),
						k.getArea());
				sTipoDoc = mi.getIdTipoDoc();

				// Mapping su campo
				k.setKey(vu.getGDMapping().getMappingCampo(k.getCm(), true,
						k.getKey()));
				// Fine Mapping

				listaTipiDoc = "(" + sTipoDoc + ")";
			} else {
				sTipoDoc = k.getCategoria();
				listaTipiDoc = k.getCategoria();
			}

			if (k2.getArea() != null) {
				// Mapping su tipoDoc
				k2.setCm(vu.getGDMapping().getMappingTipoDoc(k2.getCm()));

				ModelInformation mi = null;
				mi = (new LookUpDMTable(vu)).lookUpTipoDoc(k2.getCm(),
						k2.getArea());
				sTipoDoc2 = mi.getIdTipoDoc();

				// Mapping su campo
				k2.setKey(vu.getGDMapping().getMappingCampo(k2.getCm(), true,
						k2.getKey()));
				// Fine Mapping

				listaTipiDoc2 = "(" + sTipoDoc2 + ")";
			} else {
				sTipoDoc2 = k2.getCategoria();
				listaTipiDoc2 = k2.getCategoria();
			}

			indexDoc = getIndexDoc(sTipoDoc);
			indexDoc2 = getIndexDoc(sTipoDoc2);

			// Fine recupero tipiDoc

			// Mi segno il n° di condizione prima di aggiungerle
			int numCond = iNumCondizione;

			// Aggiungo la prima Condizione
			dbs = getSQLCampo_CondizioneJoin(k.getKey(), listaTipiDoc,
					indexDoc, k.getArea(), k.getCm(), k.getCategoria());
			// Aggiungo la seconda Condizione
			dbs2 = getSQLCampo_CondizioneJoin(k2.getKey(), listaTipiDoc2,
					indexDoc2, k2.getArea(), k2.getCm(), k2.getCategoria());

			// Aggiungo la condizione di join fra i due
			String sTipo = getNomeCampo(dbs.tipoCampo);
			String sTipo2 = getNomeCampo(dbs2.tipoCampo);
			String sJoinWhere = " AND VALO" + numCond + "." + sTipo + "=VALO"
					+ (numCond + 1) + "." + sTipo2 + " ";

			sFrom.append(dbs.sFrom);
			sFrom.append(dbs2.sFrom);
			sWhere.append(dbs.sWhere);
			sWhere.append(dbs2.sWhere);
			sWhere.append(sJoinWhere);

		}

		// ***********************************Esiste almeno una condizione di
		// tipo oggetto file
		for (int i = 0; i < OjfiCondition.size(); i++) {
			String listaTipiDoc, indexDoc;
			keyval k = (keyval) (OjfiCondition.get(i));

			if (!k.getArea().equals("") || !k.getCategoria().equals("")) {
				String sTipoDoc;

				if (!k.getArea().equals("")) {
					// Mapping su tipoDoc
					k.setCm(vu.getGDMapping().getMappingTipoDoc(k.getCm()));

					ModelInformation mi = null;
					mi = (new LookUpDMTable(vu)).lookUpTipoDoc(k.getCm(),
							k.getArea());
					sTipoDoc = mi.getIdTipoDoc();

					listaTipiDoc = "(" + sTipoDoc + ")";
				} else {
					sTipoDoc = k.getCategoria();
					listaTipiDoc = k.getCategoria();
				}

				indexDoc = getIndexDoc(sTipoDoc);
			} else {
				// Significa che devo mettere anche doc (senza numero)
				// come alias di documento
				bExistsDocAliasInFrom = true;
				indexDoc = "";

				if (sLista.equals(""))
					listaTipiDoc = "(-1)";
				else
					listaTipiDoc = sLista;
			}

			String joinLista = " IN " + listaTipiDoc + " ";

			if (!k.getCategoria().equals("")) {
				joinLista = " = MOD" + indexDoc + ".ID_TIPODOC ";
			}

			sWhere.append(" AND DOCU" + indexDoc + ".ID_TIPODOC " + joinLista);
			sWhere.append(" AND OGFI" + indexDoc + ".ID_DOCUMENTO = DOCU"
					+ indexDoc + ".ID_DOCUMENTO");
			
			if (!k.getIsOcr())
				sWhere.append(" AND decode(nvl(OGFI" + indexDoc
						+ ".path_file,' '),' ',CONTAINS(OGFI" + indexDoc
						+ ".TESTOOCR,'" + k.getKey() + "'),CONTAINS(OGFI"
						+ indexDoc + ".\"FILE\",'" + k.getKey() + "'))>0 ");
			else
				sWhere.append(" AND CONTAINS(OGFI" + indexDoc
						+ ".OCR_FILE,'" + k.getKey() + "')>0 ");

			if (sFrom.indexOf(" ,OGGETTI_FILE OGFI" + indexDoc)==-1)
				sFrom.append(" ,OGGETTI_FILE OGFI" + indexDoc);
		}

		// ***********************************Esiste almeno una condizione di
		// fulltext
		if ((condAnd != null && !(condAnd.trim().equals(":@")))
				|| (condOr != null && !(condOr.trim().equals(":@")))
				|| condNot != null || condFullText != null) {
			DoubleString dbs;

			try {
				bExistsDocAliasInFrom = true;
				dbs = getSQLCampo_Condizione(null, null, null,
						calcolaCondizoneFullText(), false, sLista, "", null);
			} catch (Exception e) {
				throw new Exception("Aggiunta condizione FULLTEXT. "
						+ e.getMessage());
			}

			sFrom.append(dbs.sFrom);
			sWhere.append(dbs.sWhere);
		}

		// ***********************************NESSUNA CONDIZIONE CAMPO E NESSUNA
		// FULLTEXT -> SOLO AREA
		if (campi.size() == 0
				&& campiOrdinamento.size() == 0
				&& (condAnd == null && condOr == null && condNot == null && condFullText == null)) {
			DoubleString dbs;

			try {
				dbs = getSQLCampo_Condizione(null, null, null, null, false,
						sLista, "", null);
			} catch (Exception e) {
				throw new Exception("Aggiunta condizione SOLO AREA. "
						+ e.getMessage());
			}

			sFrom.append(dbs.sFrom);
			sWhere.append(dbs.sWhere);
		}

		// ***********************************Esiste almeno una condizione di
		// tipo CAMPOORDINAMENTO
		// PRIMO CLICLO SOLO PER METTERE IN HASH MAP GLI IDTIPODOC
		/*
		 * for(int i=0;i<campiOrdinamento.size();i++) { DoubleString dbs; try {
		 * String sCampo=estraiCampoDaCampoOrdinamento(i);
		 * 
		 * if (estraiCampoOrdinamento(i).indexOf("ORDERBY@")!=-1) continue;
		 * 
		 * String sOrdinamento=estraiAscDescDaCampoOrdinamento(i); String
		 * sArea=estraiAreaDaCampoOrdinamento(i); String
		 * sCm=estraiCmDaCampoOrdinamento(i); String
		 * sCategoria=estraiCategoriaDaCampoOrdinamento(i); String
		 * isOrdinamentoOrReturn=estraiTipoOrdOrReturn(i); String indexDoc;
		 * String listaTipiDoc;
		 * 
		 * if (sArea!=null || sCategoria!=null) { String sTipoDoc;
		 * 
		 * if (sArea!=null) { //Mapping su tipoDoc
		 * sCm=vu.getGDMapping().getMappingTipoDoc(sCm);
		 * 
		 * sTipoDoc=""+(new LookUpDMTable(vu)).lookUpTipoDoc(sCm,sArea);
		 * sTipoDoc=sTipoDoc.substring(0,sTipoDoc.indexOf("@"));
		 * 
		 * //Mapping su tipoDoc e campo
		 * sCampo=vu.getGDMapping().getMappingCampo(sCm,true,sCampo); //Fine
		 * Mapping
		 * 
		 * listaTipiDoc="("+sTipoDoc+")"; } else { sTipoDoc=sCategoria;
		 * listaTipiDoc=sCategoria; }
		 * 
		 * if (!hMapCmCampo.containsKey(sTipoDoc) && (sCampo.indexOf("{")!=-1))
		 * { if (sArea!=null) throw new
		 * Exception("Impossibile aggiungere una espressione di ordinamento per ("
		 * +sArea+","+sCm+
		 * ").\nObbligatorio specificare almento un campo di filtro per area e codice modello scelti"
		 * ); else throw new
		 * Exception("Impossibile aggiungere una espressione di ordinamento per ("
		 * +sCategoria+
		 * ").\nObbligatorio specificare almento un campo di filtro per la categoria scelta"
		 * ); }
		 * 
		 * indexDoc=getIndexDoc(sTipoDoc); } else { indexDoc="";
		 * listaTipiDoc=sLista; bExistsDocAliasInFrom=true; }
		 * 
		 * } catch (Exception e){ throw new
		 * Exception("Primo ciclo Campi Ordinamento. " + e.getMessage()); } }
		 */

		// MI COSTRUISCO IL VETTORE CON I NUMERI DI ALIAS
		Vector vAlias = new Vector();

		for (int i = -1; i < hMapCmCampo.size(); i++) {
			if (i == -1 && bExistsDocAliasInFrom) {
				vAlias.add("");
			} else {
				if (i == -1)
					continue;
				else {
					vAlias.add("" + (i + 1));
				}
			}
		}

		// Almeno un alias (vuoto) deve esistere
		if (vAlias.size() == 0)
			vAlias.add("");

		String indexAliasReturn = "" + vAlias.get(0);
		if (indexaliasIdReturn != null)
			indexAliasReturn = indexaliasIdReturn;

		// ***********************************Esiste almeno una condizione di
		// tipo CAMPOORDINAMENTO
		// PRIMO CLICLO PER METTERE LE CONDIZIONI
		for (int i = 0; i < campiOrdinamento.size(); i++) {
			DoubleString dbs;
			try {
				String sCampo = estraiCampoDaCampoOrdinamento(i);
				String sFormatoDataDaCampo = estraiFormatoDaCampoOrdinamento(i);
				String sCampoOrd = estraiCampoOrdinamento(i);

				if (sCampoOrd.indexOf("ORDERBY@") != -1) {
					String sStringaOrderBy = sCampoOrd.substring(
							sCampoOrd.indexOf("@") + 1, sCampoOrd.length());
					sOrdinamentiOrderByClause.append(sStringaOrderBy);
					continue;
				}

				String sOrdinamento = estraiAscDescDaCampoOrdinamento(i);
				String sArea = estraiAreaDaCampoOrdinamento(i);
				String sCm = estraiCmDaCampoOrdinamento(i);
				String sCategoria = estraiCategoriaDaCampoOrdinamento(i);
				String isOrdinamentoOrReturn = estraiTipoOrdOrReturn(i);
				String indexDoc = "";
				String listaTipiDoc;

				if (sArea != null || sCategoria != null) {
					String sTipoDoc;

					if (sArea != null) {
						// Mapping su tipoDoc
						sCm = vu.getGDMapping().getMappingTipoDoc(sCm);

						ModelInformation mi = null;
						mi = (new LookUpDMTable(vu)).lookUpTipoDoc(sCm, sArea);
						sTipoDoc = mi.getIdTipoDoc();
						// sTipoDoc=sTipoDoc.substring(0,sTipoDoc.indexOf("@"));

						// Mapping su tipoDoc e campo
						sCampo = vu.getGDMapping().getMappingCampo(sCm, true,
								sCampo);
						// Fine Mapping

						listaTipiDoc = "(" + sTipoDoc + ")";
					} else {
						sTipoDoc = sCategoria;
						listaTipiDoc = sCategoria;
					}

					if (!hMapCmCampo.containsKey(sTipoDoc)
							&& (sCampo.indexOf("{") != -1)) {
						if (sArea != null)
							throw new Exception(
									"Impossibile aggiungere una espressione di ordinamento per ("
											+ sArea
											+ ","
											+ sCm
											+ ").\nObbligatorio specificare almento un campo di filtro per area e codice modello scelti");
						else
							throw new Exception(
									"Impossibile aggiungere una espressione di ordinamento per ("
											+ sCategoria
											+ ").\nObbligatorio specificare almento un campo di filtro per la categoria scelta");
					}

					indexDoc = getIndexDoc(sTipoDoc);
				} else {
					indexDoc = "";
					listaTipiDoc = sLista;
					bExistsDocAliasInFrom = true;
				}

				// Controllo se è un campo di ordinamento expression
				if (sCampo.indexOf("{") != -1) {
					if (bIsMaster)
						sCampo = this.convertOrderExpression(sCampo,
								"DOCUMASTER", indexAliasReturn);
					else
						sCampo = this.convertOrderExpression(sCampo, "DOCU",
								indexDoc);

					sOrdinamentiDualSelectClause.append(",TO_CHAR(NULL)");
					sOrdinamentiSelectClause.append(",TO_CHAR(" + sCampo
							+ ") s" + (iNumCondizione - 1));
					sOrdinamentiOrderByClause.append("s" + (iNumCondizione - 1)
							+ " " + sOrdinamento + ",");
					continue;
				}

				dbs = getSQLCampo_Ordinamento(sCampo, indexDoc, listaTipiDoc,
						sCategoria, indexAliasReturn);

				if (sFormatoDataDaCampo!=null) dbs.sFormatoData = sFormatoDataDaCampo;

				String sCampoRetrun = "X";

				if (dbs.tipoCampo.equals("S")) {
					sOrdinamentiDualSelectClause.append(",TO_CHAR(NULL)");
					if (bIsMaster)
						sOrdinamentiSelectClause
								.append(",F_VALORE_IDCAMPO(DOCUMASTER"
										+ indexAliasReturn
										+ ".ID_DOCUMENTO,CADO"
										+ (iNumCondizione - 1) + ".ID_CAMPO) s"
										+ (iNumCondizione - 1));
					else {
						if (dbs.sWhere.indexOf("CADOCALCULATE") != -1)
							sOrdinamentiSelectClause.append("," + dbs.sFrom
									+ " s" + (iNumCondizione - 1));
						else
							sOrdinamentiSelectClause
									.append(",F_VALORE_IDCAMPO(DOCU"
											+ indexAliasReturn
											+ ".ID_DOCUMENTO,CADO"
											+ (iNumCondizione - 1)
											+ ".ID_CAMPO) s"
											+ (iNumCondizione - 1));
					}

					if (!isOrdinamentoOrReturn.equals(keyval.ISCAMPO_RETURN))
						sOrdinamentiOrderByClause.append("s"
								+ (iNumCondizione - 1) + " " + sOrdinamento
								+ ",");

					if (isOrdinamentoOrReturn.equals(keyval.ISCAMPO_RETURN)
							|| isOrdinamentoOrReturn
									.equals(keyval.ISCAMPO_ORDINAMENTO_AND_RETURN)) {
						sReturnSelectClause.append(",s" + (iNumCondizione - 1)
								+ ",CM_s" + (iNumCondizione - 1));
						sCampoRetrun = "s" + (iNumCondizione - 1);

						if (sFromTipoDoc.toString().indexOf(
								",TIPI_DOCUMENTO TIDO" + indexDoc) == -1) {
							sFromTipoDoc.append(",TIPI_DOCUMENTO TIDO"
									+ indexDoc + " ");
							sWhereTipoDoc.append(" AND TIDO" + indexDoc
									+ ".ID_TIPODOC=" + "CADO"
									+ (iNumCondizione - 1) + ".ID_TIPODOC ");
						}

						sOrdinamentiDualSelectClause.append(",TO_CHAR(NULL)");
						sOrdinamentiSelectClause.append(", TIDO" + indexDoc
								+ ".NOME as CM_s" + (iNumCondizione - 1));
					}

				} else if (dbs.tipoCampo.equals("N")) {
					sOrdinamentiDualSelectClause.append(",TO_NUMBER(NULL)");
					if (bIsMaster)
						sOrdinamentiSelectClause
								.append(",TO_NUMBER(F_VALORE_IDCAMPO(DOCUMASTER"
										+ indexAliasReturn
										+ ".ID_DOCUMENTO,CADO"
										+ (iNumCondizione - 1)
										+ ".ID_CAMPO)) n"
										+ (iNumCondizione - 1));
					else {
						if (dbs.sWhere.indexOf("CADOCALCULATE") != -1)
							sOrdinamentiSelectClause.append(",TO_NUMBER("
									+ dbs.sFrom + ") n" + (iNumCondizione - 1));
						else
							sOrdinamentiSelectClause
									.append(",TO_NUMBER(F_VALORE_IDCAMPO(DOCU"
											+ indexAliasReturn
											+ ".ID_DOCUMENTO,CADO"
											+ (iNumCondizione - 1)
											+ ".ID_CAMPO)) n"
											+ (iNumCondizione - 1));
					}

					if (!isOrdinamentoOrReturn.equals(keyval.ISCAMPO_RETURN))
						sOrdinamentiOrderByClause.append("TO_NUMBER(n"
								+ (iNumCondizione - 1) + ") " + sOrdinamento
								+ ",");

					if (isOrdinamentoOrReturn.equals(keyval.ISCAMPO_RETURN)
							|| isOrdinamentoOrReturn
									.equals(keyval.ISCAMPO_ORDINAMENTO_AND_RETURN)) {
						sReturnSelectClause.append(",n" + (iNumCondizione - 1)
								+ ",CM_n" + (iNumCondizione - 1));
						sCampoRetrun = "n" + (iNumCondizione - 1);
						if (sFromTipoDoc.toString().indexOf(
								",TIPI_DOCUMENTO TIDO" + indexDoc) == -1) {
							sFromTipoDoc.append(",TIPI_DOCUMENTO TIDO"
									+ indexDoc + " ");
							sWhereTipoDoc.append(" AND TIDO" + indexDoc
									+ ".ID_TIPODOC=" + "CADO"
									+ (iNumCondizione - 1) + ".ID_TIPODOC ");
						}

						sOrdinamentiDualSelectClause.append(",TO_CHAR(NULL)");
						sOrdinamentiSelectClause.append(", TIDO" + indexDoc
								+ ".NOME as CM_n" + (iNumCondizione - 1));
					}
				} else {
					sOrdinamentiDualSelectClause.append(",TO_CHAR(NULL)");
					if (bIsMaster)
						sOrdinamentiSelectClause
								.append(",F_VALORE_IDCAMPO(DOCUMASTER"
										+ indexAliasReturn
										+ ".ID_DOCUMENTO,CADO"
										+ (iNumCondizione - 1) + ".ID_CAMPO) d"
										+ (iNumCondizione - 1));
					else {
						if (dbs.sWhere.indexOf("CADOCALCULATE") != -1)
							sOrdinamentiSelectClause.append("," + dbs.sFrom
									+ " d" + (iNumCondizione - 1));
						else
							sOrdinamentiSelectClause
									.append(",F_VALORE_IDCAMPO(DOCU"
											+ indexAliasReturn
											+ ".ID_DOCUMENTO,CADO"
											+ (iNumCondizione - 1)
											+ ".ID_CAMPO) d"
											+ (iNumCondizione - 1));
					}

					if (!isOrdinamentoOrReturn.equals(keyval.ISCAMPO_RETURN))
						sOrdinamentiOrderByClause
								.append("TO_DATE(d" + (iNumCondizione - 1)
										+ ",'" + dbs.sFormatoData + "') "
										+ sOrdinamento + ",");

					if (isOrdinamentoOrReturn.equals(keyval.ISCAMPO_RETURN)
							|| isOrdinamentoOrReturn
									.equals(keyval.ISCAMPO_ORDINAMENTO_AND_RETURN)) {
						sReturnSelectClause.append(",d" + (iNumCondizione - 1)
								+ ",CM_d" + (iNumCondizione - 1));
						sCampoRetrun = "d" + (iNumCondizione - 1);

						if (sFromTipoDoc.toString().indexOf(
								",TIPI_DOCUMENTO TIDO" + indexDoc) == -1) {
							sFromTipoDoc.append(",TIPI_DOCUMENTO TIDO"
									+ indexDoc + " ");
							sWhereTipoDoc.append(" AND TIDO" + indexDoc
									+ ".ID_TIPODOC=" + "CADO"
									+ (iNumCondizione - 1) + ".ID_TIPODOC ");
						}

						sOrdinamentiDualSelectClause.append(",TO_CHAR(NULL)");
						sOrdinamentiSelectClause.append(", TIDO" + indexDoc
								+ ".NOME as CM_d" + (iNumCondizione - 1));
					}
				}

				if (!sCampoRetrun.equals("X")) {
					// Inserisco il campo fra quelli da restituire
					keyval kCampiReturn = new keyval();
					kCampiReturn.setKey(sCampo);
					kCampiReturn.setValue(sCampoRetrun);
					kCampiReturn.setArea(sArea);
					kCampiReturn.setCm(sCm);
					kCampiReturn.setCategoria(sCategoria);
					// Azzero il cursore che conterrà i valori restituiti
					kCampiReturn.valoriCursore = new Vector();

					vAliasCampiReturn.add(kCampiReturn);
				}

				if (dbs.sWhere.indexOf("CADOCALCULATE") == -1) {
					sFrom.append(dbs.sFrom);
					sWhere.append(dbs.sWhere);
				} else {
					if (campi.size() == 0) {
						String joinLista;
						if (listaTipiDoc.indexOf(",") != -1)
							joinLista = " IN " + listaTipiDoc + " ";
						else
							joinLista = " = " + listaTipiDoc + " ";

						sWhere.append("  AND DOCU" + indexAliasReturn
								+ ".ID_TIPODOC " + joinLista);
					}
				}
			} catch (Exception e) {
				throw new Exception("Aggiunta condizione Campi Ordinamento. "
						+ e.getMessage());
			}
		}

		// Ho impostato il filtro dalla workarea e sono in master
		// quindi devo applicare il filtro all'id master non ai figli
		// , mi costruisco una query esterna che filtra ulteriormente gli
		// id estratti internamente
		if (bIsMaster && condFiltroWAreaCasoMaster != null) {
			sSql.append("SELECT " + hintMasterWorkareaFiltro + " ID,TI,DA,CR ");
			sSql.append("  FROM ( ");
		}

		sSql.append("SELECT ID,TI,DA,CR " + sReturnSelectClause.toString());
		sSql.append("  FROM ( ");

		if (bIsMaster)
			sSql.append("SELECT DOCU" + indexAliasReturn
					+ ".ID_DOCUMENTO_PADRE ID, DOCUMASTER" + indexAliasReturn
					+ ".ID_TIPODOC TI, DOCUMASTER" + indexAliasReturn
					+ ".DATA_AGGIORNAMENTO DA , DOCUMASTER" + indexAliasReturn
					+ ".CODICE_RICHIESTA CR "
					+ sOrdinamentiSelectClause.toString() + " ");
		else
			sSql.append("SELECT " + hintOnlyOneField + " DOCU"
					+ indexAliasReturn + ".ID_DOCUMENTO ID, DOCU"
					+ indexAliasReturn + ".ID_TIPODOC TI, DOCU"
					+ indexAliasReturn + ".DATA_AGGIORNAMENTO DA , DOCU"
					+ indexAliasReturn + ".CODICE_RICHIESTA CR "
					+ sOrdinamentiSelectClause.toString() + " ");
		sSql.append("FROM ");

		for (int i = 0; i < vAlias.size(); i++) {
			if (i > 0)
				sSql.append(" ,");

			sSql.append("     DOCUMENTI DOCU" + vAlias.get(i));

			if (bIsMaster) {
				sSql.append("  ,DOCUMENTI DOCUMASTER" + vAlias.get(i));
				// sSql.append("  STATI_DOCUMENTO STDOMASTER"+vAlias.get(i)+", ");
			}

			// sSql.append("     STATI_DOCUMENTO STDO"+vAlias.get(i)+" ");

			if (hMapCategorie.containsKey("" + vAlias.get(i))) {
				sSql.append("  ,CATEGORIE_MODELLO CAT_MOD" + vAlias.get(i));
				sSql.append("  ,MODELLI MOD" + vAlias.get(i));
			}
		}

		sSql.append(sFrom + " ");
		sSql.append(sFromTipoDoc + " ");
		sSql.append("WHERE 1=1 ");
		sSql.append(sWhere);

		String sStati = "('CA','RE','PB')";
		if (bTrovaAnchePreBozza)
			sStati = "('CA','RE')";

		for (int i = 0; i < vAlias.size(); i++) {
			if (hMapCategorie.containsKey("" + vAlias.get(i))) {
				sSql.append(" AND CAT_MOD" + vAlias.get(i) + ".CATEGORIA='"
						+ hMapCategorie.get("" + vAlias.get(i)) + "' ");
				sSql.append(" AND CAT_MOD" + vAlias.get(i) + ".AREA = MOD"
						+ vAlias.get(i) + ".AREA ");
				sSql.append(" AND CAT_MOD" + vAlias.get(i)
						+ ".CODICE_MODELLO = MOD" + vAlias.get(i)
						+ ".CODICE_MODELLO ");
			}
			// sSql.append("  AND STDO"+vAlias.get(i)+".ID_DOCUMENTO=DOCU"+vAlias.get(i)+".ID_DOCUMENTO ");
			sSql.append("  AND DOCU" + vAlias.get(i)
					+ ".STATO_DOCUMENTO NOT IN " + sStati + " ");
			// sSql.append("  AND STDO"+vAlias.get(i)+".ID_STATO = (SELECT MAX(ID_STATO) ");
			// sSql.append("  					FROM STATI_DOCUMENTO S ");
			// sSql.append("  				   WHERE S.ID_DOCUMENTO=DOCU"+vAlias.get(i)+".ID_DOCUMENTO) ");

			if (bIsMaster) {
				sSql.append("AND DOCUMASTER" + vAlias.get(i)
						+ ".ID_DOCUMENTO=DOCU" + vAlias.get(i)
						+ ".ID_DOCUMENTO_PADRE ");
				// sSql.append("  AND STDOMASTER"+vAlias.get(i)+".ID_DOCUMENTO=DOCUMASTER"+vAlias.get(i)+".ID_DOCUMENTO ");
				sSql.append("  AND DOCUMASTER" + vAlias.get(i)
						+ ".STATO_DOCUMENTO NOT IN " + sStati + " ");
				/*
				 * sSql.append("  AND STDOMASTER"+vAlias.get(i)+
				 * ".ID_STATO = (SELECT MAX(ID_STATO) ");
				 * sSql.append("  					FROM STATI_DOCUMENTO S ");
				 * sSql.append("  				   WHERE S.ID_DOCUMENTO=DOCUMASTER"
				 * +vAlias.get(i)+".ID_DOCUMENTO) ");
				 */
			}
		}

		if (idDoc != null) {
			if (idDoc.size() > 0) {
				if (idDoc.size() == 1)
					sSql.append(" AND DOCU" + indexAliasReturn
							+ ".ID_DOCUMENTO=" + idDoc.get(0) + " ");
				else
					sSql.append(" AND "
							+ getSequenzaBlocchi(idDoc, indexAliasReturn) + " ");
			}
		}

		sSql.append(sWhereTipoDoc);
		if (!extraConditionSearch.equals(""))
			sSql.append(" AND " + extraConditionSearch + " ");

		if (!vu.getByPassCompetenze()) {
			sSql.append("UNION ");
			sSql.append("SELECT TO_NUMBER(NULL),TO_NUMBER(NULL),TO_DATE(NULL),TO_CHAR(NULL) "
					+ sOrdinamentiDualSelectClause.toString() + " ");
			sSql.append("  FROM DUAL ) A, DUAL ");
		} else {
			sSql.append(" ) A ");
		}

		if (!vu.getByPassCompetenze()) {
			sSql.append("  WHERE GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',");
			sSql.append("  								    A.ID,");
			sSql.append("  								    '" + controlloCompetenzaQuery + "',");
			sSql.append("  								    '" + vu.getUser() + "',");
			sSql.append("  								    F_TRASLA_RUOLO('" + vu.getUser()
					+ "','GDMWEB','GDMWEB'),");
			sSql.append("  								    TO_CHAR(SYSDATE,'dd/mm/yyyy')");
			if (bControllaPadre)
				sSql.append("                                    ,'Y'");
			sSql.append("                                    )||DUMMY = '1X' ");
			sSql.append("");
			if (!sFullTextWarea.equals("")) sSql.append(" AND " + sFullTextWarea); 
			
			if (bIsMaster && (!sCatMaster.equals(""))) {
				sSql.append(" AND F_DOCUMENTO_IN_CATEGORIA(A.ID,'"+Global.replaceAll(sCatMaster, "'", "''")+"') <> 0 "); 
		    }
		}
		else {
			sSql.append("  WHERE  1=1 ");
			if (!sFullTextWarea.equals("")) sSql.append(" AND "+sFullTextWarea); 
			
			if (bIsMaster && (!sCatMaster.equals(""))) {
				sSql.append(" AND F_DOCUMENTO_IN_CATEGORIA(A.ID,'"+Global.replaceAll(sCatMaster, "'", "''")+"') <> 0 "); 
		    }
		}
		// else
		// sSql.append("  WHERE ID IS NOT NULL ");
		if (!bEscludiOrdinamento)
			sSql.append(" ORDER BY " + sOrdinamentiOrderByClause.toString()
					+ " DA DESC");
// 
		// Ho impostato il filtro dalla workarea e sono in master
		// quindi devo applicare il filtro all'id master non ai figli
		// , mi costruisco una query esterna che filtra ulteriormente gli
		// id estratti internamente
		if (bIsMaster && condFiltroWAreaCasoMaster != null) {
			sSql.append(") A, VALORI VALOEXTERN");
			sSql.append(" WHERE VALOEXTERN.ID_DOCUMENTO = A.ID ");
			if ((bIsRicercaPuntualeGlobale == false))
				sSql.append("  AND "
						+ calcolaANDContains(
								"CONTAINS(VALOEXTERN.VALORE_CLOB,'",
								Global.replaceAll(
										protectReserveWord(condFiltroWAreaCasoMaster),
										"'", "''"), "')>0 ") + " ");
			else {
				if (condFiltroWAreaCasoMaster.indexOf("%") != -1)
					sSql.append("  AND VALOEXTERN.VALORE_STRINGA like '"
							+ Global.replaceAll(condFiltroWAreaCasoMaster, "'",
									"''") + "' ");
				else
					sSql.append("  AND VALOEXTERN.VALORE_STRINGA='"
							+ Global.replaceAll(condFiltroWAreaCasoMaster, "'",
									"''") + "' ");
			}
		}

		return sSql.toString();
	}

	/**
	 * Costruisce la select principale della ricerca
	 * 
	 * @throws Exception
	 */
	private void execRicerca(String sSql) throws Exception {

		try {
			dbOp = connect();
   
			if (vu.Global.PRINT_QUERY.equals("S")) {
				System.out.println("[INFO Query di Ricerca (VERTICALE) - "
						+ UtilityDate.now("dd/MM/yyyy HH:mm:ss") + "]: "
						+ sSql.toString());
			}
			// System.out.println("[INFO Query di Ricerca (VERTICALE) - "+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"]: "+sSql.toString());
			if (log4jSuiteDb != null)
				log4jSuiteDb
						.ScriviLog(sSql.toString(), "Costruzione SQL",
								Global.TAG_RICERCA_SEMPLICE_CREATESQL,
								LogDb.INFO_LEVEL);

			lastQueryExecuted = sSql.toString();
			dbOp.setStatement(sSql.toString());

			dbOp.getStmSql().setQueryTimeout(timeout);
			//dbOp.getStmSql().setQueryTimeout(3600);

			if (vu.Global.PRINT_QUERY.equals("S"))
				elpsTime.start("Esecuzione della Query", " ");
			dbOp.execute();
			if (vu.Global.PRINT_QUERY.equals("S"))
				elpsTime.stop();

			if (log4jSuiteDb != null) {
				if (elpsTime != null)
					log4jSuiteDb.ScriviLog(sSql.toString(), "Esecuzione SQL",
							Global.TAG_RICERCA_SEMPLICE_EXECSQL,
							LogDb.INFO_LEVEL, elpsTime.getLastElpsTime());
				else
					log4jSuiteDb.ScriviLog(sSql.toString(), "Esecuzione SQL",
							Global.TAG_RICERCA_SEMPLICE_EXECSQL,
							LogDb.INFO_LEVEL);
			}

			ResultSet rst = dbOp.getRstSet();

			if (fetchInit != 0) {
				int conta = 1;
				while (conta++ != fetchInit)
					rst.next();
			}

			// Aggiungo il keyval degli idDocumento
			keyval kCampiReturn = new keyval();
			kCampiReturn.setKey("ID");
			kCampiReturn.setValue("ID");

			// Azzero il cursore che conterrà i valori restituiti
			kCampiReturn.valoriCursore = new Vector();

			vAliasCampiReturn.add(kCampiReturn);

			// FINE IDDOCUMENTO

			// Aggiungo il keyval degli CR
			kCampiReturn = new keyval();
			kCampiReturn.setKey("_CR_");
			kCampiReturn.setValue("CR");

			// Azzero il cursore che conterrà i valori restituiti
			kCampiReturn.valoriCursore = new Vector();

			vAliasCampiReturn.add(kCampiReturn);
			// FINE CR

			int conta = 0;
			while (rst.next() && conta++ != fetchSize) {
				documentList.add("" + rst.getLong(1));
				documentListWithIdTipoDoc.add(rst.getLong("ti") + "@"
						+ rst.getLong(1));

				// Estraggo i campi da restituire
				for (int i = 0; i < vAliasCampiReturn.size(); i++) {
					keyval kAppoggio = (keyval) vAliasCampiReturn.get(i);

					kAppoggio.valoriCursore.add(rst.getString(kAppoggio
							.getVal()));

					vAliasCampiReturn.set(i, kAppoggio);
				}
			}

			bIsLastRow = rst.isAfterLast();

			disconnect();

		} catch (Exception e) {
			if (log4jSuiteDb != null) {
				if (elpsTime != null)
					log4jSuiteDb.ScriviLog(sSql.toString(), e.getMessage(),
							Global.TAG_RICERCA_SEMPLICE_ERRORSQL,
							LogDb.ERROR_LEVEL, elpsTime.getLastElpsTime());
				else
					log4jSuiteDb.ScriviLog(sSql.toString(), e.getMessage(),
							Global.TAG_RICERCA_SEMPLICE_ERRORSQL,
							LogDb.ERROR_LEVEL);
			}
			disconnect();
			throw new Exception("Esecuzione query ricerca: " + sSql.toString()
					+ "\nErrore: " + e.getMessage());
		}
	}

	/**
	 * @return Restituisce l'SQL per calcolare la lista dei tipi documento in
	 *         funzione dei casi ABCDE
	 */
	private String getSQLListaModelli() throws Exception {
		StringBuffer sSql = new StringBuffer();

		// CASO D - FULL TEXT
		// Non ho impostato area e nessun
		// nessun campo, nessun tipoDoc, ma solo
		// un criterio full text.
		// Non serve creare la lista dei tipiDoc
		if (vCm.size() == 0 && area == null && campi.size() == 0)
			return "";

		// CASO A - ASSEGNAZIONE DELLA SOLA AREA
		if (area != null && campi.size() == 0 && vCm.size() == 0) {
			sSql.append("SELECT ID_TIPODOC ");
			sSql.append("FROM MODELLI ");
			sSql.append("WHERE CODICE_MODELLO_PADRE IS NULL ");
			sSql.append("AND AREA='" + area + "'");

			return sSql.toString();
		}

		// CASO B - ASSEGNAZIONE DELL'AREA E DI CAMPI (NO TIPIDOC)
		if (area != null && campi.size() != 0 && vCm.size() == 0) {
			for (int i = 0; i < campi.size(); i++) {
				sSql.append("SELECT ID_TIPODOC ");
				sSql.append("FROM MODELLI MO, ");
				sSql.append("     DATI_MODELLO DM ");
				sSql.append("WHERE MO.AREA = DM.AREA ");
				sSql.append("AND MO.CODICE_MODELLO = DM.CODICE_MODELLO ");
				sSql.append("AND MO.CODICE_MODELLO_PADRE IS NULL ");
				sSql.append("AND DM.DATO = '"
						+ ((keyval) campi.elementAt(i)).getKey() + "' ");
				sSql.append("AND MO.AREA = '" + area + "' ");

				if (i != campi.size() - 1)
					sSql.append(" INTERSECT ");
			}

			return sSql.toString();
		}

		// CASO C - ASSEGNAZIONE DEI CAMPI (NO TIPI DOC, NO AREA)
		if (area == null && campi.size() != 0 && vCm.size() == 0) {
			for (int i = 0; i < campi.size(); i++) {
				sSql.append("SELECT TD.ID_TIPODOC ");
				sSql.append("FROM TIPI_DOCUMENTO TD, ");
				sSql.append("     CAMPI_DOCUMENTO CD ");
				sSql.append("WHERE CD.NOME='"
						+ ((keyval) campi.elementAt(i)).getKey() + "'");
				sSql.append("AND TD.ID_TIPODOC = CD.ID_TIPODOC ");

				if (i != campi.size() - 1)
					sSql.append(" INTERSECT ");
			}

			return sSql.toString();
		}

		// CASO E - ASSEGNAZIONE DEI TIPIDOC
		if (vCm.size() != 0) {

			for (int i = 0; i < vCm.size(); i++) {
				String sCm = "" + vCm.get(i);

				try {
					sCm = vu.getGDMapping().getMappingTipoDoc(sCm);
				} catch (Exception e) {
					throw new Exception(
							"Costruzione lista modelli. Errore nel mapping: \n"
									+ e.getMessage());
				}

				sSql.append("SELECT ID_TIPODOC ");
				sSql.append("FROM MODELLI ");

				sSql.append("WHERE CODICE_MODELLO='" + sCm + "' ");

				if (!(vCmArea.get(i).equals("")))
					sSql.append("AND AREA = '" + vCmArea.get(i) + "' ");
				else if (area != null)
					sSql.append("AND AREA = '" + area + "' ");

				if (i != vCm.size() - 1)
					sSql.append(" UNION ");

			}

			return sSql.toString();
		}

		return sSql.toString();
	}

	/**
	 * Restituisce l'SQL interna (from e where condition) da mettere nelle JOIN
	 * per l'ordinamento
	 * 
	 * @param campo
	 */
	private DoubleString getSQLCampo_Ordinamento(String campo,
			String indexAliasDoc, String listaTipiDoc, String cat,
			String indexMaster) throws Exception {
		StringBuffer sFrom = new StringBuffer("");
		StringBuffer sWhere = new StringBuffer("");
		String formatoData;

		String tipo = (lookupTipoValore(campo, null, null)).getTipo();
		formatoData = (lookupTipoValore(campo, null, null)).getFormatoData();

		if (formatoData == null || formatoData.equals(""))
			formatoData = sFormatoData;

		formatoData = Global.replaceAll(formatoData, "hh:", "hh24:");

		String joinLista;

		if (listaTipiDoc.indexOf(",") != -1)
			joinLista = " IN " + listaTipiDoc + " ";
		else
			joinLista = " = " + listaTipiDoc + " ";

		if (cat != null)
			joinLista = " = MOD" + indexAliasDoc + ".ID_TIPODOC ";

		if (cat != null || bIsMaster || (joinLista.indexOf("=") != -1)) {
			sFrom.append("  ,CAMPI_DOCUMENTO CADO" + iNumCondizione);
			// sFrom.append("  ,VALORI VALO"+iNumCondizione);

			if (bIsMaster) {
				sWhere.append("  AND CADO" + iNumCondizione
						+ ".ID_TIPODOC = DOCUMASTER" + indexMaster
						+ ".ID_TIPODOC ");
			} else {
				sWhere.append("  AND CADO" + iNumCondizione + ".ID_TIPODOC"
						+ joinLista);
				if (campi.size() == 0)
					sWhere.append("  AND DOCU" + indexMaster + ".ID_TIPODOC "
							+ joinLista);
			}

			sWhere.append("AND CADO" + iNumCondizione + ".NOME = '" + campo
					+ "' ");
			// sWhere.append("AND VALO"+iNumCondizione+".ID_CAMPO+0 = CADO"+iNumCondizione+".ID_CAMPO ");

			/*
			 * if (bIsMaster)
			 * sWhere.append("AND VALO"+iNumCondizione+".ID_DOCUMENTO = DOCUMASTER"
			 * +indexMaster+".ID_DOCUMENTO "); else
			 * sWhere.append("AND VALO"+iNumCondizione
			 * +".ID_DOCUMENTO = DOCU"+indexAliasDoc+".ID_DOCUMENTO ");
			 */
		} else {
			sFrom.append("(SELECT MAX(NVL(DBMS_LOB.SUBSTR(VALORE_CLOB, 4000), NVL(TO_CHAR(VALORE_DATA, nvl(REPLACE(formato_data,'hh:','hh24:'),'dd/mm/yyyy')),TO_CHAR(VALORE_NUMERO) )))  ");
			sFrom.append("  FROM VALORI,campi_documento,dati_modello, dati ");
			sFrom.append(" WHERE ID_DOCUMENTO = DOCU" + indexMaster
					+ ".ID_DOCUMENTO ");
			sFrom.append("   AND campi_documento.id_tipodoc " + joinLista);
			sFrom.append("   and campi_documento.nome='" + campo + "'");
			sFrom.append("   AND campi_documento.id_campo = valori.ID_CAMPO");
			sFrom.append("   AND campi_documento.id_campo = dati_modello.id_campo");
			sFrom.append("	AND dati_modello.AREA_DATO = dati.area");
			sFrom.append("	AND dati_modello.dato = dati.dato)");

			sWhere.append("CADOCALCULATE");
		}

		iNumCondizione++;

		return new DoubleString(sFrom.toString(), sWhere.toString(), tipo,
				formatoData);
	}

	private DoubleString getSQLCampo_Condizione(String campo, String valore,
			String operatore, String condizioneFullText,
			boolean bIsRicercaPuntualeCampo, String listaTipiDoc,
			String indiceAliasDocumento, String valueNvl) throws Exception {
		return getSQLCampo_Condizione(campo, valore, operatore,
				condizioneFullText, bIsRicercaPuntualeCampo, listaTipiDoc,
				indiceAliasDocumento, null, null, null, null, valueNvl);
	}

	/**
	 * Restituisce l'SQL interna (from e where condition) da mettere nelle JOIN
	 * Se condizioneFullText==null è un SQL di tipo CAMPO altrimenti è la
	 * condizione FULLTEXT
	 * 
	 * @param campo
	 * @param valore
	 * @param condizioneFullText
	 * @return
	 */
	private DoubleString getSQLCampo_Condizione(String campo, String valore,
			String operatore, String condizioneFullText,
			boolean bIsRicercaPuntualeCampo, String listaTipiDoc,
			String indiceAliasDocumento, String area, String cm, String cat,
			String tipoUguaglianza, String campoNVL) throws Exception {
		String tipo = null, tipoOperatore = "";
		boolean isCampo = false, isOnlyArea = false;

		String formatoData;

		formatoData = (lookupTipoValore(campo, area, cm)).getFormatoData();

		if (formatoData == null || formatoData.equals(""))
			formatoData = sFormatoData;

		formatoData = Global.replaceAll(formatoData, "hh:", "hh24:");

		/**** Testo tipo e se è campo, fulltext o solo area ******/

		// Solo Area
		if (campo == null && valore == null && operatore == null
				&& condizioneFullText == null)
			isOnlyArea = true;
		else {
			// E' un Campo
			if (condizioneFullText == null) {
				isCampo = true;

				// Sono nel caso isNull
				if (valore.equals("is null") || valore.equals("is not null")) {
					tipo = (lookupTipoValore(campo, area, cm)).getTipo();
				} else {

					// Se mi trovo nel caso settaChiave(campo,valore) dove il
					// campo è di tipo N o D
					// setto l'operatore a '='
					if ((operatore.equals("contains"))
							&& (!(lookupTipoValore(campo, area, cm)).getTipo()
									.equals("S")))
						operatore = "=";

					if ((operatore.equals("<>"))
							&& (!lookupTipoValore(campo, area, cm).equals("S")))
						operatore = "<>";

					// Che tipo è l'operatore??
					if (!lookupTipoOperatore(operatore).equals("S"))
						tipoOperatore = "BETWEEN";

					// Che tipo è valore1? (valore2 , se esiste, sarà dello
					// stesso tipo...)
					if (operatore.equals("contains"))
						tipo = "S";
					else {
						// tipo=lookupTipoValore(valore);
						tipo = (lookupTipoValore(campo, area, cm)).getTipo();
						if (DateUtility.isDateValid(valore, sFormatoData))
							tipo = "D";
						else {
							// tipo=lookupTipoValore(valore);
							tipo = (lookupTipoValore(campo, area, cm))
									.getTipo();

							if (tipo.equals("S") && !operatore.equals("<>"))
								throw new Exception("Valore <" + valore
										+ "> passato in ricerca "
										+ "per campo <" + campo
										+ "> non valido");
						}
					}
				}

			}
			// E' un FullText
			else
				isCampo = false;
		}

		/*
		 * System.out.println("Campo= "+campo);
		 * System.out.println("Valore= "+valore);
		 * System.out.println("Operatore= "+operatore);
		 * System.out.println("condizioneFullText= "+condizioneFullText);
		 * System.out.println("Tipo= "+tipo);
		 * System.out.println("TipoOperatore= "+tipoOperatore);
		 * //System.out.println("lookupTipoValore= "+lookupTipoValore(campo));
		 * //
		 * System.out.println("lookupTipoOperatore= "+lookupTipoOperatore(operatore
		 * ));
		 */

		/************** FINE Test *************/
		StringBuffer sFrom = new StringBuffer("");
		StringBuffer sWhere = new StringBuffer("");
		StringBuffer sqlIdCampo = new StringBuffer("");
		String sListaIdCampo = "";
		String outer = "";

		// if (valore.equals("is null")) outer="(+)";
		if (valore != null && !valore.equals("is null")
				&& !valore.equals("is not null"))
			valore = valore.toUpperCase();

		if (isCampo || condizioneFullText != null) {
			sFrom.append("  ,CAMPI_DOCUMENTO CADO" + iNumCondizione);
		}
		if (!isOnlyArea)
			sFrom.append("  ,VALORI VALO" + iNumCondizione);

		if (!listaTipiDoc.equals("")) {
			String joinLista = "IN " + listaTipiDoc + " ";

			if (cat != null) {
				joinLista = " = MOD" + indiceAliasDocumento + ".ID_TIPODOC ";
			}

			if (isCampo || condizioneFullText != null) {
				sWhere.append("  AND CADO" + iNumCondizione + ".ID_TIPODOC "
						+ outer + joinLista);
				sWhere.append("  AND DOCU" + indiceAliasDocumento
						+ ".ID_TIPODOC " + joinLista);
			} else
				sWhere.append("  AND DOCU" + indiceAliasDocumento
						+ ".ID_TIPODOC " + joinLista);

			if (isCampo) {
				sWhere.append("AND CADO" + iNumCondizione + ".NOME " + outer
						+ " = '" + campo + "' ");
				if (cat != null) {
					sqlIdCampo.append("SELECT ID_CAMPO ");
					sqlIdCampo
							.append("  FROM CATEGORIE_MODELLO CM, MODELLI M, CAMPI_DOCUMENTO CADO ");
					sqlIdCampo.append(" WHERE CADO.ID_TIPODOC IN M.ID_TIPODOC");
					sqlIdCampo.append("   AND CADO.NOME " + outer + " = '"
							+ campo + "' ");
					sqlIdCampo.append("   AND CM.CATEGORIA='" + cat + "' ");
					sqlIdCampo.append("   AND CM.AREA=M.AREA ");
					sqlIdCampo
							.append("   AND CM.CODICE_MODELLO=M.CODICE_MODELLO ");
					sqlIdCampo.append("   AND M.CODICE_MODELLO_PADRE IS NULL ");
				} else {
					sqlIdCampo.append("SELECT ID_CAMPO ");
					sqlIdCampo.append("  FROM CAMPI_DOCUMENTO CADO ");
					sqlIdCampo.append(" WHERE CADO.ID_TIPODOC " + outer
							+ joinLista);
					sqlIdCampo.append("   AND CADO.NOME " + outer + " = '"
							+ campo + "' ");
				}
			}

			if (!isOnlyArea) {
				String sZero = "";
				if (!isCampo || valore.equals("%"))
					sZero = "+0";

				sWhere.append("AND VALO" + iNumCondizione + ".ID_CAMPO" + sZero
						+ " = CADO" + iNumCondizione + ".ID_CAMPO " + outer);
			}

		}

		if (!isOnlyArea)
			sWhere.append("AND VALO" + iNumCondizione + ".ID_DOCUMENTO "
					+ outer + " = DOCU" + indiceAliasDocumento
					+ ".ID_DOCUMENTO ");
		else if (isCampo || condizioneFullText != null)
			sWhere.append("AND CADO" + iNumCondizione + ".ID_TIPODOC = DOCU"
					+ indiceAliasDocumento + ".ID_TIPODOC ");

		if (!isOnlyArea) {
			if (isCampo) {
				if (!(tipo.equals("S")) && valore.equals("")) {
					throw new Exception("Valore <" + valore
							+ "> passato in ricerca " + "per campo <" + campo
							+ "> non valido");
				}

				// Caso is null or is not null
				if (valore.equals("is null") || valore.equals("is not null")) {
					if (tipo.equals("S"))
						sWhere.append("  AND VALO" + iNumCondizione
								+ ".VALORE_STRINGA " + outer + " " + valore
								+ " ");
					else if (tipo.equals("N"))
						sWhere.append("  AND VALO" + iNumCondizione
								+ ".VALORE_NUMERO " + outer + " " + valore
								+ " ");
					else
						sWhere.append("  AND VALO" + iNumCondizione
								+ ".VALORE_DATA " + outer + " " + valore + " ");
				}
				// Caso between
				else if (tipoOperatore.equals("BETWEEN")) {
					if (tipo.equals("N")) {
						if (campoNVL == null)
							sWhere.append("  AND VALO" + iNumCondizione
									+ ".VALORE_NUMERO BETWEEN " + valore
									+ " AND " + operatore + " ");
						else
							sWhere.append("  AND NVL(VALO" + iNumCondizione
									+ ".VALORE_NUMERO," + campoNVL
									+ ") BETWEEN " + valore + " AND "
									+ operatore + " ");
					} else if (tipo.equals("D")) {
						if (valore.toUpperCase().indexOf("SYSDATE") == -1)
							valore = "TO_DATE('" + valore + "','"
									+ sFormatoData + "')";

						if (operatore.toUpperCase().indexOf("SYSDATE") == -1)
							operatore = "TO_DATE('" + operatore + "','"
									+ sFormatoData + "')";

						String sCampo;
						if (campoNVL != null) {
							if (campoNVL.toUpperCase().indexOf("SYSDATE") != -1)
								sCampo = "NVL(VALO" + iNumCondizione
										+ ".VALORE_DATA," + campoNVL + ") ";
							else
								sCampo = "NVL(VALO" + iNumCondizione
										+ ".VALORE_DATA,TO_DATE('" + campoNVL
										+ "','" + sFormatoData + "')) ";
						} else
							sCampo = "VALO" + iNumCondizione + ".VALORE_DATA";

						sWhere.append("  AND " + sCampo + " BETWEEN " + valore
								+ " AND " + operatore + " ");
					}
				}
				// Caso con operatore
				else {
					String sCampo;

					if (tipo.equals("S")) {
						// VENGO DALLA PAGINA DEI PARAMETRI DELLA QUERY
						// PARAMETRICA
						// E QUINDI DEVO DARE RETTA ALLA TENDINA CON I TRE
						// POSSIBILI VALORI:
						// Inizia per,Contiene,Frase Esatta
						if (tipoUguaglianza != null) {
							if (tipoUguaglianza.equals("LIKE"))
								sWhere.append("  AND VALO" + iNumCondizione
										+ ".VALORE_STRINGA like '"
										+ Global.replaceAll(valore, "'", "''")
										+ "%' ");
							else if (tipoUguaglianza.equals("ESATTA"))
								sWhere.append("  AND VALO" + iNumCondizione
										+ ".VALORE_STRINGA='"
										+ Global.replaceAll(valore, "'", "''")
										+ "' ");
							else if (tipoUguaglianza.equals("CONTAINS")) {
								sListaIdCampo = costruzioneListaIdCampo(sqlIdCampo
										.toString());

								sWhere.append("  AND "
										+ calcolaANDContains(
												"CATSEARCH(VALO"
														+ iNumCondizione
														+ ".VALORE_STRINGA,'",
												Global.replaceAll(
														protectReserveWord(valore),
														"'", "''"),
												"','id_campo in "
														+ sListaIdCampo
														+ "')>0 ") + " ");
							}
						}
						// CASO NORMALE: VENGO DALLA QUERY NON PARAMETRICA (WEB
						// O JAVA)
						else {
							if ((bIsRicercaPuntualeCampo == false
									&& bIsRicercaPuntualeGlobale == false && valore
									.length() >= 3)) {
								sListaIdCampo = costruzioneListaIdCampo(sqlIdCampo
										.toString());

								if (operatore.equals("<>")) {
									// sWhere.append("  AND "+calcolaANDContains("CONTAINS(VALO"+iNumCondizione+".VALORE_CLOB,'",Global.replaceAll(protectReserveWord(valore),"'","''"),"')=0 ")+" ");
									sWhere.append("  AND "
											+ calcolaANDContains(
													"CATSEARCH(VALO"
															+ iNumCondizione
															+ ".VALORE_STRINGA,'",
													Global.replaceAll(
															protectReserveWord(valore),
															"'", "''"),
													"','id_campo in "
															+ sListaIdCampo
															+ "')=0 ") + " ");
								} else {
									// sWhere.append("  AND "+calcolaANDContains("CONTAINS(VALO"+iNumCondizione+".VALORE_CLOB,'",Global.replaceAll(protectReserveWord(valore),"'","''"),"')>0 ")+" ");
									sWhere.append("  AND "
											+ calcolaANDContains(
													"CATSEARCH(VALO"
															+ iNumCondizione
															+ ".VALORE_STRINGA,'",
													Global.replaceAll(
															protectReserveWord(valore),
															"'", "''"),
													"','id_campo in "
															+ sListaIdCampo
															+ "')>0 ") + " ");
								}
							} else {
								hintOnlyOneField = "";

								if (campoNVL == null)
									sCampo = "VALO" + iNumCondizione
											+ ".VALORE_STRINGA";
								else
									sCampo = "NVL(VALO"
											+ iNumCondizione
											+ ".VALORE_STRINGA,'"
											+ Global.replaceAll(campoNVL, "'",
													"''") + "')";

								if (operatore.equals("<>"))
									sWhere.append("  AND "
											+ sCampo
											+ "<>'"
											+ Global.replaceAll(valore, "'",
													"''") + "' ");
								else if (valore.indexOf("%") != -1)
									sWhere.append("  AND "
											+ sCampo
											+ " like '"
											+ Global.replaceAll(valore, "'",
													"''") + "' ");
								else
									sWhere.append("  AND "
											+ sCampo
											+ "='"
											+ Global.replaceAll(valore, "'",
													"''") + "' ");
							}
						}
					} else if (tipo.equals("N")) {
						if (campoNVL == null)
							sWhere.append("  AND VALO" + iNumCondizione
									+ ".VALORE_NUMERO " + operatore + " "
									+ valore + " ");
						else
							sWhere.append("  AND NVL(VALO" + iNumCondizione
									+ ".VALORE_NUMERO," + campoNVL + ") "
									+ operatore + " " + valore + " ");
					} else {
						if (valore.toUpperCase().indexOf("SYSDATE") == -1)
							valore = "TO_DATE('" + valore + "','"
									+ sFormatoData + "')";

						if (campoNVL != null) {
							if (campoNVL.toUpperCase().indexOf("SYSDATE") != -1)
								sCampo = "NVL(VALO" + iNumCondizione
										+ ".VALORE_DATA," + campoNVL + ") ";
							else
								sCampo = "NVL(VALO" + iNumCondizione
										+ ".VALORE_DATA,TO_DATE('" + campoNVL
										+ "','" + sFormatoData + "')) ";
						} else
							sCampo = "VALO" + iNumCondizione + ".VALORE_DATA";

						sWhere.append("  AND " + sCampo + " " + operatore + " "
								+ valore);
					}
				}
			} else {
				sWhere.append("  AND CONTAINS(VALO"
						+ iNumCondizione
						+ ".VALORE_CLOB,'"
						+ Global.replaceAll(
								protectReserveWord(condizioneFullText), "'",
								"''") + "')>0 ");
			}
		}

		iNumCondizione++;

		return new DoubleString(sFrom.toString(), sWhere.toString(), tipo,
				formatoData);
	}

	private DoubleString getSQLCampo_CondizioneJoin(String campo,
			String listaTipiDoc, String indiceAliasDocumento, String area,
			String cm, String categoria) throws Exception {
		StringBuffer sFrom = new StringBuffer("");
		StringBuffer sWhere = new StringBuffer("");
		String sTipo;

		String formatoData;

		formatoData = (lookupTipoValore(campo, area, cm)).getFormatoData();

		if (formatoData == null || formatoData.equals(""))
			formatoData = sFormatoData;

		formatoData = Global.replaceAll(formatoData, "hh:", "hh24:");

		sTipo = (lookupTipoValore(campo, area, cm)).getTipo();

		String joinLista = "IN " + listaTipiDoc + " ";

		if (categoria != null) {
			joinLista = " = MOD" + indiceAliasDocumento + ".ID_TIPODOC ";
		}

		sFrom.append("  ,CAMPI_DOCUMENTO CADO" + iNumCondizione);
		sFrom.append("  ,VALORI VALO" + iNumCondizione);

		sWhere.append("  AND CADO" + iNumCondizione + ".ID_TIPODOC "
				+ joinLista);
		sWhere.append("  AND DOCU" + indiceAliasDocumento + ".ID_TIPODOC "
				+ joinLista);
		sWhere.append("  AND CADO" + iNumCondizione + ".NOME = '" + campo
				+ "' ");
		sWhere.append("  AND VALO" + iNumCondizione + ".ID_CAMPO+0 = CADO"
				+ iNumCondizione + ".ID_CAMPO ");
		sWhere.append("  AND VALO" + iNumCondizione + ".ID_DOCUMENTO = DOCU"
				+ indiceAliasDocumento + ".ID_DOCUMENTO ");

		iNumCondizione++;

		return new DoubleString(sFrom.toString(), sWhere.toString(), sTipo,
				formatoData);
	}

	/**
	 * Dato un valore restituisce il tipo S=Stringa D=Data N=Numerico
	 *
	 * @return
	 */
	private FieldInformation lookupTipoValore(String campo, String ar, String cm)
			throws Exception {
		String areaDaPassare;
		Vector cmDaPassare = new Vector();

		if (ar == null) {
			areaDaPassare = area;
			if (cm != null)
				cmDaPassare.add(cm);
		}
		// Se non gestisco area e cm per il campo prendo, quelli passati con
		// la addCodicemodello e la settaArea della IQuery
		else {
			areaDaPassare = ar;
			cmDaPassare = vCmArea;
		}

		return (new LookUpDMTable(vu)).retrieveTipo(campo, cmDaPassare,
				areaDaPassare);

	}

	/**
	 * Dato un valore restituisce il tipo S=Stringa D=Data N=Numerico
	 * 
	 * @param valore
	 * @return
	 */
	private String lookupTipoOperatore(String valore) {
		return (new LookUpDMTable(vu)).lookupTipoOperatore(valore);
	}

	private String protectReserveWord(String phrase) {
		for (int i = 0; i < reserveWord.length; i++) {
			phrase = vu.Global.replaceAll(phrase, reserveWord[i],
					escapeCaracter + reserveWord[i]);
		}

		return phrase;
	}

	private String calcolaANDContains(String sContains, String sFrase,
			String sConfronto) {
		String condizioneAndSistemata = "";

		java.util.StringTokenizer s = new java.util.StringTokenizer(sFrase, " ");

		while (s.hasMoreTokens()) {
			condizioneAndSistemata += sContains + s.nextElement() + sConfronto;
			if (s.hasMoreTokens())
				condizioneAndSistemata += " AND ";
		}

		return condizioneAndSistemata;
	}

	private String calcolaCondizoneFullText() {
		StringBuffer sCondizioneFullText = new StringBuffer("");
		String condizioneOrSistemata = "", condizioneAndSistemata = "";

		if (condAnd != null && (!condAnd.equals(""))) {
			java.util.StringTokenizer s = new java.util.StringTokenizer(
					protectReserveWord(condAnd), " ");
			condizioneAndSistemata = "(";
			while (s.hasMoreTokens()) {
				condizioneAndSistemata += s.nextElement();
				if (s.hasMoreTokens())
					condizioneAndSistemata += " AND ";
			}

			sCondizioneFullText.append(condizioneAndSistemata + ")");
		}

		/**** CALCOLO DELL'OR *****/

		if (condOr != null) {

			condOr = condOr.trim();

			if (!condOr.equals("")) {
				java.util.StringTokenizer s = new java.util.StringTokenizer(
						protectReserveWord(condOr), " ");

				while (s.hasMoreTokens()) {
					condizioneOrSistemata += s.nextElement();
					if (s.hasMoreTokens())
						condizioneOrSistemata += " OR ";
				}

				if (!sCondizioneFullText.toString().equals(""))
					sCondizioneFullText.append(" AND ");

				sCondizioneFullText.append("(" + condizioneOrSistemata + ")");
			}
		}
		/**** FINE CALCOLO OR *****/

		// AGGIUNGERE LA CONDIZIONE NOT??

		// AGGIUNGERE LA CONDIZONE SINGLE???

		return sCondizioneFullText.toString();
	}

	/**
	 * Metodo che sostituisce dal vettore degli ordinamenti il campo passato in
	 * input con la frase da mettere nell'ordinamento
	 * 
	 * @param nomeCampo
	 *            Campo da eliminare
	 * @return Campo completo (nomecampo@ASC/DESC) se rimosso null Altrimenti
	 */
	private Vector removeCampoOrdinamentoFromCampo(String nomeCampo,
			String area, String cm, String categoria) {
		String sRetCampoCompleto = null;

		Vector vRet = new Vector();

		for (int i = 0; i < campiOrdinamento.size(); i++)
			if (nomeCampo.equals(estraiCampoDaCampoOrdinamento(i))) {
				String areaCampo, cmCampo, categoriaCampo;

				if (campiOrdinamento.get(i) instanceof keyval) {
					areaCampo = ((keyval) campiOrdinamento.get(i)).getArea();
					cmCampo = ((keyval) campiOrdinamento.get(i)).getCm();
					categoriaCampo = ((keyval) campiOrdinamento.get(i))
							.getCategoria();
				} else {
					areaCampo = null;
					cmCampo = null;
					categoriaCampo = null;
				}

				if ((area == null && cm == null && categoria == null
						&& areaCampo == null && cmCampo == null && categoriaCampo == null)
						|| (area != null && cm != null && areaCampo != null
								&& cmCampo != null && area.equals(areaCampo) && cmCampo
								.equals(cmCampo))
						|| (categoria != null && categoriaCampo != null && categoria
								.equals(categoriaCampo))) {
					if (campiOrdinamento.get(i) instanceof keyval) {
						sRetCampoCompleto = ((keyval) campiOrdinamento.get(i))
								.getKey();
						vRet.add(sRetCampoCompleto);
						vRet.add(((keyval) campiOrdinamento.get(i))
								.getCampoReturn());
					} else {
						sRetCampoCompleto = "" + campiOrdinamento.get(i);
						vRet.add(sRetCampoCompleto);
						vRet.add(keyval.ISCAMPO_ORDINAMENTO);
					}

					vRet.add("" + i);

				}
			}

		return vRet;
	}

	private String estraiAreaDaCampoOrdinamento(int index) {
		String stringaCompleta;

		if (campiOrdinamento.get(index) instanceof keyval)
			stringaCompleta = ((keyval) campiOrdinamento.get(index)).getArea();
		else
			stringaCompleta = null;

		return stringaCompleta;
	}

	private String estraiCmDaCampoOrdinamento(int index) {
		String stringaCompleta;

		if (campiOrdinamento.get(index) instanceof keyval)
			stringaCompleta = ((keyval) campiOrdinamento.get(index)).getCm();
		else
			stringaCompleta = null;

		return stringaCompleta;
	}

	private String estraiTipoOrdOrReturn(int index) {
		String stringaCompleta;

		if (campiOrdinamento.get(index) instanceof keyval)
			stringaCompleta = ((keyval) campiOrdinamento.get(index))
					.getCampoReturn();
		else
			stringaCompleta = keyval.ISCAMPO_ORDINAMENTO;

		return stringaCompleta;
	}

	private String estraiCategoriaDaCampoOrdinamento(int index) {
		String stringaCompleta;

		if (campiOrdinamento.get(index) instanceof keyval)
			stringaCompleta = ((keyval) campiOrdinamento.get(index))
					.getCategoria();
		else
			stringaCompleta = null;

		return stringaCompleta;
	}

	private String estraiFormatoDaCampoOrdinamento(int index) {
		if (campiOrdinamento.get(index) instanceof keyval)
			return ((keyval) campiOrdinamento.get(index)).getFormatoCampo();
		else
			return null;
	}

	private String estraiCampoDaCampoOrdinamento(int index) {
		String stringaCompleta;

		if (campiOrdinamento.get(index) instanceof keyval)
			stringaCompleta = ((keyval) campiOrdinamento.get(index)).getKey();
		else
			stringaCompleta = "" + campiOrdinamento.get(index);

		return stringaCompleta.substring(0, stringaCompleta.indexOf("@"));
	}

	private String estraiCampoOrdinamento(int index) {
		String stringaCompleta;

		if (campiOrdinamento.get(index) instanceof keyval)
			stringaCompleta = ((keyval) campiOrdinamento.get(index)).getKey();
		else
			stringaCompleta = "" + campiOrdinamento.get(index);

		return stringaCompleta;
	}

	private String estraiAscDescDaCampoOrdinamento(int index) {
		String stringaCompleta;

		if (campiOrdinamento.get(index) instanceof keyval)
			stringaCompleta = ((keyval) campiOrdinamento.get(index)).getKey();
		else
			stringaCompleta = "" + campiOrdinamento.get(index);

		return stringaCompleta.substring(stringaCompleta.indexOf("@") + 1,
				stringaCompleta.length());
	}

	private String estraiCampoDaCampoOrdinamento(String stringaCompleta) {
		return stringaCompleta.substring(0, stringaCompleta.indexOf("@"));
	}

	private String estraiAscDescDaCampoOrdinamento(String stringaCompleta) {
		return stringaCompleta.substring(stringaCompleta.indexOf("@") + 1,
				stringaCompleta.length());
	}

	private String getIndexDoc(String sTipoDoc) {
		String indexDoc;

		// Questo serve per dare un numero ai doc
		// della from (per doc1,doc2,...,docn)
		if (!hMapCmCampo.containsKey(sTipoDoc)) {
			hMapCmCampo.put(sTipoDoc, "" + (hMapCmCampo.size() + 1));
			indexDoc = "" + (hMapCmCampo.size());
			try {
				// se è un numero è un tipodoc
				Long.parseLong(sTipoDoc);
				// controllo dell'indexAlias di ritorno sulla select
				if (IdTipoDocIdRicercaReturn != null
						&& IdTipoDocIdRicercaReturn.equals(sTipoDoc))
					indexaliasIdReturn = indexDoc;

			} catch (Exception e) {
				// Altrimenti è il nome di una categoria
				// lo inseriesco nella hash map delle categorie
				hMapCategorie.put("" + hMapCmCampo.size(), sTipoDoc);

				// controllo dell'indexAlias di ritorno sulla select
				if (categoriaIdRicercaReturn != null
						&& categoriaIdRicercaReturn.equals(sTipoDoc))
					indexaliasIdReturn = indexDoc;
			}
		} else
			indexDoc = "" + hMapCmCampo.get(sTipoDoc);

		return indexDoc;
	}

	private String getNomeCampo(String tipo) {
		if (tipo.equals("S"))
			return "VALORE_STRINGA";
		if (tipo.equals("N"))
			return "VALORE_NUMERO";

		return "VALORE_DATA";
	}

	private String convertOrderExpression(String sInput, String aliasDoc,
			String indexDoc) {
		Pattern pattern = Pattern.compile("\\{\\w+\\}");

		String sStringa = sInput;

		Matcher matcher = pattern.matcher(sInput);

		while (matcher.find()) {
			String sReg = matcher.group();
			sStringa = Global.replaceAll(
					sStringa,
					sReg,
					"f_valore_campo("
							+ aliasDoc
							+ indexDoc
							+ ".id_documento,'"
							+ Global.replaceAll(
									Global.replaceAll(sReg, "{", ""), "}", "")
							+ "')");
		}

		return sStringa;
	}

	private String generaListaId() {
		String sRet = "(";

		for (int i = 0; i < idDoc.size(); i++) {
			sRet += "" + idDoc.get(i);

			if (i != idDoc.size() - 1)
				sRet += ",";
		}

		sRet += ")";

		return sRet;
	}

	private String getSequenzaBlocchi(Vector v, String indexAliasReturn) {
		String seq = "";
		int count = 0, max_list = 1000, v_size, s;

		if (v.size() == 0)
			return seq;

		v_size = v.size();
		s = 0;

		seq = " (  DOCU" + indexAliasReturn + ".id_documento IN ( ";

		for (int i = 0; i < v.size(); i++) {
			if (count == max_list) {
				count = 0;
				s++;
				v_size = v.size() - s * max_list;
				seq += " or DOCU" + indexAliasReturn + ".id_documento IN ( ";
			}

			seq += v.get(i);

			if (v.size() <= max_list)
				seq += (i != v.size() - 1) ? (" , ") : (")");
			else {
				if (count == v_size - 1)
					seq += " ) ";
				else if (count == (max_list - 1))
					seq += " ) ";
				else
					seq += " , ";
			}

			count++;
		}

		seq += " ) ";
		return seq;
	}

	private IDbOperationSQL connect() throws Exception {
		if (vu.getDbOp() == null) {
			bIsNew = true;
			return (new ManageConnection(vu.Global)).connectToDB();
		}

		return vu.getDbOp();
	}

	private void disconnect() throws Exception {
		if (bIsNew)
			(new ManageConnection(vu.Global)).disconnectFromDB(dbOp, true,
					false);
	}

}

class DoubleString {
	public String sFrom, sWhere;
	// Tipo campo lo uso per sapere getSQLCampo_Condizione ad
	// ogni ciclo tratta un campo di tipo numero, stringa o data
	// Mi serve per decidere cosa mettere sulla select dei campi
	// di ordinamento
	public String tipoCampo;
	public String sFormatoData;

	public DoubleString(String from, String where, String sTipoCampo,
			String formatDate) {
		sFrom = from;
		sWhere = where;
		tipoCampo = sTipoCampo;
		sFormatoData = formatDate;
	}
}
