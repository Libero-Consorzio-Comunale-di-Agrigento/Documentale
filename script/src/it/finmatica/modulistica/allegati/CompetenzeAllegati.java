package it.finmatica.modulistica.allegati;

import java.sql.ResultSet;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.competenze.Abilitazioni;
import it.finmatica.dmServer.competenze.GDM_Competenze;
import it.finmatica.dmServer.competenze.UtenteAbilitazione;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;

public class CompetenzeAllegati {
  private Environment vu;
  private UtenteAbilitazione ua = null;
  private String  id_tipodoc = null;
  private String idDoc = null;
  private String 	competenzeAllegati 	= "N";
  private long	lettura = 0;
  private long	modifica = 0;
  private long 	cancellazione = 0;

	public CompetenzeAllegati(Environment vu, String id_tipodoc, String idDoc) throws Exception {
		this.vu = vu;
		this.id_tipodoc = id_tipodoc;
		this.idDoc = idDoc;

		init();
	}
	
	private void init() throws Exception {
		String query;
		IDbOperationSQL dbOp = null;
		ResultSet rst = null;
		Abilitazioni ab = null;
		String tipo, oggetto;
		
    query = "SELECT COMPETENZE_ALLEGATI"+
						" FROM TIPI_DOCUMENTO T"+
						" WHERE ID_TIPODOC = :ID_TIPODOC";
		try {
			if (vu.getDbOp()!=null) {
				dbOp=vu.getDbOp();
			}
			else {
				dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
			}

			dbOp.setStatement(query);
			dbOp.setParameter(":ID_TIPODOC",id_tipodoc);
			dbOp.execute();
			rst = dbOp.getRstSet();

			if (rst.next() ) {
				competenzeAllegati = rst.getString("COMPETENZE_ALLEGATI");
			}
			if (competenzeAllegati != null && competenzeAllegati.equalsIgnoreCase("S")) {
				if (idDoc == null) {
					tipo = "TIPI_DOCUMENTO";
					oggetto = id_tipodoc;
				} else {
					tipo = "DOCUMENTI";
					oggetto = idDoc;
				}
				ua = new UtenteAbilitazione(vu.getUser(), vu.getGruppo(), "GDM", vu.getPwd(),  vu.getUser());
				ab = new Abilitazioni(tipo, oggetto, "LA");
				lettura = ((new GDM_Competenze(vu)).verifica_GDM_Compentenza(ua,ab));
				ab = new Abilitazioni(tipo, oggetto, "UA");
				modifica = ((new GDM_Competenze(vu)).verifica_GDM_Compentenza(ua,ab));
				ab = new Abilitazioni(tipo, oggetto, "DA");
				cancellazione = ((new GDM_Competenze(vu)).verifica_GDM_Compentenza(ua,ab));
			} else {
				competenzeAllegati = "N";
				lettura = 1;
			  modifica = 1;
			  cancellazione = 1;
			}

		} catch (Exception e) {
			throw e;
		}
		finally {
			if (vu.getDbOp()==null) free(dbOp);
		}
	}
	
	
  /**
	 * @return the competenzeAllegati
	 */
	public String getCompetenzeAllegati() {
		return competenzeAllegati;
	}

	/**
	 * @return the lettura
	 */
	public long getLettura() {
		return lettura;
	}

	/**
	 * @return the modifica
	 */
	public long getModifica() {
		return modifica;
	}

	/**
	 * @return the cancellazione
	 */
	public long getCancellazione() {
		return cancellazione;
	}

	private void free(IDbOperationSQL dbOp) {
    try {
      dbOp.close();
    } catch (Exception e) { }
  }

}
