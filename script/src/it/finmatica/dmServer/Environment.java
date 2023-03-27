package it.finmatica.dmServer;

/*
 * GESTIONE DELLE VARIABILI DI AMBIENTE
 * SIA GLOBALI CHE A LIVELLO UTENTE
 *
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 *
 * */

import org.w3c.dom.*;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Vector;

import it.finmatica.jfc.dbUtil.*;
import it.finmatica.dmServer.util.*;
import it.finmatica.dmServer.mapping.*;

public class Environment {

    // Variabili pivate
    private String user;
    private String pwd = null;
    private String ente;
    private String library = null;
    private String applicativo;
    private String ruolo = null;
    private String gruppo = "";
    private String iniFile = null;
    private Document docSystem = null;
    private Node nodeMapping = null;
    private boolean byPassCompetenze = false;
    public Global Global;
    private GDMapping gdMapping;
    private boolean dbOpRestaConnessa = false;
    private int dbOpCreate = 0;
    private int dbOpDistrutte = 0;

    private IDbOperationSQL dbOpSql;

    // ***************** METODI DI INIZIALIZZAZIONE ***************** //

    /*
     * METHODS:      Constructor
     *
     * DESCRIPTION: Inizializza le variabili globali
     *              a partire dal file propertiese
     *
     * RETURN:      none
     */

    public Environment() throws Exception {
        try {
            Global = new Global();
            Global.settaGlobal(null);
        } catch (Exception e) {
            throw new Exception("Environment::settaGlobal()\n" + e.getMessage());
        }
    }

    public Environment(String u, String p, String appl, String ente, String lib, JNDIParameter jndi) throws Exception {
        Global = new Global();
        Global.initVar();
        Global.CONNECTION = null;
        Global.CLOSECONNECTION_EXTERN = true;

        Global.JNDIPARM = jndi;

        try {
            inizializza(u, p, appl, ente, lib, null);
        } catch (Exception e) {
            throw new Exception("Environment::Costructor - inizializza\n" + e.getMessage());
        }
    }

    public Environment(String u, String p, String appl, String ente, String lib, String ini) throws Exception {
        this(u, p, appl, ente, lib, ini, false);
    }

    public Environment(String u, String p, String appl, String ente, String lib, String ini, boolean usaIniPerTest)
        throws Exception {
        try {
            Global = new Global();
            Global.setUsaIniPerTest(usaIniPerTest);
            Global.settaGlobal(ini);
        } catch (Exception e) {
            throw new Exception("Environment::Costructor - settaGlobal(" + ini + ")\n" + e.getMessage());
        }

        try {

            Global.CONNECTION = null;
            Global.CLOSECONNECTION_EXTERN = true;

            inizializza(u, p, appl, ente, lib, ini);
        } catch (Exception e) {
            throw new Exception("Environment::Costructor - inizializza\n" + e.getMessage());
        }
    }

    public Environment(String u, String p, String appl, String ente, String lib, Connection cn) throws Exception {
        this(u, p, appl, ente, lib, cn, true);
    }

    public Environment(String u, String p, String appl, String ente, String lib, Connection cn, boolean closeCn)
        throws Exception {
        try {
            Global = new Global();
            Global.settaGlobal(null);
        } catch (Exception e) {
            throw new Exception("Environment::CostructorCn - settaGlobal(null)\n" + e.getMessage());
        }

        try {
            Global.CONNECTION = cn;
            Global.CLOSECONNECTION_EXTERN = closeCn;

            inizializza(u, p, appl, ente, lib, null);
        } catch (Exception e) {
            throw new Exception("Environment::CostructorCn - inizializza\n" + e.getMessage());
        }
    }

    public void setExternalConnection(Connection newCn) {
        Global.CONNECTION = newCn;
    }

    public void resetExternalConnection() {
        Global.CONNECTION = null;
    }

    public void inizializza() throws Exception {
        inizializza(null, null, null, null, null, null);
    }

    public GDMapping getGDMapping() {
        return gdMapping;
    }

    public void inizializza(String u, String p, String a, String e, String lib, String ini) throws Exception {

        user = u;
        pwd = p;

        if (e != null) {
            ente = e;
        } else {
            ente = Global.MAPPING_ENTE;
        }

        if (a != null) {
            applicativo = a;
        } else {
            applicativo = Global.MAPPING_APPL;
        }

        if (lib != null) {
            this.library = lib;
        } else {
            this.library = Global.MAPPING_LIBRARY;
        }

        iniFile = ini;

        //GESTIONE DEL SYSTEM
        calcolaDM();

        //GESTIONE DEL MAPPING
        gdMapping = new GDMapping(this);

        if (Global.DM.compareTo(Global.FINMATICA_DM) == 0) {
            (new ManageConnection(Global)).creaSessioneDb();
        }

        //LETTURA PARAMETRI DA DB
        LookUpDMTable lookUp = (new LookUpDMTable(this));

        String sUseIntermedia, sRebuildImmediate, sPrintQuery, sLogSQL, sPrintWarea, sPrintTreeView, sParamCompetenze, sParamDebug, queryTimeOut, qryServiceLimit;

        Vector<String> vPar = new Vector<String>();
        Vector<String> vTipoModello = new Vector<String>();
        HashMap<String, String> hRet;

        vPar.add("USE_INTERMEDIA");
        vTipoModello.add("@DMSERVER@");

        vPar.add("REBUILD_IMMEDIATE");
        vTipoModello.add("@DMSERVER@");

        vPar.add("PRINT_QUERY");
        vTipoModello.add("@DMSERVER@");

        vPar.add("LOG_SQL");
        vTipoModello.add("@DMSERVER@");

        vPar.add("PRINT_WAREA");
        vTipoModello.add("@DMSERVER@");

        vPar.add("PRINT_TREEVIEW");
        vTipoModello.add("@DMSERVER@");

        vPar.add("PARAM_COMPETENZE");
        vTipoModello.add("@STANDARD");

        vPar.add("DEBUG");
        vTipoModello.add("@STANDARD");

        vPar.add("TIMEOUT_QUERY");
        vTipoModello.add("@DMSERVER@");

        vPar.add("DIM_MAX_ALL_BYTE");
        vTipoModello.add("@DMSERVER@");

        vPar.add("URL_ORACLE");
        vTipoModello.add("@STANDARD");

        vPar.add("QRYSERVICE_LIMIT");
        vTipoModello.add("@DMSERVER@");

        hRet = lookUp.lookUpParametro(vPar, vTipoModello);

        sUseIntermedia = hRet.get("USE_INTERMEDIA");
        if (sUseIntermedia != null) {
            Global.USE_INTERMEDIA = sUseIntermedia;
        }

        sRebuildImmediate = hRet.get("REBUILD_IMMEDIATE");
        if (sRebuildImmediate != null) {
            Global.REBUILD_IMMEDIATE = sRebuildImmediate;
        }

        sPrintQuery = hRet.get("PRINT_QUERY");
        if (sPrintQuery != null) {
            Global.PRINT_QUERY = sPrintQuery;
        }

        sLogSQL = hRet.get("LOG_SQL");
        if (sLogSQL != null) {
            Global.LOG_SQL = sLogSQL;
        }

        sPrintWarea = hRet.get("PRINT_WAREA");
        if (sPrintWarea != null) {
            Global.PRINT_WAREA = sPrintWarea;
        }

        sPrintTreeView = hRet.get("PRINT_TREEVIEW");
        if (sPrintTreeView != null) {
            Global.PRINT_TREEVIEW = sPrintTreeView;
        }

        sParamCompetenze = hRet.get("PARAM_COMPETENZE");
        if (sParamCompetenze != null) {
            Global.PARAM_COMPETENZE = sParamCompetenze;
        }

        sParamDebug = hRet.get("DEBUG");
        if (sParamDebug != null) {
            Global.PARAM_DEBUG = sParamDebug;
        }

        queryTimeOut = hRet.get("TIMEOUT_QUERY");
        if (queryTimeOut != null) {
            Global.QUERY_TIMEOUT = Integer.parseInt(queryTimeOut) * 1000;
        }

        qryServiceLimit = hRet.get("QRYSERVICE_LIMIT");
        if (qryServiceLimit != null) {
            Global.QRYSERVICE_LIMIT = Integer.parseInt(qryServiceLimit);
        }

        if (hRet.get("DIM_MAX_ALL_BYTE") != null) {
            try {
                Global.DIM_MAX_ALL_BYTE = Long.parseLong(hRet.get("DIM_MAX_ALL_BYTE"));
            } catch (Exception ei) {
                Global.DIM_MAX_ALL_BYTE = -1;
            }
        }

        String sUrlOracle;
        sUrlOracle = hRet.get("URL_ORACLE");
        if (sUrlOracle != null) {
            Global.URL_ORACLE_PARAM = sUrlOracle;
        }

        //FINE LETTURA PARAMETRI DA DB
    }

    // ****************** GESTIONE DELLE CONNESSIONI ********** //

    public void connect() throws Exception {
        if (!dbOpRestaConnessa || dbOpSql == null) {
            dbOpSql = (new ManageConnection(Global)).connectToDB();
           // System.out.println("connect->" + dbOpSql);
            dbOpCreate++;
            //System.out.println("dbOpCreate->" + dbOpCreate);
        }
    }

    public void createSavePoint() throws Exception {
        (new ManageConnection(Global)).createSavePoint(dbOpSql);
    }

    public void rollbackToSavePoint() throws Exception {
        (new ManageConnection(Global)).rollbackToSavePoint(dbOpSql);
    }

    public void disconnectClose() throws Exception {

       // System.out.println("close->" + dbOpSql);

        if (!dbOpRestaConnessa) {
            if (dbOpSql != null) {
                dbOpDistrutte++;
                //System.out.println("dbOpDistrutte->" + dbOpDistrutte);
            }
            (new ManageConnection(Global)).disconnectFromDB(dbOpSql, true, false);
            dbOpSql = null;
        }
    }

    public void disconnectRollback() throws Exception {

        //System.out.println("close rollback->" + dbOpSql);

        if (dbOpRestaConnessa) {
            if (Global.CONNECTION == null) {
                dbOpSql.rollback();
            }
        } else {
            if (dbOpSql != null) {
                dbOpDistrutte++;
               // System.out.println("dbOpDistrutte->" + dbOpDistrutte);
            }
            (new ManageConnection(Global)).disconnectFromDB(dbOpSql, false, false);
            dbOpSql = null;
        }
    }

    public void disconnectCommit() throws Exception {

        //System.out.println("close commit->" + dbOpSql);

        if (dbOpRestaConnessa) {
            if (Global.CONNECTION == null) {
                dbOpSql.commit();
            }
        } else {
            if (dbOpSql != null) {
                dbOpDistrutte++;
               // System.out.println("dbOpDistrutte->" + dbOpDistrutte);
            }
            (new ManageConnection(Global)).disconnectFromDB(dbOpSql, false, true);
            dbOpSql = null;
        }
    }

    public IDbOperationSQL getDbOp() {
        return dbOpSql;
    }

    public void setDbOp(IDbOperationSQL db) {
        dbOpSql = db;
    }

    // ***************** METODI DI SET E GET ***************** //

    public void settaGlobal() throws Exception {
        try {
            Global.settaGlobal(iniFile);
        } catch (Exception e) {
            throw new Exception("Environment::settaGlobal(" + iniFile + ")\n" + e.getMessage());
        }
    }

    public void setUser(String u) {
        user = u;
    }

    public String getUser() {
        return user;
    }

    public void setGruppo(String g) {
        gruppo = g;
    }

    public String getGruppo() {
        return gruppo;
    }

    public void byPassCompetenzeON() {
        byPassCompetenze = true;
    }

    public void byPassCompetenzeOFF() {
        byPassCompetenze = false;
    }

    public boolean getByPassCompetenze() {
        return byPassCompetenze;
    }

    public void setPwd(String p) {
        pwd = p;
    }

    public String getPwd() {
        return pwd;
    }

    public void setEnte(String e) {
        ente = e;
    }

    public String getEnte() {
        return ente;
    }

    public void setApplicativo(String a) {
        applicativo = a;
    }

    public String getApplicativo() {
        return applicativo;
    }

    public String getDM() {
        return Global.DM;
    }

    public String getLibrary() {
        return library;
    }

    public void setRuolo(String r) {
        ruolo = r;
    }

    public String getRuolo() {
        if (ruolo == null) {
            ruolo = verificaRuolo();
        }

        return ruolo;
    }

    public void setIniFile(String i) {
        iniFile = i;
    }

    public String getIniFile() {
        return iniFile;
    }

    public Document getSystemFile() {
        return docSystem;
    }

    public Node getNodeMapping() {
        return nodeMapping;
    }

    private String verificaRuolo() //da implementare
    {
        return null;
    }

    public boolean isDbOpRestaConnessa() {
        return dbOpRestaConnessa;
    }

    public void setDbOpRestaConnessa(boolean dbOpRestaConnessa) {
        this.dbOpRestaConnessa = dbOpRestaConnessa;
    }

    private void calcolaDM() throws Exception {
        //Ho passato INI
        if (Global.CONNECTION == null && iniFile != null) {
            //Se c'è il file GDSYSTEM.XML mappato sull'INI
            //cerco il DM nella riga XML
            //(se non c'è il file lascio DM preso dal properties)
            if (!Global.MSYSTEM_PATH.equals("@")) {
                try {
                    XMLSystem xml = new XMLSystem(Global.MSYSTEM_PATH, ente, applicativo);

                    Global.DM = xml.getDM();
                } catch (Exception e) {
                    throw new Exception("Environment::calcolaDM()\n" + e.getMessage());
                }
            }
        }
        //Ho passato la CN
        else {
            //Il DM è sicuro FINMATICA
            Global.DM = Global.FINMATICA_DM;
        }
    }

    public boolean dbOpTutteChiuse() {
        return (dbOpCreate == dbOpDistrutte);
    }
}
         