package it.finmatica.modulistica.modulisticapack;
 
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.sql.*;
//import java.io.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import org.apache.log4j.Logger;

public class BloccoPopup implements IElementoModello {
  private  static Logger logger = Logger.getLogger(BloccoPopup.class);
  private  boolean debuglog = logger.isDebugEnabled();
  private  boolean settoreProtetto = false;
  private String   ricerca = "";
  protected String user = "";
  protected String ruolo = "";
  protected String modulo = "";
  protected String istanza = "";
  protected String nominativo = "";
  protected String bloccoWHtml = "";

  public BloccoPopup(HttpServletRequest pRequest, String area, String cm, String dato, String area_blocco, String blocco, String pNome, String pdo, String campi, IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("BloccoPopup - Inizio",area,cm,blocco,0);
    //Debug Tempo
    Dominio dp = null;
    ListaProtetti lp = (ListaProtetti)pRequest.getSession().getAttribute("listaProtetti");
    String myVal = null;
    if (lp != null) {
      int numDom = lp.domini.size();
      int i = 0;
      while (i < numDom && myVal == null) { 
        dp = (Dominio)lp.domini.get(i);
          myVal = dp.getValore("$P$"+pNome);
        i++;
      }
    }
    if (myVal != null) {
      if (myVal.equalsIgnoreCase("S")) {
        bloccoWHtml = "";
        //Debug Tempo
        stampaTempo("BloccoPopup - Fine",area,cm,blocco,ptime);
        //Debug Tempo
        return;
      }
    }


    String rw = pRequest.getParameter("rw");
    if (!rw.equalsIgnoreCase("W") && !rw.equalsIgnoreCase("Q")) {
      //Debug Tempo
      stampaTempo("BloccoPopup - Fine",area,cm,blocco,ptime);
      //Debug Tempo
      return;
    }
    user = (String)pRequest.getSession().getAttribute("Utente");
    if (user == null) {
      user = "GUEST";
    }

    ruolo = (String)pRequest.getSession().getAttribute("Ruolo");
    if (ruolo == null) {
      ruolo = "GUEST";
    }

    modulo = (String)pRequest.getSession().getAttribute("Modulo");
    if (modulo == null) {
      modulo = "";
    }

    istanza = (String)pRequest.getSession().getAttribute("Istanza");
    if (istanza == null) {
      istanza = "";
    }

    nominativo = (String)pRequest.getSession().getAttribute("UserLogin");
    if (nominativo == null) {
      nominativo = "";
    }
    caricaInformazioniBlocco(area_blocco, blocco, dbOpEsterna);
    bloccoWHtml = creaPulsante(area, cm, dato, area_blocco, blocco, pNome, pdo, campi);
    //Debug Tempo
    stampaTempo("BloccoPopup - Fine",area,cm,blocco,ptime);
    //Debug Tempo
  }

  private String creaPulsante(String area, String cm, String dato, String area_blocco, String bloccoPopup, String nomePopup, String pdo, String campi) {
    //Debug Tempo
    long ptime = stampaTempo("BloccoPopup::creaPulsante - Inizio",area,cm,bloccoPopup,0);
    //Debug Tempo
    String retval = "";
    String campo = "";
    String valore = "";
    retval += "<script type='text/javascript' >\n"+
              "   function showBlocco"+nomePopup+"() {\n"+
              "      var szURL;\n";
    if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
      retval += "      szURL = '../../"+Parametri.APPLICATIVO+"/ServletPopup?area_blocco="+area_blocco+"&blocco="+bloccoPopup+"&dato="+dato+"&campi="+campi+"&area="+area+"&cm="+cm+"';\n";
    } else {
      retval += "      szURL = 'ServletPopup?area_blocco="+area_blocco+"&blocco="+bloccoPopup+"&dato="+dato+"&campi="+campi+"&area="+area+"&cm="+cm+"';\n";
    }
    retval += "      szURL += '&us="+user+"&="+ruolo+"&ul="+nominativo+"&mo="+modulo+"&is="+istanza+"&BLK_BLCNAV=1';\n";
    if (ricerca.length() != 0) {
      StringTokenizer st = new StringTokenizer(ricerca,"@");
      while (st.hasMoreElements()) {
        valore = "";
        campo = st.nextToken();
        int i = campo.indexOf("(");
        if (i > -1) {
          int j = campo.indexOf(")");
          valore = campo.substring(i+1,j);
          campo = campo.substring(0,i);
          int x = valore.length();
          String sConst = valore.substring(1,x-1);
          if (valore.equalsIgnoreCase("'"+sConst+"'")) {
            retval += "      szURL += '&"+campo+"="+sConst+"'\n";
          } else {
//            retval += "      szURL += '&amp;"+campo+"='+document.getElementById(\"submitForm\")."+valore+".value;\n";
            retval += "      szURL += '&"+campo+"='+encodeURIComponent(getValoreCampo(\""+valore+"\"));\n";
          }
        }
      }
    }
//    retval += "      window.open(szURL,\"SceltaValori\",\"width=600,height=500, resizable=yes, scrollbars=1,top=134,left=212 \");\n"+
    retval += "      apriSceltaValoreModale(szURL);\n"+
              "      resetCursor();"+
              "   }\n"+
              "</script>";
    retval += "<span id='POPUP_"+nomePopup+"'><a class='AFCDataLink' href='javascript:showBlocco"+nomePopup+"()'>\n";
    if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
      retval += "<img style='border: none' src='../common/images/gdm/dot.gif' alt='Finestra di selezione valori' /></a></SPAN>&nbsp;\n";
    } else {
      retval += "<img style='border: none' src='images/gdm/dot.gif' alt='Finestra di selezione valori' /></a></SPAN>&nbsp;\n";
    }

    //Debug Tempo
    stampaTempo("BloccoPopup::creaPulsante - Fine",area,cm,bloccoPopup,ptime);
    //Debug Tempo
    return retval;
  }

  private void caricaInformazioniBlocco(String area, String blocco, IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("BloccoPopup::caricaInformazioniBlocco - Fine",area,"",blocco,0);
    //Debug Tempo
    IDbOperationSQL dbOp = null;
    String query ="";
    ResultSet rst = null;
//    BufferedInputStream	bis;
//    StringBuffer sb = null;
    String cRic, cFiltri, cEsterni, parametro, campo, valore, separatore;

    query = "SELECT CAMPI_DI_RICERCA, FILTRI, nvl(FILTRI_ESTERNI,'N') "+
            "FROM BLOCCHI_POPUP "+
            "WHERE BLOCCO = :BLOCCO " +
            "AND AREA = :AREA";

    try {
      if (dbOpEsterna==null) {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      }
      else {
        dbOp = dbOpEsterna;
      }

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", area);
      dbOp.setParameter(":BLOCCO", blocco);
      dbOp.execute();
      rst = dbOp.getRstSet();
      if (rst.next()) {
        cRic = rst.getString(1);
        cFiltri = rst.getString(2);
        if (cRic == null) {
          cRic = "";
        }
        if (cFiltri == null) {
          cFiltri = "";
        }
        cEsterni = rst.getString(3);
        if (cEsterni.equalsIgnoreCase("N")) {
          if (cRic.length() != 0 || cFiltri.length() != 0) {
            ricerca = cRic +'@'+ cFiltri;
          } else {
            ricerca = cRic + cFiltri;
          }
        } else {
          query = "SELECT PARAMETRO, VALORE_CAMPO, VALORE_DEFAULT "+
                  "FROM PARAMETRI_POPUP "+
                  "WHERE BLOCCO = :BLOCCO " +
                  "AND AREA = :AREA";
          dbOp.setStatement(query);
          dbOp.setParameter(":AREA", area);
          dbOp.setParameter(":BLOCCO", blocco);
          dbOp.execute();
          rst = dbOp.getRstSet();
          separatore = "";
          ricerca = "";
          while (rst.next()) {
            parametro = rst.getString("PARAMETRO");
            campo = rst.getString(2);
            if (campo == null) {
              campo = "";
            }
            valore = rst.getString(3);
            if (valore == null) {
              valore = "";
            }
            ricerca += separatore + parametro;
            if (campo.length() != 0) {
              ricerca += "("+campo+")";
            } else {
              if (valore.length() != 0) {
                ricerca += "('"+valore+"')";
              }
            }
            separatore = "@";
          }
        }
      }

    } catch(Exception e) {

      logger.error("BloccoPopup::caricaInformazioniBlocco - Errore: "+e.toString());
    }
    finally {
      if (dbOpEsterna == null)  free(dbOp);
    }
    //Debug Tempo
    stampaTempo("BloccoPopup::caricaInformazioniBlocco - Fine",area,"",blocco,ptime);
    //Debug Tempo
  }

  public String getValue() {
    return getValue(null);
  }

  public String getValue(IDbOperationSQL dbOpEsterna) {
    if (settoreProtetto) {
      return "";
    }
    return bloccoWHtml;
  }
  public String getZValue() {
    return null;
  }

  public String getPRNValue() {
    return getPRNValue(null);
  }

  public String getPRNValue(IDbOperationSQL dbOpEsterna) {
    return "";
  }
  public String getPRNComValue() {
    return "";
  }

  public String getPRNComValue(IDbOperationSQL dbOpEsterna) {
    return "";
  }

  public  void release() {
  }

  public void settaListFields(String l_fields){
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

  public void settaProtetto(boolean b_protetto) {
    settoreProtetto = b_protetto;
  }
}