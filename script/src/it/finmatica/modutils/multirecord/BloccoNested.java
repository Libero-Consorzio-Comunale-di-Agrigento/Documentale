package it.finmatica.modutils.multirecord;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.sql.*;
import java.io.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modulistica.personalizzazioni.Personalizzazioni;
import it.finmatica.modutils.multirecord.Multirecord;
import it.finmatica.modutils.informazioniblocco.InformazioniBlocco;
import org.apache.log4j.Logger;
import org.dom4j.Element;

public class BloccoNested  {
  protected String parArea = "";
  protected String parCm = "";
  protected String parBlocco = "";
  protected String blocco = "";
  protected String nomeBlocco = "";
  protected String area = "";
  protected String codMod = "";
  protected String inifile = "";
  protected String tipo = "";
  protected String rw = "";  
  protected String utente = "";  
  protected Multirecord mr = null;
  protected LinkedList listaDati = null;
  protected int riga = 0;
  protected HttpServletRequest sRequest;
  private DatiBlocchiNested dbn = null;
  private Connection pConn;
  private  static Logger logger = Logger.getLogger(BloccoNested.class);
  private boolean debuglog = logger.isDebugEnabled();

  public BloccoNested(HttpServletRequest pRequest, String pArea, String pModello, String pBlocco,LinkedList pListaDati, int pRiga, Connection p_Conn, boolean noCompetenze) throws Exception {
    //Debug Tempo
    long ptime = stampaTempo("BloccoNested - Inizio",pArea,pModello,pBlocco,0);
    //Debug Tempo
    BufferedInputStream bis;
    parArea   = pArea;
    parCm     = pModello;
    parBlocco = pBlocco;
    area = pArea;  
    pConn = p_Conn;
    boolean         protetto = false;
//    boolean         ajax = false;
    listaDati = pListaDati;
    riga = pRiga;
    IDbOperationSQL  dbOp2   = null;
    utente  = (String)pRequest.getSession().getAttribute("UtenteGDM");
    String          pdo     = (String)pRequest.getSession().getAttribute("pdo");
    if (pdo == null) {
      pdo = "";
    }
    if (utente == null || utente.equalsIgnoreCase("")) {
      utente  = (String)pRequest.getSession().getAttribute("Utente");
    }
    ResultSet       rst     = null;
    String          sDsn    = "";
    String          corpo   = "",
                    blkDriver = "",
                    blkConn = "",
                    blkUte  = "",
                    blkPswd = "",
                    istruz  = "",
                    tab     = "",
                    leg     = "",
                    ord     = "",
                    agg     = "",
                    sXML    = "";
    int             nav     = 1;

    sRequest = pRequest;
    InformazioniBlocco infoBlk = new InformazioniBlocco(pBlocco);
    nomeBlocco = infoBlk.getBlocco();
    String areaBlocco = infoBlk.getAreaBlocco();
    if ((areaBlocco == null) || (areaBlocco.equalsIgnoreCase(""))){
      areaBlocco = pArea;
    } else {
      area = areaBlocco;
    }

    //Controllo se c'� una personalizzazione
    Personalizzazioni pers = null;
    String ente = (String)pRequest.getSession().getAttribute("Ente");
    if (ente == null) {
      ente = "";
    }
    String us = (String)pRequest.getSession().getAttribute("Utente");
    if (us == null) {
      us = "";
    }
    pers = (Personalizzazioni)pRequest.getSession().getAttribute("_personalizzazioni_gdm");
    if (pers == null) {
      try {
        pers = new Personalizzazioni(ente, us);
      } catch (Exception e) {
        
      }
      pRequest.getSession().setAttribute("_personalizzazioni_gdm",pers);
    }

    if (pers != null) {
      String persBlocco = pers.getPersonalizzazione(Personalizzazioni.BLOCCHI, areaBlocco+"#"+nomeBlocco);
      int j = persBlocco.indexOf("#");
      areaBlocco = persBlocco.substring(0,j);
      nomeBlocco = persBlocco.substring(j+1);
    }

    leg       = infoBlk.getLegame();
    ord       = infoBlk.getOrdinamento();
    nav       = Integer.parseInt(infoBlk.getNumeroRecord());
    agg       = "N";

    blocco = pBlocco;
    String nomeServ = (String)pRequest.getSession().getAttribute("p_nomeservlet");
    if (nomeServ == null) {
      nomeServ = "";
    } else {
      if (!pdo.equalsIgnoreCase("")) {
        nomeServ += ".do";
      }
    }

    rw = pRequest.getParameter("rw");
    if (rw == null) {
      rw = "R";
    }
//    if (rw.equalsIgnoreCase("W")) {
//      ajax = true;
//    } 
    IDbOperationSQL dbOp = null;
    try {
      MultiRecParser bp = new MultiRecParser(listaDati, riga, utente);
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(pConn,0);
       String query = 
        "select codice_modello, corpo, tipo,"+
        "       driver, connessione, "+
        "       utente, passwd, "+
        "       dsn "+
        "  from blocchi "+
        " where area = :AREA and "+
        "       blocco = :BLOCCO ";

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", areaBlocco);
      dbOp.setParameter(":BLOCCO", nomeBlocco);
      dbOp.execute();
      rst = dbOp.getRstSet();
      if (rst.next()) {
        codMod    = rst.getString(1);
        tipo      = rst.getString(3);
        sDsn      = rst.getString(8);
        if (sDsn == null) {
          sDsn = "";
        }
        protetto = true;
        bis = dbOp.readClob("corpo");
        StringBuffer sb = new StringBuffer();
        int ic;
        while ((ic =  bis.read()) != -1) {
          sb.append((char)ic);
        }
        corpo = sb.toString();

        istruz = "";
        if (!sDsn.equalsIgnoreCase("")) {
          Connessione cn = new Connessione(dbOp,sDsn);
          blkDriver = cn.getDriver();
          blkConn   = cn.getConnessione();
          blkUte    = cn.getUtente();
          blkPswd   = cn.getPassword();
        } else {
          blkDriver = rst.getString(4);
          blkConn   = rst.getString(5);
          blkUte    = rst.getString(6);
          blkPswd   = rst.getString(7);
        }
      }
      
      //fase per istruzione
     // dbOp = SessioneDb.getInstance().createIDbOperationSQL(pConn,0);
/*      query = 
       "select istruzione "+
       "  from blocchi "+
       " where area = '"+areaBlocco+"' and "+
       "       blocco = '"+nomeBlocco+"' ";*/

      query = 
          "select istruzione "+
          "  from blocchi "+
          " where area = :AREA and "+
          "       blocco = :BLOCCO ";

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", areaBlocco);
      dbOp.setParameter(":BLOCCO", nomeBlocco);
      dbOp.execute();
      rst = dbOp.getRstSet();
      if (rst.next()) {
        try {
          bis = dbOp.readClob(1);
        } catch (Exception esql) {
          bis = null;
        }
        int ic2;
        StringBuffer sb2 = new StringBuffer();
        if (bis != null) {
          sb2 = new StringBuffer();
          while ((ic2 =  bis.read()) != -1) {
            sb2.append((char)ic2);
          }
          istruz = sb2.toString();
        } else {
          istruz = "";
        }
      }
      if (leg == null) {
        leg = "";
      }

      if (ord == null) {
        ord = "";
      }

      if (blkDriver == null) {
        blkDriver = "";
      }

      if (blkConn == null) {
        blkConn = "";
      }

      if (blkUte == null) {
        blkUte = "";
      }

      if (blkPswd == null) {
        blkPswd = "";
      }

      if (istruz == null) {
        istruz = "";
      }

      inifile = (String)pRequest.getAttribute("GDM_CONFIG_FILE");
      if (inifile == null) {
        inifile = "";
      }

      if (tipo.equalsIgnoreCase("J")) {
        mr = new Multirecord(pRequest,nomeBlocco, corpo, leg, nav, bp);
        mr.setAjax(false);
        mr.setNested(true);
        mr.setAggiungi(agg);
        mr.setProteggi(protetto);
        if (!pdo.equalsIgnoreCase("")) {
          mr.setThemesPath("..");
        }
        mr.setDomini(true);
        mr.setOrdinamento(pRequest,ord);
        mr.setNonCaricareDati(false);
        mr.setJoin(istruz);
        mr.setEscludiCompetenze(noCompetenze);
      }

      if (tipo.equalsIgnoreCase("S")) {
        tab = "GDM_"+ricavaAliasView(areaBlocco,codMod);

        if (!leg.equalsIgnoreCase("")) {
          leg += " AND";
        
        }
        leg += " GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',documento,'L','"+utente+"','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1";
        leg += " AND STATO NOT IN ('CA','RE','PB','AN') ";

        mr = new Multirecord(pRequest, nomeBlocco, corpo, null, tab, leg, ord, nav, bp);
        mr.setAjax(false);
        mr.setNested(true);
        mr.setAggiungi(agg);
        mr.setProteggi(protetto);
        if (!pdo.equalsIgnoreCase("")) {
          mr.setThemesPath("..");
        }
        mr.setDomini(true);
        mr.setNonCaricareDati(false);
      } 

      String filtroXML = "";
      String campo = "";
      String valore = "";
      if (tipo.equalsIgnoreCase("P")) {
        us = pRequest.getParameter("us");
        if (us == null) {
          us = "";
        }

        String ruolo = pRequest.getParameter("ru");
        if (ruolo == null) {
          ruolo = "";
        }

        String modulo = pRequest.getParameter("mo");
        if (modulo == null) {
          modulo = "";
        }

        String istanza = pRequest.getParameter("is");
        if (istanza == null) {
          istanza = "";
        }

        String nominativo = pRequest.getParameter("ul");
        if (nominativo == null) {
          nominativo = "";
        }

        String myUrl = "";

        if (Parametri.PROTOCOLLO.equalsIgnoreCase("")) {
          myUrl = pRequest.getScheme();
        } else {
          myUrl = Parametri.PROTOCOLLO;
        }
        if (Parametri.SERVERNAME.equalsIgnoreCase("")) {
          myUrl += "://"+pRequest.getServerName();
        } else {
          myUrl += "://"+Parametri.SERVERNAME;
        }
        if (Parametri.SERVERPORT.equalsIgnoreCase("")) {
          myUrl += ":"+pRequest.getServerPort();
        } else {
          myUrl += ":"+Parametri.SERVERPORT;
        }

        String contextPath = pRequest.getContextPath();

        filtroXML = "<FILTRI>";
        if (!leg.equalsIgnoreCase("")) {
          StringTokenizer st = new StringTokenizer(leg,"@");
          while (st.hasMoreElements()) {
            campo = st.nextToken();
            valore = bp.bindingDeiParametri("<"+campo+">:"+campo+"</"+campo+">");
            if (valore == null) {
              valore = "";
            }
            filtroXML += valore;
          }
        }
        filtroXML += "</FILTRI>";

        sXML = 
          "<FUNCTION_INPUT>"+
            "<CONNESSIONE_DB>"+
              "<USER>"+blkUte+"</USER>"+
              "<PASSWORD>"+blkPswd+"</PASSWORD>"+
              "<HOST_STRING>"+blkConn+"</HOST_STRING>"+
              "<DRIVER>"+blkDriver+"</DRIVER>"+
            "</CONNESSIONE_DB>"+
            "<CONNESSIONE_TOMCAT>"+
              "<UTENTE>"+us+"</UTENTE>"+
              "<NOMINATIVO>"+nominativo+"</NOMINATIVO>"+
              "<RUOLO>"+ruolo+"</RUOLO>"+
              "<MODULO>"+modulo+"</MODULO>"+
              "<ISTANZA>"+istanza+"</ISTANZA>"+
              "<PROPERTIES>"+inifile+"</PROPERTIES>"+
              "<URL_SERVER>"+myUrl+"</URL_SERVER>"+
              "<CONTEXT_PATH>"+contextPath+"</CONTEXT_PATH>"+
            "</CONNESSIONE_TOMCAT>"+
            filtroXML+
          "</FUNCTION_INPUT>";
        mr = new Multirecord(pRequest, nomeBlocco, corpo, tipo, istruz, sXML, nav);
        mr.setAjax(false);
        mr.setNested(true);
        mr.setAggiungi(agg);
        if (!pdo.equalsIgnoreCase("")) {
          mr.setThemesPath("..");
        }
      }

    } catch (Exception e){
      //Debug Tempo
      stampaTempo("BloccoMultirecord - Fine",pArea,pModello,pBlocco,ptime);
      //Debug Tempo
      throw e;
    }
    finally {
      free(dbOp);
    }
    //Debug Tempo
    stampaTempo("BloccoMultirecord - Fine",pArea,pModello,pBlocco,ptime);
    //Debug Tempo
  }

//  public java.lang.String getBlocco() {
//    return blocco;
//  }
//  
//  public String getMVPG() {
//    return mr.getMVPG();
//  }
//  
//  public void setMVPG(String newMPVG) {
//    mr.setMVPG(newMPVG);
//  }
//
//  public java.lang.String getNomeBlocco() {
//    return nomeBlocco;
//  }
//
//  public boolean getNavigatore() {
//    return pNav;
//  }
  
  public LinkedList<String> getListaCampi() {
  	return mr.listaCampi;
  }

  public String getValue() {
    //Debug Tempo
    long ptime = stampaTempo("BloccoNested::getValue - Inizio",area,codMod,blocco,0);
    //Debug Tempo
    String bloccoWHtml = "";
    IDbOperationSQL dbOp = null;
    if ((rw!=null) && (rw.equalsIgnoreCase("P"))) {
      bloccoWHtml = getPRNValue();
    } else {
      try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(pConn);
//        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
        MultiRecParser bp = new MultiRecParser(listaDati,riga, utente);
        if (dbn != null) {
        	LinkedList<String>  lDati = new LinkedList<String>();
        	String iddoc = bp.findParamValue("ID_DOCUMENTO", null);
        	Element e = dbn.getIdDocumento(iddoc);
        	if (e != null) {
	          for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext();) {
	          	Element record = (Element)iterator.next();
	          	lDati.addLast(record.getText());
	          }
        	}
          bloccoWHtml = mr.creaHtmlNestedMonorecord(dbOp, sRequest, bp, lDati, area, codMod);
        } else {
	        if (tipo.equalsIgnoreCase("J")) {
	          String primRec = sRequest.getParameter(nomeBlocco+"_BLCNAV");
	          if (primRec == null) {
	            primRec = "";
	          }
	          bloccoWHtml = mr.creaHtmlIQuery(dbOp,sRequest,bp,false, area, codMod);
	        }
	        if (tipo.equalsIgnoreCase("S")) {
	          bloccoWHtml = mr.creaHtml(dbOp,sRequest,bp,false);
	        }
	        if (tipo.equalsIgnoreCase("P")) {
	          bloccoWHtml = mr.creaHtmlXML(dbOp,sRequest,bp,false);
	        }
        }
        free(dbOp);
      } catch (Exception e) {
        free(dbOp);
        logger.error("Errore in costruzione Multirecord! Area: "+area+" - Blocco: "+blocco,e);
        bloccoWHtml = "";
      }
    }
    
    //Debug Tempo
    stampaTempo("BloccoMultirecord::getValue - Fine",area,codMod,blocco,ptime);
    //Debug Tempo
    if (sRequest.getParameter("gdm_Ajax") == null) {
     return "<SPAN ID='BLK_"+nomeBlocco+"' >"+bloccoWHtml+"</SPAN>";
    } else {
     return "<SPAN ID='gdmIdAjax_BLK_"+nomeBlocco+"' >"+bloccoWHtml+"</SPAN>";
    }
  }
  public String getZValue() {
    return null;
  }
  public String getPRNValue() {
    //Debug Tempo
    long ptime = stampaTempo("BloccoMultirecord::getPRNValue - Inizio",area,codMod,blocco,0);
    //Debug Tempo
    String bloccoRHtml = "";
    IDbOperationSQL dbOp = null;
    try {
      MultiRecParser bp = new MultiRecParser(listaDati,riga, utente);
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(pConn);
//      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
      if (tipo.equalsIgnoreCase("J")) {
        bloccoRHtml = mr.creaHtmlIQuery(dbOp,sRequest,bp,false, area, codMod);
      }
      if (tipo.equalsIgnoreCase("S")) {
        bloccoRHtml = mr.creaHtml(dbOp,sRequest,bp,false);
      }
      if (tipo.equalsIgnoreCase("P")) {
        bloccoRHtml = mr.creaHtmlXML(dbOp,sRequest,bp,false);
      }
    } catch (Exception e) {

      logger.error("Errore in costruzione Multirecord! Area: "+area+" - Blocco: "+blocco,e);
      bloccoRHtml = "";
    }
    finally {
      free(dbOp);
    }
    //Debug Tempo
    stampaTempo("BloccoMultirecord::getPRNValue - Fine",area,codMod,blocco,ptime);
    //Debug Tempo
    return  bloccoRHtml;
  }
  public String getPRNComValue() {
    return getPRNValue();
  }

  public  void release() {
  }

  public void settaListFields(String l_fields){
  }

  /**
   * 
   */
  private String ricavaAliasView (String ar, String cm) {
    //Debug Tempo
    long ptime = stampaTempo("BloccoNested::ricavaAliasView - Inizio",ar,cm,blocco,0);
    //Debug Tempo
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    String          idtipodoc = null;
    String          codmod = null;
    String          retval = "";

    query = " SELECT ID_TIPODOC, CODICE_MODELLO_PADRE"+
            " FROM MODELLI"+
            " WHERE AREA = :AREA"+
            "   AND CODICE_MODELLO = :CM";
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(pConn);
//      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",ar);
      dbOp.setParameter(":CM",cm);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next() ) {
        idtipodoc = rst.getString("ID_TIPODOC");
        codmod = rst.getString("CODICE_MODELLO_PADRE");
      } else {
        logger.error("Errore in ricerca Identificativo tipo documento. - Modello non trovato!");
        free(dbOp);
        return "";
      }

      if (idtipodoc == null) {
        idtipodoc = "";
      }

      dbOp.setStatement(query);
      if (idtipodoc.equalsIgnoreCase("")) {
        dbOp.setParameter(":AREA",ar);
        dbOp.setParameter(":CM",codmod);
        dbOp.execute();
        rst = dbOp.getRstSet();

        if (rst.next() ) {
           idtipodoc = rst.getString("ID_TIPODOC");
        }
      }

      query = " SELECT ALIAS_VIEW"+
              " FROM TIPI_DOCUMENTO"+
              " WHERE ID_TIPODOC = :ID_TD";

      dbOp.setStatement(query);
      dbOp.setParameter(":ID_TD", idtipodoc);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next() ) {
         retval = rst.getString(1);
      }

      //Debug Tempo
      stampaTempo("BloccoMultirecord::ricavaAliasView - Fine",ar,cm,blocco,ptime);
      //Debug Tempo
      return retval;
    
    } catch (Exception e) {

      logger.error("BloccoMultirecord::ricavaAliasView - "+e.toString());
      return "";
    }
    finally {
      free(dbOp);
    }
  }

  /**
   *
   */
  private void free(IDbOperationSQL dbOp) {
    try {
      dbOp.close();
    } catch (Exception e) { }
  }

  private long stampaTempo(String sMsg, String area, String cm, String blocco, long ptime) {
    if (!debuglog) {
      return 0;
    }
    long adesso = Calendar.getInstance().getTimeInMillis();
    long trascorso = 0;
    if (ptime > 0) {
      trascorso = adesso - ptime;
    }
    if (Parametri.DEBUG.equalsIgnoreCase("1") && ptime > 0) {
      logger.debug("\n"+sMsg+"\n-Area:"+area+" -CodiceModello:"+cm+" -Blocco:"+blocco+"\n-------->Trascorso dall'inizio: "+trascorso+"\n");
    }
    if (Parametri.DEBUG.equalsIgnoreCase("2")) {
      logger.debug("\n"+sMsg+"\n-Area:"+area+" -CodiceModello:"+cm+" -Blocco:"+blocco+"\n----->TIME: "+adesso+"\n--------Trascorso dall'inizio: "+trascorso+"\n");
    }
    return adesso;
  }
  
  public String getArea() {
    return parArea;
  }

  public String getCm() {
    return parCm;
  }

  public String getBlocco() {
    return parBlocco;
  }

  public void setRiga(int newRiga) {
    riga = newRiga;
  }
  
  public void setDati(DatiBlocchiNested dati) {
  	dbn = dati;
  }
}
