package it.finmatica.dmServer.check;

import it.finmatica.dmServer.util.JNDIParameter;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;


public class CheckDocumento {
	private int livello = 0;
	private String utente = "";
	private String nominativo = "";
	private String iddocumento = "";
	private String id_tipodoc = "";
	private String errorMessage = "";
	private Connection cn = null;
	private JNDIParameter jndi=null;
	private IDbOperationSQL dbOpEsterna=null;

	public CheckDocumento(Connection cn) throws Exception {
		this.cn = cn;
		livello = 0;
		utente = "";
		nominativo = "";
		iddocumento = "";
		id_tipodoc = "";
	}
	public CheckDocumento(String iddoc, Connection cn) throws Exception {
		iddocumento = iddoc;
		this.cn = cn;
		leggiCheck();
	}
		
	public CheckDocumento(JNDIParameter jndi) throws Exception {
		this.jndi = jndi;
		livello = 0;
		utente = "";
		nominativo = "";
		iddocumento = "";
		id_tipodoc = "";
	}
	public CheckDocumento(String iddoc, JNDIParameter jndi) throws Exception {
		this(iddoc,jndi,true);
	}
	public CheckDocumento(String iddoc, IDbOperationSQL dbOpEsterna) throws Exception {
		iddocumento = iddoc;
		this.dbOpEsterna = dbOpEsterna;
		leggiCheck();
	}

	public CheckDocumento(String iddoc, JNDIParameter jndi, boolean leggiCheck) throws Exception {
		iddocumento = iddoc;
		this.jndi = jndi;
		if (leggiCheck) leggiCheck();
	}
	
	public void settaDocumento (String iddoc) throws Exception {
		iddocumento = iddoc;
		leggiCheck();
	}

	public void leggiCheck() throws Exception {
		leggiCheck(null);
	}
	
	public void leggiCheck(IDbOperationSQL dbOpLocale) throws Exception {
		IDbOperationSQL dbOp = null;
		String 					query = "";
		ResultSet				rst = null;
		livello = 0;
		utente = "";
		nominativo = "";
		errorMessage = "";
		try {
			if (dbOpLocale==null)
				dbOp = getDbOp();//SessioneDb.getInstance().createIDbOperationSQL(cn);
			else
				dbOp=dbOpLocale;
			query = "SELECT ID_TIPODOC FROM DOCUMENTI WHERE ID_DOCUMENTO = :ID_DOC";
			dbOp.setStatement(query);
			dbOp.setParameter(":ID_DOC", iddocumento);
			dbOp.execute();
			rst = dbOp.getRstSet();
			if (rst.next()) {
				id_tipodoc = rst.getString(1);
			}
			
			query = "SELECT C.LIVELLO_CHECKIN, C.UTENTE_CHECKIN, U.NOMINATIVO "+
							"FROM CHECK_DOCUMENTI C, AD4_UTENTI U "+
							"WHERE C.ID_DOCUMENTO = :ID_DOC "+
							"AND U.UTENTE(+) = C.UTENTE_CHECKIN";

			dbOp.setStatement(query);
			dbOp.setParameter(":ID_DOC", iddocumento);
			dbOp.execute();
			rst = dbOp.getRstSet();
			if (rst.next()) {
				livello 		= rst.getInt(1);
				utente 			= rst.getString(2);
				nominativo 	= rst.getString(3);
			}
		} catch (Exception e) {
			livello = -1;
			throw new Exception ("Errore in fase ricerca blocco. \n"+ e.getMessage());
		} finally {
			if (dbOpLocale==null) close(dbOp);
		}
	}
	
	public int verificaCheck(String user) {
		errorMessage = "";
		if (livello == 0) {
			return livello;
		} else {
			if (user.equals(utente)) {
				return 0;
			} else {
				if (livello == 1) {
					errorMessage = "Il documento è bloccato in maniera esclusiva dall'utente "+nominativo;
				}
				if (livello == 2) {
					errorMessage = "Attenzione. Impossibile modificare il documento in quanto bloccato dall'utente "+nominativo;
				}
				if (livello == 3) {
					errorMessage = "Attenzione. Il documento è bloccato dall'utente "+nominativo+". Ogni modifica effettuata potrebbe andare persa.";
				}
				return livello;
			}
		}
	}

	public boolean verificaCompetenze(String utente_checkin, int livello_checkin) throws Exception {
		return verificaCompetenze(utente_checkin,livello_checkin,null);
	}
	
	public boolean verificaCompetenze(String utente_checkin, int livello_checkin, IDbOperationSQL dbOpLocale) throws Exception {
		IDbOperationSQL dbOp = null;
		String 					query = "";
		ResultSet				rst = null;
		boolean					retval = false;
		String					be,bl,ba;

		try {
			if (dbOpLocale==null)
  				dbOp = getDbOp();//SessioneDb.getInstance().createIDbOperationSQL(cn);
			else
				dbOp = dbOpLocale;
  			//verifica competenze
  			query = "SELECT GDM_COMPETENZA.GDM_VERIFICA('TIPI_DOCUMENTO', '"+id_tipodoc+"', 'be', '"+utente_checkin+"', f_trasla_ruolo('"+utente_checkin+"','GDMWEB','GDMWEB')) BE, "+
  						"GDM_COMPETENZA.GDM_VERIFICA('TIPI_DOCUMENTO', '"+id_tipodoc+"', 'bl', '"+utente_checkin+"', f_trasla_ruolo('"+utente_checkin+"','GDMWEB','GDMWEB')) BL, "+
  						"GDM_COMPETENZA.GDM_VERIFICA('TIPI_DOCUMENTO', '"+id_tipodoc+"', 'ba', '"+utente_checkin+"', f_trasla_ruolo('"+utente_checkin+"','GDMWEB','GDMWEB')) BA "+
  						" FROM DUAL";
			dbOp.setStatement(query);
//			dbOp.setParameter(":ID_TIPODOC", id_tipodoc);
//			dbOp.setParameter(":UTENTE", utente_checkin);
			dbOp.execute();
			rst = dbOp.getRstSet();
			if (rst.next()) {
				be = rst.getString(1);
				bl = rst.getString(2);
				ba = rst.getString(3);
				if (be.equalsIgnoreCase("1")) {
					retval = true;
				} else {
					if (livello_checkin > 1) {
						if (bl.equalsIgnoreCase("1")) {
							retval = true;
						} else {
							if (livello_checkin == 3 && ba.equalsIgnoreCase("1")) {
								retval = true;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new Exception ("Errore in fase di verifica competenze. \n"+ e.getMessage());
		} finally {
			if (dbOpLocale==null) close(dbOp);
		}
		return retval;

	}

	public int checkOut(String utente_checkin, int livello_checkin) throws Exception {
		return checkOut(utente_checkin,livello_checkin,null);
	}
	
	public int checkOut(String utente_checkin, int livello_checkin, IDbOperationSQL dbOpLocale) throws Exception {
		IDbOperationSQL dbOp = null;
		String 					query = "";
		if (iddocumento.length() == 0) {
			errorMessage = "Documento non selezionato.";
			return -1;
		}
		if (livello !=0) {
			return -1;
		}
		
		//verifica competenze
		if (!verificaCompetenze(utente_checkin, livello_checkin)) {
			errorMessage = "Impossibile bloccare il documento. Non si hanno le competenze di blocco.";
			return -1;
		}
		
		try {
			if (dbOpLocale==null)
				dbOp = getDbOp();//SessioneDb.getInstance().createIDbOperationSQL(cn);
			else
				dbOp = dbOpLocale;
			
			query = "INSERT INTO CHECK_DOCUMENTI "+
							"(ID_CHECK, ID_DOCUMENTO, UTENTE_CHECKIN, DATA_CHECKIN, LIVELLO_CHECKIN, UTENTE_MODIFICA, DATA_MODIFICA) "+
							"VALUES "+
							"(CHDO_SQ.NEXTVAL, :ID_DOC, '"+utente_checkin+"', sysdate, "+livello_checkin+", '"+utente_checkin+"', sysdate )";

			dbOp.setStatement(query);
			dbOp.setParameter(":ID_DOC", iddocumento);
			dbOp.execute();
//			dbOp.commit();
			leggiCheck(dbOpLocale);
		} catch (Exception e) {
			livello = -1;
			throw new Exception ("Errore in fase di blocco. \n"+ e.getMessage());
		} finally {
			if (dbOpLocale==null) close(dbOp);
		}
		return 0;
	}


	public int checkIn() throws Exception {
		return checkIn(null);
	}

	public int checkIn(IDbOperationSQL dbOpLocale) throws Exception {
		IDbOperationSQL dbOp = null;
		String 					query = "";
		if (iddocumento.length() == 0) {
			errorMessage = "Documento non selezionato.";
			return -1;
		}
		if (livello ==0) {
			errorMessage = "Documento non bloccato.";
			return -1;
		}
		try {
			if (dbOpLocale==null)
				dbOp = getDbOp();//SessioneDb.getInstance().createIDbOperationSQL(cn);
			else
				dbOp = dbOpLocale;//SessioneDb.getInstance().createIDbOperationSQL(cn);
			query = "DELETE CHECK_DOCUMENTI "+
							"WHERE  ID_DOCUMENTO = :ID_DOC";

			dbOp.setStatement(query);
			dbOp.setParameter(":ID_DOC", iddocumento);
			dbOp.execute();
			livello = 0;
			utente = "";
			nominativo = "";
			errorMessage = "";
		} catch (Exception e) {
			livello = -1;
			throw new Exception ("Errore in fase di sblocco. \n"+ e.getMessage());
		} finally {
			if (dbOpLocale==null) close(dbOp);
		}
		return 0;
	}


  public static void main(String[] args) throws Exception {
	 Connection conn=null;
	 Class.forName("oracle.jdbc.driver.OracleDriver");
	 conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.12:1521:ORCL","GDM","GDM");
	 conn.setAutoCommit(false);
	 CheckDocumento cd = new CheckDocumento("10",conn);
	 System.out.println(cd.verificaCheck("GDM"));
	 cd.settaDocumento("10");
	 try {
		 System.out.println(cd.checkOut("GDM", 2));
		 System.out.println(cd.getErrorMessage());
	 } catch (Exception e) {
		 System.out.println(e.getMessage());
	 }
	 conn.close();
  }

  public int getLivello() {
		return livello;
	}
	public String getNominativo() {
		return nominativo;
	}
	public String getUtente() {
		return utente;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	public void commit() throws Exception {
		cn.commit();
	}

	private void close(IDbOperationSQL dbOp) {
		if (dbOpEsterna!=null) return;
		try {
			dbOp.close();
		}
		catch (Exception e) {

		}
	}
	
	private IDbOperationSQL getDbOp() throws Exception {
		if (dbOpEsterna!=null) {
			return dbOpEsterna;
		}
		else {
			if (cn!=null)
				return SessioneDb.getInstance().createIDbOperationSQL(cn);
			else
				return SessioneDb.getInstance().createIDbOperationSQL(jndi.getJndiString(),0);
		}
	}
	 
}
