package it.finmatica.modulistica.domini;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.connessioni.Connessione;
import it.finmatica.modulistica.parametri.Parametri;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class SoaDominio {
	private 		Dominio     d = null;
	private			String			area;
	private			String			cm;
	private			String			dominio;
	private static Logger logger = Logger.getLogger(SoaDominio.class);

	public SoaDominio(String pArea, String pCm, String pDominio) {
		area = pArea;
		cm = pCm;
		dominio = pDominio;
	}
	
	private void caricaDominio(String pParametri) throws Exception {
		Connection      pConn;
		IDbOperationSQL dbOp = null;
		ResultSet   resultQuery = null;
		String      query, 
                tipo, 
                istruzione = null,
                driver = null,
                ordinamento = null,
                connessione, 
                user, 
                passwd,
                sDsn = "";

		query = "SELECT * FROM DOMINI " +
            "WHERE AREA = :AREA AND "+
            "      DOMINIO = :DOMINIO"+
            "  AND CODICE_MODELLO = :CM ";
	            
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL("jdbc/gdm",0);
      pConn = dbOp.getConn();
      Parametri.leggiParametriConnection(pConn);
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", area);
      dbOp.setParameter(":CM", cm);
      dbOp.setParameter(":DOMINIO", dominio);
      dbOp.execute();
      resultQuery = dbOp.getRstSet();
      
      if (resultQuery.next()) {
        tipo = resultQuery.getString("TIPO");
        ordinamento = resultQuery.getString("ORDINAMENTO");

        if (tipo.equals("O") || tipo.equals("F") ||  tipo.equals("J")) {
          // Ricavo l'istruzione (il campo è un CLOB).
          Clob clob = resultQuery.getClob("ISTRUZIONE");
          long clobLen = clob.length();
          if (clobLen < Dominio.MAXLEN_ISTRUZIONE) {
            int i_clobLen = (int)clobLen;
            istruzione = clob.getSubString(1, i_clobLen);
          } else {
            logger.error("ListaDominiDMS::caricaDominio() - Attenzione! Si è verificato un errore. L'istruzione per il caricamento del dominio supera i "+Dominio.MAXLEN_ISTRUZIONE+" caratteri.");
          }

          if (!tipo.equals("J")) {
            // Determino il driver da utilizzare
            driver = resultQuery.getString("DRIVER");
            if ((driver == null) || (driver.trim().length() == 0)) {
              driver = "";  // Bridge STANDARD JDBC-ODBC
            }
          }
        }

        // Leggo i parametri di connessione
        connessione = resultQuery.getString("CONNESSIONE");
        if (connessione == null) {
          connessione = "";
        } 
        user = resultQuery.getString("UTENTE");
        passwd = resultQuery.getString("PASSWD");
        
        sDsn = resultQuery.getString("DSN");
        if (sDsn == null) {
          sDsn = "";
        }
        if (!sDsn.equalsIgnoreCase("")) {
          Connessione cn = new Connessione(dbOp,sDsn);
          driver      = cn.getDriver();
          connessione = cn.getConnessione();
          user        = cn.getUtente();
          passwd      = cn.getPassword();
        }

        SoaDominioParser pAbPar = new SoaDominioParser(pParametri);
        if (tipo.equals("S"))
          d = new DominioStandard(pConn, area, dominio, tipo, ordinamento, pAbPar);
        else if (tipo.equals("J")) 
          d = new DominioJava(pConn, area, dominio, tipo, ordinamento, istruzione, pAbPar);
        else if (tipo.equals("N")) 
          d = new DominioNotes(pConn, area, dominio, tipo, ordinamento, connessione, pAbPar);
        else if (tipo.equals("O"))
          d = new DominioCONN(pConn, area, dominio, tipo, ordinamento, driver, connessione, user, passwd, istruzione, pAbPar);
        else if (tipo.equals("F"))
          d = new DominioFunction(pConn, area, dominio, tipo, ordinamento, driver, connessione, user, passwd, istruzione, pAbPar);
        else
          d = null;  // TIPO DOMINIO NON RICONOSCIUTO
      } else {
        // Il dominio cercato NON è presente nel database
        d = null;
      }
      
    } catch(Exception e) {
      loggerError("ListaDominiDMS::caricaDominio() - Attenzione: il dominio "+dominio+" presenta delle anomalie. "+e.toString(),e);
      d = null;
      free(dbOp);
      throw new Exception(e);
    } 

    free(dbOp);
	}

	/**
  *
  */
	private void free(IDbOperationSQL dbOp) {
		try {
			dbOp.close();
		} catch (Exception e) { }
	}

  /**
   * 
   */
  private void loggerError(String sMsg, Exception e) {
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        logger.error(sMsg);
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        logger.error(sMsg,e);
      }
  }
  
  public String valorizza(String pParametri) {
  	String retval = "", 
  				numero_valori = "";
  	Document dDoc;
    Element root, elp;

    try {
    	caricaDominio(pParametri);
    	if (d == null) {
    		throw new Exception("Impossibile caricare il dominio: "+dominio+" - area: "+area+" - cm: "+cm);
    	}
    	numero_valori = ""+d.getNumeroValori();
      root = DocumentHelper.createElement("ROWSET");
      root.addAttribute("requestId", "prova");
      root.addAttribute("pages", "1");
      root.addAttribute("page", "1");
      root.addAttribute("lastRow", numero_valori);
      root.addAttribute("rows", numero_valori);
      root.addAttribute("firstRow", "1");
      dDoc = DocumentHelper.createDocument();
      dDoc.setRootElement(root);
      
      int j = 0;
      for (int i=0; i< d.getNumeroValori(); i++) {
      	elp = DocumentHelper.createElement("ROW");
      	j = i+1;
      	elp.addAttribute("num", ""+j);
      	elp = aggFiglio(elp,"C",d.getCodice(i));
      	elp = aggFiglio(elp,"V",d.getValore(i));
      	root.add(elp);
      }
      retval = dDoc.asXML();
    } catch (Exception e) {
    	try {
    		root = DocumentHelper.createElement("message");
    		dDoc = DocumentHelper.createDocument();
        dDoc.setRootElement(root);
      	elp = DocumentHelper.createElement("result");
      	elp.setText("error");
      	root.add(elp);
      	elp = DocumentHelper.createElement("type");
      	elp.setText(e.getClass().getName());
      	root.add(elp);
      	elp = DocumentHelper.createElement("code");
      	root.add(elp);
      	elp = DocumentHelper.createElement("text");
      	elp.setText(e.getMessage());
      	root.add(elp);
        retval = dDoc.asXML();
    	} catch (Exception e2) {}
    }

  	return retval;
  }

  public String decodifica(String pParametri, String codice) {
  	String retval = "";
		Document dDoc;
		Element root, elp;
	  try {
	  	caricaDominio(pParametri);
	  	retval = d.getValore(codice);
	  } catch (Exception e) {
	  	try {
	  		root = DocumentHelper.createElement("message");
	  		dDoc = DocumentHelper.createDocument();
	      dDoc.setRootElement(root);
	    	elp = DocumentHelper.createElement("result");
	    	elp.setText("error");
	    	root.add(elp);
	    	elp = DocumentHelper.createElement("type");
	    	elp.setText(e.toString());
	    	root.add(elp);
	    	elp = DocumentHelper.createElement("code");
	    	root.add(elp);
	    	elp = DocumentHelper.createElement("text");
	    	elp.setText(e.getMessage());
	    	root.add(elp);
	      retval = dDoc.asXML();
	      
	  	} catch (Exception e2) {}
	  }
	  return retval;
  }
  
  private Element aggFiglio(Element elp, String nome, String valore) {
    Element elf = DocumentHelper.createElement(nome);
    elf.setText(valore);
    elp.add(elf);
    return elp;
  }

}
