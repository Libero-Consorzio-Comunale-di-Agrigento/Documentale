package it.finmatica.modutils.multirecord;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.sql.*;
import java.io.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modutils.multirecord.Multirecord;
import it.finmatica.modutils.informazioniblocco.InformazioniBlocco;
import org.apache.log4j.Logger;

public class BlocchiNestedMonorecord {
  protected String blocco = "";
  protected String nomeBlocco = "";
  protected String area = "";
  protected String codMod = "";
  protected String inifile = "";
  protected String tipo = "";
  protected String rw = "";  
  protected Multirecord mr = null;
  protected LinkedList listaDati = null;
  protected int riga = 0;
  protected HttpServletRequest sRequest;
  private Connection pConn;
  private  static Logger logger = Logger.getLogger(BlocchiNestedMonorecord.class);
  private boolean debuglog = logger.isDebugEnabled();

  public BlocchiNestedMonorecord(String pArea, 
                                 String pModello, 
                                 String pBlocco,
                                 LinkedList pListaDati, 
                                 Connection p_Conn) throws Exception {
    
    //Debug Tempo
    long ptime = stampaTempo("BlocchiNestedMonorecord - Inizio",pArea,pModello,pBlocco,0);
    //Debug Tempo
    area = pArea;  
    listaDati = pListaDati;
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

    InformazioniBlocco infoBlk = new InformazioniBlocco(pBlocco);
    nomeBlocco = infoBlk.getBlocco();
    String areaBlocco = infoBlk.getAreaBlocco();
    if ((areaBlocco == null) || (areaBlocco.equalsIgnoreCase(""))){
      areaBlocco = pArea;
    } else {
      area = areaBlocco;
    }

    leg       = infoBlk.getLegame();
    ord       = "";
    nav       = Integer.parseInt(infoBlk.getNumeroRecord());
    agg       = "N";

    blocco = pBlocco;
//    if (nomeServ == null) {
//      nomeServ = "";
//    } else {
//      if (!pdo.equalsIgnoreCase("")) {
//        nomeServ += ".do";
//      }
//    }

    rw = "R";
  
    //Debug Tempo
    stampaTempo("BlocchiNestedMonorecord - Fine",pArea,pModello,pBlocco,ptime);
    //Debug Tempo
  }

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

}
