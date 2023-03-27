package it.finmatica.modulistica.modulisticapack;
 
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.net.URLEncoder;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modulistica.personalizzazioni.Personalizzazioni;
import it.finmatica.modutils.multirecord.Multirecord;
import it.finmatica.modutils.informazioniblocco.InformazioniBlocco;
import it.finmatica.modulistica.modulisticapack.Connessione;
import org.apache.log4j.Logger;

public class BloccoMultirecord implements IElementoModello {
  protected String blocco = "";
  protected String nomeBlocco = "";
  protected String area = "";
  protected String codMod = "";
  protected String inifile = "";
  protected String caricato = "";
  protected String tipo = "";
  protected String rw = "";  
  protected String mvpg = "";
  protected String areaBlocco = "";
  protected boolean isHorizontal = false;
  protected boolean pNav = false;
  protected Multirecord mr = null;
  protected HttpServletRequest sRequest;
  private  static Logger logger = Logger.getLogger(BloccoMultirecord.class);
  private  boolean debuglog = logger.isDebugEnabled();
  protected boolean   settoreProtetto = false;

  public BloccoMultirecord(HttpServletRequest pRequest, String pArea, String pModello, String pBlocco, boolean pNavigatore, boolean nonCaricareDati, IDbOperationSQL dbOpEsterna) throws Exception {
    //Debug Tempo
    long ptime = stampaTempo("BloccoMultirecord - Inizio",pArea,pModello,pBlocco,0);
    //Debug Tempo
    BufferedInputStream bis;
    pNav = pNavigatore;
    area = pArea;  
    boolean         protetto = false;
    boolean         ajax = false;
    IDbOperationSQL  dbOp2   = null;
    String          utente  = (String)pRequest.getSession().getAttribute("UtenteGDM");
    String          pdo     = (String)pRequest.getSession().getAttribute("pdo");
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
    mvpg = pRequest.getParameter("MVPG");

    sRequest = pRequest;
    InformazioniBlocco infoBlk = new InformazioniBlocco(pBlocco);
    String nomeBloccoOrig = infoBlk.getBlocco();
    String areaBloccoOrig = infoBlk.getAreaBlocco();
    if ((areaBloccoOrig == null) || (areaBloccoOrig.length() == 0)){
      areaBloccoOrig = pArea;
    }

    //Controllo se c'� una personalizzazione
    Personalizzazioni pers = null;
    pers = (Personalizzazioni)pRequest.getSession().getAttribute("_personalizzazioni_gdm");
    if (pers != null) {
      String persBlocco = pers.getPersonalizzazione(Personalizzazioni.BLOCCHI, areaBloccoOrig+"#"+nomeBloccoOrig);
      int j = persBlocco.indexOf("#");
      areaBlocco = persBlocco.substring(0,j);
      nomeBlocco = persBlocco.substring(j+1);
    }

    if (mvpg == null) {
      mvpg = "";
    }
    blocco = pBlocco;
    String nomeServ = (String)pRequest.getSession().getAttribute("p_nomeservlet");
    if (nomeServ == null) {
      nomeServ = "";
    } else {
      if (pdo.length() != 0) {
        nomeServ += ".do";
      }
    }

    rw = pRequest.getParameter("rw");
    if (rw.equalsIgnoreCase("W")) {
      ajax = true;
    } 
    if (rw.equalsIgnoreCase("P") || rw.equalsIgnoreCase("R") || rw.equalsIgnoreCase("V")) {
      nonCaricareDati = false;
    }
    if (nonCaricareDati) {
      caricato = "<input type='hidden' id='GDM_BLK_"+nomeBlocco+"_CAR' value='N' />\n";
    } else {
      caricato = "<input type='hidden' id='GDM_BLK_"+nomeBlocco+"_CAR' value='Y' />\n";
    }
    IDbOperationSQL dbOp = null;
    try {
      ModulisticaParser bp = new ModulisticaParser(pRequest);
      Properties pbp = new Properties();
      if (dbOpEsterna==null) {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      }
      else {
        dbOp = dbOpEsterna;
      }

       String query = 
        "select bm.condizioni_legame, bm.ordinamento, "+
        "       bm.condizioni_navigazione, bm.aggiungi, "+
        "       b.codice_modello, b.corpo, b.tipo,"+
        "       b.driver, b.connessione, "+
        "       b.utente, b.passwd, "+
        "       b.istruzione, "+
        "       b.dsn "+
        "  from blocchi_modello bm, " +
        "       blocchi b "+
        " where bm.area = :AREA and "+
        "       bm.codice_modello = :CM and " +
        "       bm.blocco = :BLOCCO and"+
        "       '"+nomeBlocco+"' = b.blocco and"+
        "       '"+areaBlocco+"' = b.area";

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", pArea);
      dbOp.setParameter(":CM", pModello);
      dbOp.setParameter(":BLOCCO", nomeBloccoOrig);
      dbOp.execute();
      rst = dbOp.getRstSet();
      if (rst.next()) {
        leg       = rst.getString(1);
        ord       = rst.getString(2);
        nav       = rst.getInt(3);
        agg       = rst.getString(4);
        codMod    = rst.getString(5);
        tipo      = rst.getString(7);
        sDsn      = rst.getString(13);
        if (sDsn == null) {
          sDsn = "";
        }
        if (sDsn.length() != 0) {
          //dbOp2 = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
          Connessione cn = new Connessione(dbOp,sDsn);
          blkDriver = cn.getDriver();
          blkConn   = cn.getConnessione();
          blkUte    = cn.getUtente();
          blkPswd   = cn.getPassword();
         // free(dbOp2);
        } else {
          blkDriver = rst.getString(8);
          blkConn   = rst.getString(9);
          blkUte    = rst.getString(10);
          blkPswd   = rst.getString(11);
        }
//        istruz    = rst.getString(12);

        if ((rw!=null) && (rw.equalsIgnoreCase("P") || rw.equalsIgnoreCase("V"))) {
          pNav = false;
          nav = 100;
        }

        if (agg == null) {
          agg = "";
        } else {
          String newNomeServ = nomeServ.replaceFirst("ServletModulisticaCartella","../common/DocumentoView");
          if (agg.equalsIgnoreCase("Y")) {
            agg = newNomeServ+"?area="+areaBlocco+"&amp;cm="+codMod+"&amp;rw=W&amp;wfather="+nomeBlocco;
          } else {
            pbp.setProperty("TIPO","BLK");
            bp.setExtraKeys(pbp);
            agg = bp.bindingDeiParametri(agg);
//            agg = agg.replaceAll("'", "&#39;");
//            agg = agg.replaceAll("%", "%25");
            agg += "&amp;wfather="+nomeBlocco;
            agg = newNomeServ+"?area="+areaBlocco+"&amp;cm="+codMod+"&amp;rw=W&amp;"+ agg;
            pbp.setProperty("TIPO","");
            bp.setExtraKeys(pbp);
           }
          if (mvpg.length() != 0) {
            agg += "&amp;MVPG="+mvpg;
          }
        }
        if (agg.length() == 0) {
          agg = "N";
        }

//        if (!agg.equalsIgnoreCase("N")) {
          Dominio dp = null;
          ListaProtetti lp = (ListaProtetti)pRequest.getSession().getAttribute("listaProtetti");
          String myVal = null;
          if (lp != null) {
            int numDom = lp.domini.size();
            int i = 0;
            while (i < numDom && myVal == null) { 
              dp = (Dominio)lp.domini.get(i);
                myVal = dp.getValore("$B$"+nomeBlocco);
              i++;
            }
          }
          if (myVal != null) {
            if (myVal.equalsIgnoreCase("S")) {
              agg = "N";
              protetto = true;
            }
          }
//        }

        bis = dbOp.readClob(6);
        StringBuffer sb = new StringBuffer();
        int ic;
        while ((ic =  bis.read()) != -1) {
          sb.append((char)ic);
        }
        corpo = sb.toString();

        try {
        bis = dbOp.readClob(12);
        } catch (Exception esql) {
          bis = null;
        }
        if (bis != null) {
          sb = new StringBuffer();
          while ((ic =  bis.read()) != -1) {
            sb.append((char)ic);
          }
          istruz = sb.toString();
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
        mr.setAjax(ajax);
        mr.setAggiungi(agg);
        mr.setProteggi(protetto);
        if (pdo.length() != 0) {
          mr.setThemesPath("..");
        }
        mr.setDomini(true);
        mr.setOrdinamento(pRequest,ord);
        mr.setNonCaricareDati(nonCaricareDati);
        mr.setJoin(istruz);
      }

      if (tipo.equalsIgnoreCase("S")) {
        tab = ricavaNomeTab(areaBlocco,codMod,dbOpEsterna);

        if (leg.length() != 0) {
          leg += " AND";
        }
        if (isHorizontal) {
          leg += " GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',d.id_documento,'L','"+utente+"','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1";
          leg += " AND D.STATO_DOCUMENTO NOT IN ('CA','RE','PB','AN') ";
          mr = new Multirecord(pRequest, nomeBlocco, corpo, codMod, tab, leg, ord, nav, bp, isHorizontal);
        } else {
          leg += " GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',documento,'L','"+utente+"','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1";
          leg += " AND STATO NOT IN ('CA','RE','PB','AN') ";
          mr = new Multirecord(pRequest, nomeBlocco, corpo, null, tab, leg, ord, nav, bp, isHorizontal);
        }

        mr.setAjax(ajax);
        mr.setAggiungi(agg);
        mr.setProteggi(protetto);
        if (pdo.length() != 0) {
          mr.setThemesPath("..");
        }
        mr.setDomini(true);
        mr.setNonCaricareDati(nonCaricareDati);
      } 

      String filtroXML = "";
      String campo = "";
      String valore = "";
      if (tipo.equalsIgnoreCase("P")) {
        String us = pRequest.getParameter("us");
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

        if (Parametri.PROTOCOLLO.length() == 0) {
          myUrl = pRequest.getScheme();
        } else {
          myUrl = Parametri.PROTOCOLLO;
        }
        if (Parametri.SERVERNAME.length() == 0) {
          myUrl += "://"+pRequest.getServerName();
        } else {
          myUrl += "://"+Parametri.SERVERNAME;
        }
        if (Parametri.SERVERPORT.length() == 0) {
          myUrl += ":"+pRequest.getServerPort();
        } else {
          myUrl += ":"+Parametri.SERVERPORT;
        }

        String contextPath = pRequest.getContextPath();

        filtroXML = "<FILTRI>";
        if (leg.length() != 0) {
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
        mr.setAjax(ajax);
        mr.setAggiungi(agg);
        if (pdo.length() != 0) {
          mr.setThemesPath("..");
        }
      }


    } catch (Exception e){
      //free(dbOp2);
      //Debug Tempo
      stampaTempo("BloccoMultirecord - Fine",pArea,pModello,pBlocco,ptime);
      //Debug Tempo
      throw new Exception (e.toString());
    }
    finally {
      if (dbOpEsterna == null ) free(dbOp);
    }
    //Debug Tempo
    stampaTempo("BloccoMultirecord - Fine",pArea,pModello,pBlocco,ptime);
    //Debug Tempo
  }

  public String getBlocco() {
    return blocco;
  }
  
  public String getMVPG() {
    return mr.getMVPG();
  }
  
  public void setMVPG(String newMPVG) {
    mr.setMVPG(newMPVG);
  }

  public String getNomeBlocco() {
    return nomeBlocco;
  }

  public boolean getNavigatore() {
    return pNav;
  }

  public String getValue() {
    return getValue(null);
  }

  public String getValue(IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("BloccoMultirecord::getValue - Inizio",areaBlocco,codMod,blocco,0);
    //Debug Tempo
    String bloccoWHtml = "";
    IDbOperationSQL dbOp = null;
    if ((rw!=null) && (rw.equalsIgnoreCase("P"))) {
      bloccoWHtml = getPRNValue(dbOpEsterna);
    } else {
      try {
        if (dbOpEsterna == null) {
          dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
        }
        else {
          dbOp = dbOpEsterna;
        }

        ModulisticaParser bp = new ModulisticaParser(sRequest);
        if (settoreProtetto) {
          mr.setAggiungi("N");
          mr.setProteggi(true);
        }
        if (tipo.equalsIgnoreCase("J")) {
          String primRec = sRequest.getParameter(nomeBlocco+"_BLCNAV");
          if (primRec == null) {
            primRec = "";
          }
          bloccoWHtml = mr.creaHtmlIQuery(dbOp,sRequest,bp,pNav, areaBlocco, codMod);
        }
        if (tipo.equalsIgnoreCase("S")) {
          bloccoWHtml = mr.creaHtml(dbOp,sRequest,bp,pNav);
        }
        if (tipo.equalsIgnoreCase("P")) {
          bloccoWHtml = mr.creaHtmlXML(dbOp,sRequest,bp,pNav);
        }
      } catch (Exception e) {
        logger.error("Errore in costruzione Multirecord! Area: "+area+" - Blocco: "+blocco,e);
        bloccoWHtml = "";
      }
      finally {
        if (dbOpEsterna == null) free(dbOp);
      }
    }
    
    //Debug Tempo
    stampaTempo("BloccoMultirecord::getValue - Fine",areaBlocco,codMod,blocco,ptime);
    //Debug Tempo
    if (sRequest.getParameter("gdm_Ajax") == null) {
     return "<span id='BLK_"+nomeBlocco+"' >"+caricato+bloccoWHtml+"</span>";
    } else {
     return "<span id='gdmIdAjax_BLK_"+nomeBlocco+"' >"+caricato+bloccoWHtml+"</span>";
    }
  }
  public String getZValue() {
    return null;
  }

  public String getPRNValue() {
    return getPRNValue(null);
  }

  public String getPRNValue(IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("BloccoMultirecord::getPRNValue - Inizio",areaBlocco,codMod,blocco,0);
    //Debug Tempo
    String bloccoRHtml = "";
    IDbOperationSQL dbOp = null;
    try {
      ModulisticaParser bp = new ModulisticaParser(sRequest);
      if (dbOpEsterna == null) {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      }
      else {
        dbOp = dbOpEsterna;
      }

      if (tipo.equalsIgnoreCase("J")) {
        bloccoRHtml = mr.creaHtmlIQuery(dbOp,sRequest,bp,false, areaBlocco, codMod);
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
      if (dbOpEsterna == null) free(dbOp);
    }
    //Debug Tempo
    stampaTempo("BloccoMultirecord::getPRNValue - Fine",areaBlocco,codMod,blocco,ptime);
    //Debug Tempo
    return  bloccoRHtml;
  }
  public String getPRNComValue() {
    return getPRNValue();
  }

  public String getPRNComValue(IDbOperationSQL dbOpEsterna) {
    return getPRNValue(dbOpEsterna);
  }

  public  void release() {
  }

  public void settaListFields(String l_fields){
  }

  /**
   * 
   */
  private String ricavaNomeTab (String ar, String cm, IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("BloccoMultirecord::ricavaAliasView - Inizio",ar,cm,blocco,0);
    //Debug Tempo
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    String          idtipodoc = null;
    String          codmod = null;
    String          retval = "";

    query = "SELECT ID_TIPODOC, CODICE_MODELLO_PADRE"+
            " FROM MODELLI"+
            " WHERE AREA = :AREA"+
            "   AND CODICE_MODELLO = :CM";
    try {
      dbOp = dbOpEsterna;

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

        return "";
      }

      if (idtipodoc == null) {
        idtipodoc = "";
      }

      dbOp.setStatement(query);
      if (idtipodoc.length() == 0) {
        dbOp.setParameter(":AREA",ar);
        dbOp.setParameter(":CM",codmod);
        dbOp.execute();
        rst = dbOp.getRstSet();

        if (rst.next() ) {
           idtipodoc = rst.getString("ID_TIPODOC");
        }
      }

      query = "SELECT ALIAS_VIEW, F_NOME_TABELLA(AREA_MODELLO, NOME)"+
              " FROM TIPI_DOCUMENTO"+
              " WHERE ID_TIPODOC = :TIPODOC";

      dbOp.setStatement(query);
      dbOp.setParameter(":TIPODOC", idtipodoc);
      dbOp.execute();
      rst = dbOp.getRstSet();

      isHorizontal = false;
      if (rst.next() ) {
//         retval = rst.getString(2);
//         if (retval == null || retval.length() == 0) {
//           retval = rst.getString(1);
//           if (retval != null && !retval.length() == 0) {
//             retval = "GDM_"+retval;
//           }
//         } else {
//           isHorizontal = true;
//         }
         retval = rst.getString(1);
         retval = "GDM_"+retval;
      }


      //Debug Tempo
      stampaTempo("BloccoMultirecord::ricavaAliasView - Fine",ar,cm,blocco,ptime);
      //Debug Tempo
      return retval;
    
    } catch (Exception e) {

      logger.error("BloccoMultirecord::ricavaAliasView - "+e.toString());
      return "";
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
    if (debuglog) {
      long adesso = Calendar.getInstance().getTimeInMillis();
      long trascorso = 0;
      if (ptime > 0) {
        trascorso = adesso - ptime;
      }
      long min_time = Long.parseLong(Parametri.MIN_TIME_LOG);
      if (trascorso < min_time) {
        return adesso;
      }
      if (Parametri.DEBUG.equalsIgnoreCase("1") && ptime > 0) {
        logger.debug("\n"+sMsg+"\n-Area:"+area+" -CodiceModello:"+cm+" -Blocco:"+blocco+"\n-------->Trascorso dall'inizio: "+trascorso+"\n");
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        logger.debug("\n"+sMsg+"\n-Area:"+area+" -CodiceModello:"+cm+" -Blocco:"+blocco+"\n----->TIME: "+adesso+"\n--------Trascorso dall'inizio: "+trascorso+"\n");
      }
      return adesso;
    }
    return 0;
  }

  public void settaProtetto(boolean b_protetto) {
    settoreProtetto = b_protetto;
  }
  
  public boolean getProtetto() {
    return settoreProtetto;
  }

  /**
	 * Codifica la stringa in formato URL.
	 * 
	 * @param string la stringa da codificare
	 * @return la stringa codificata in formato URL.
	 */
	public String urlencode(String string) {
		if (string == null)
			return "";
		
		try {
			return URLEncoder.encode(string, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(string);
		}
	}

}
