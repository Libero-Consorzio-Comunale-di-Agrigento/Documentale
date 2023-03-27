package it.finmatica.modulistica;

import javax.servlet.*;
import javax.servlet.http.*;

import java.awt.Color;
import java.io.*;
import java.sql.*;
import java.util.*;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.io.LetturaScritturaFileFS;
import it.finmatica.modulistica.allegati.FormatiFile;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.dmServer.management.*;
//import it.finmatica.dmServer.motoreRicerca.ParametriQuery;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.JNDIParameter;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.GD4_Oggetti_File;
import org.apache.log4j.Logger;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.SimpleBookmark;
import it.finmatica.jsign.verify.result.CriticityLevel;
import it.finmatica.jsign.api.SignedObjectReaderFactory;
import it.finmatica.jsign.api.SignedObjectReaderI;
import it.finmatica.jsign.verify.result.VerifyResult;

public class Visualizza {

    private String inifile = null;
    private String path = null;
    private String completeContextURL = null;
    private String serverScheme;
    private String serverName;
    private String serverPort;
    private Environment vu;
    private String p_user;
    private String p_ruolo;
    private String corpoHtml;
    private boolean errore;
    private String id_tipodoc;
    private String noBody = Global.NOBODY;
    private String pie_pagina = "N";
    private int border = 40;
    private int bottom = 40;

    private static Logger logger = Logger.getLogger(Visualizza.class);

    public Visualizza(String sPath) {
        init(sPath);
    }

    public void init(String sPath) {
        // super.init(config);
        try {
            String separa = File.separator;
            inifile = sPath + separa + "config" + separa + "gd4dm.properties";
            File f = new File(inifile);
            if (!f.exists()) {
                inifile = sPath + separa + ".." + separa + "jgdm" + separa + "config" + separa + "gd4dm.properties";
            }
            // FileInputStream fis = new FileInputStream(inifile);
            // confLogger = new Properties();
            // confLogger.load(fis);
            // String esiste
            // =confLogger.getProperty("log4j.logger.it.finmatica.modulistica");
            // if (esiste == null) {
            // esiste = "";
            // }
            // if (esiste == "") {
            // confLogger.setProperty("log4j.logger.it.finmatica.modulistica","ERROR,
            // S");
            // confLogger.setProperty("log4j.appender.S","org.apache.log4j.RollingFileAppender");
            // confLogger.setProperty("log4j.appender.S.File","${catalina.home}/logs/jgdm.log");
            // confLogger.setProperty("log4j.appender.S.MaxFileSize","10MB");
            // confLogger.setProperty("log4j.appender.S.MaxBackupIndex","10");
            // confLogger.setProperty("log4j.appender.S.layout","org.apache.log4j.PatternLayout");
            // confLogger.setProperty("log4j.appender.S.layout.ConversionPattern","%d{HH:mm:ss}
            // [%p] %c: %m%n");
            // }
            // PropertyConfigurator.configure(confLogger);

            // Lettura parametri da file ini
            Parametri.leggiParametriStandard(inifile);

            // Creazione alias
            SessioneDb.getInstance().addAlias(Parametri.ALIAS, Parametri.SPORTELLO_DRIVER);
        } catch (Exception e) {
            logger.error("Visualizza::init() - Attenzione! si è verificato un errore: " + e.toString());
        }
    }

    public void genera(HttpServletRequest request, HttpServletResponse response, String pdo) {
        HTML.Template tmpl = null;
        AccediDocumento ad = null;
        InputStream bis = null;
        String pt = "";
        if (pdo.length() == 0) {
            pt = request.getSession().getServletContext().getRealPath("") + File.separator + "template";
        } else {
            pt = request.getSession().getServletContext().getRealPath("") + File.separator + "../"
                + Parametri.APPLICATIVO + "/template";
        }
        String[] paths = {pt, "."};
        String fileName = "ServletFirma.tmpl";
        HttpSession session = request.getSession();
        String ar = request.getParameter("ar");
        String cm = request.getParameter("cm");
        String cr = request.getParameter("cr");
        String ca = request.getParameter("ca");
        String iddoc = request.getParameter("iddoc");
        String scelta = request.getParameter("scelta");
        String firma = request.getParameter("firma");
        String allNum = request.getParameter("allNum");
        String docVer = request.getParameter("docVer");
        String nome = "";
        String iddocs = request.getParameter("iddocs");
        int nAllNum;
        long ndocVer = 0;
        boolean ie = false;

        String useragent = request.getHeader("User-Agent");
        String user = useragent.toLowerCase();
        if (user.indexOf("msie") != -1) {
            ie = true;
        }

        errore = false;
        if (allNum == null) {
            allNum = "1";
        }
        nAllNum = Integer.parseInt(allNum);

        if (docVer == null) {
            docVer = "";
        } else {
            ndocVer = Integer.parseInt(docVer);
        }
        if (firma == null) {
            firma = "";
        }
        if (scelta == null) {
            scelta = "";
        }

        if (iddocs == null) {
            iddocs = "";
        }

        if (Parametri.ALLEGATI_SINGLE_SIGN_ON.equalsIgnoreCase("S")) {
            p_user = "";
        } else {
            p_user = request.getParameter("us");
            if (p_user == null) {
                p_user = "";
            }
        }
        if (p_user.length() == 0) {
            String nominativo = request.getRemoteUser();
            if (nominativo == null) {
                p_user = (String) session.getAttribute("Utente");
            } else {
                p_user = cercaUtente(nominativo.toUpperCase());
            }

            if (p_user == null) {
                p_user = "";
            }
            if (p_user.length() == 0) {
                p_user = creaUtente(request.getSession());
            }
            p_user = p_user.toUpperCase();

            p_ruolo = caricaRuolo(p_user);
        } else {
            p_ruolo = request.getParameter("ruolo");
        }



        try {
            initVu(p_user, p_ruolo);
            try {
                vu.connect();
                vu.setDbOpRestaConnessa(true); //Questo evita che ogni volta che passo la vu alle classi del dmserver, es. AggiornaDocumento
                                               //la classe rifaccia connect e/o disconnect.... a quello ci penso io
            }
            catch (Exception e) {
                    corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Errore in fase connessione. Errore:  ["+ e.toString()+"].</span>";
                    logger.error("ServletVisualizza::genera() Errore in connessione enviroment - Errore:  ["+ e.toString()+"]",e);
                    return;
            }

            serverScheme = request.getScheme();

            // -------------------------------------------------
            // Inizializzo il completeContextURL che è del tipo:
            // http://hostName:port/Sportello/
            // -------------------------------------------------
            if (Parametri.PROTOCOLLO.length() == 0) {
                serverScheme = request.getScheme();
            } else {
                serverScheme = Parametri.PROTOCOLLO;
            }
            if (Parametri.SERVERNAME.length() == 0) {
                serverName = request.getServerName();
            } else {
                serverName = Parametri.SERVERNAME;
            }
            if (Parametri.SERVERPORT.length() == 0) {
                serverPort = "" + request.getServerPort();
            } else {
                serverPort = Parametri.SERVERPORT;
            }

            if (completeContextURL == null) {
                completeContextURL = serverScheme + "://" + serverName + ":" + serverPort + request.getContextPath() + "/";
            }

            if (iddocs.length() > 0) {
                // Chiamo il copia conforme multiplo
                copiaConformeMulitplo(iddocs, response);
                return;
            }
            IDbOperationSQL dbOp = null;
            ResultSet rst = null;

            if (cm == null) {
                cm = "";
            }

            if (iddoc == null) {
                iddoc = "";
            }
            if (iddoc.length() == 0) {
                id_tipodoc = ricavaIdtipodoc(ar, cm);
                iddoc = ricercaIdDocumento(request);
            }

            if (iddoc == null) {
                iddoc = "";
            }
            ServletOutputStream sosStream = null;
            String formato = "";
            String formatoOggettoFile = "";

            if (iddoc.length() != 0) {
                try {
                    if (docVer.equalsIgnoreCase("")) {
                        ad = new AccediDocumento(iddoc, vu);
                        ad.accediDocumentoAllegati();
                        //ad.connect();
                        if (ca == null) {
                            int numAll = ad.listaIdOggettiFile().size();
                            if (numAll == 0) {
                                return;
                            }
                            int numVis = 0;
                            for (int i = 0; i < numAll; i++) {
                                if (ad.isOggettoFileVisibile(ad.listaIdOggettiFile().get(i).toString())) {
                                    numVis++;
                                    if (numVis == nAllNum) {
                                        ca = ad.listaIdOggettiFile().get(i).toString();
                                        formato = ad.nomeFormatoOggettoFile(ca);
                                    }
                                }
                            }
                            if (ca == null) {
                                return;
                            }
                        }
                        formatoOggettoFile = ad.nomeFormatoOggettoFile(ca);
                        // formato = ad.nomeFormatoOggettoFile(ca);

                        try {
                            bis = ad.leggiOggettoFile(ca);
                            nome = ad.nomeOggettoFile(ca);
                        } catch (Exception letturaOF) {
                            logger.info("File con ca:" + ca + " non trovato, forse ancora in tabella temporanea.");
                            bis = null;
                            nome = null;
                        }
                    } else {
                        dbOp = vu.getDbOp();// SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME, 0);
                        ProfiloVersion pv = new ProfiloVersion(iddoc, ndocVer);
                        pv.initVarEnv(p_user, p_ruolo, dbOp.getConn());
                        if (pv.accedi().booleanValue()) {
                            Vector<GD4_Oggetti_File> v = pv.getListaFile();
                            for (int i = 0; i < v.size(); i++) {
                                GD4_Oggetti_File obj = v.get(i);
                                if (ca.equalsIgnoreCase(obj.getIdOggettoFile())) {
                                    nome = obj.getFileName();
                                    bis = pv.getFileStream(nome);
                                }
                            }
                        } else {
                            logger.error("Visualizza - Errore in accedi pv: " + pv.getError());
                            errore = true;
                            return;
                        }
                    }
                } catch (Exception e) {
                    try {
                       // ad.disconnect();
                    } catch (Exception e1) {
                    }
                    if (Parametri.DEBUG.equalsIgnoreCase("0")) {
                        corpoHtml = "Attenzione si è verificato un Errore.";
                    } else {
                        corpoHtml = e.toString();
                    }
                    e.printStackTrace();
                    logger.error("Visualizza - Errore in accedidoc: " + e.toString());
                    errore = true;
                    return;
                }
            }

            if (nome == null) {
                nome = "";
            }
            SignedObjectReaderI allFirm = null;
            if (firma.length() == 0 && formatoFile(nome).equalsIgnoreCase("PDF") && scelta.length() == 0) {
                try {
                    InputStream bis2 = new BufferedInputStream(bis);
                    boolean isSigned = false;
                    boolean isPDFerror = false;
                    try {
                        allFirm = SignedObjectReaderFactory.getSignedObjectReader(bis);
                        isSigned = allFirm.isSigned();
                    } catch (Exception eSigned) {
                        if (formatoFile(nome).equalsIgnoreCase("PDF")) {
                            logger.error("Visualizza - Errore in pdf: " + eSigned.toString());
                            isSigned = false;
                            isPDFerror = true;
                        } else {
                            throw eSigned;
                        }
                    }
                    if (isSigned) {
                        firma = "S";
                    } else {
                        if (!isPDFerror) {
                            bis = allFirm.getOriginalContent();
    //						bis = (ByteArrayInputStream) allFirm.getOriginalContent();
    //						bis.reset();
                        } else {
                            //ad.disconnect();
                            ad = new AccediDocumento(iddoc, vu);
                            ad.accediDocumentoAllegati();
                           // ad.connect();
                            bis = ad.leggiOggettoFile(ca);
                        }
                    }
                } catch (Exception e) {
                    try {
                       // ad.disconnect();
                    } catch (Exception e1) {
                    }
                    if (Parametri.DEBUG.equalsIgnoreCase("0")) {
                        corpoHtml = "Attenzione si è verificato un errore.";
                    } else {
                        corpoHtml = e.toString();
                    }
                    e.printStackTrace();
                    logger.error("Visualizza - Errore in firma: " + e.toString());
                    errore = true;
                    return;
                }
            } else {
                if (firma.length() == 0 && formatoFile(nome).equalsIgnoreCase("PDF") && scelta.length() != 0) {
                    firma = "S";
                }
            }
            if (firma.length() != 0) {
                if (scelta.length() == 0 || scelta.equalsIgnoreCase("1")) {
                    Hashtable tmpl_args = new Hashtable();
                    tmpl_args.put("filename", fileName);
                    tmpl_args.put("path", paths);

                    try {
                        tmpl = new HTML.Template(tmpl_args);
                    } catch (Exception e) {
                        logger.error("Visualizza::genera() - Attenzione! Si è verificato un errore: " + e.toString());
                    }
                    tmpl.setParam("varea", ar);
                    tmpl.setParam("codModello", cm);
                    tmpl.setParam("codRichiesta", cr);
                    tmpl.setParam("idOgg", ca);
                    tmpl.setParam("idDoc", iddoc);
                    if (pdo.equalsIgnoreCase("CC")) {
                        tmpl.setParam("header", "0");
                        tmpl.setParam("wmsg", "../../" + Parametri.APPLICATIVO + "/AmvMessaggi.html");
                    } else {
                        tmpl.setParam("header", "1");
                        tmpl.setParam("wmsg", "AmvMessaggi.html");
                    }
                    if (nome.toUpperCase().indexOf(".PDF") > -1) {
                        tmpl.setParam("copia", "1");
                    } else {
                        tmpl.setParam("copia", "0");
                    }
                    if (scelta.equalsIgnoreCase("1")) {
                        try {
                            if (allFirm == null) {
                                allFirm = SignedObjectReaderFactory.getSignedObjectReader(bis);
                            }

                            boolean firma_http_crl = true;
                            if (Parametri.FIRMA_HTTP_CRL.equalsIgnoreCase("N")) {
                                firma_http_crl = false;
                            }
                            VerifyResult vr = allFirm.verify(firma_http_crl);

                            String result = "";
                            boolean isValid = false;
                            if (Parametri.JSIGN_LEVEL.equalsIgnoreCase("HIGH")) {
                                isValid = vr.isValid(CriticityLevel.HIGH);
                            }
                            if (Parametri.JSIGN_LEVEL.equalsIgnoreCase("MEDIUM")) {
                                isValid = vr.isValid(CriticityLevel.MEDIUM);
                            }
                            if (Parametri.JSIGN_LEVEL.equalsIgnoreCase("LOW")) {
                                isValid = vr.isValid(CriticityLevel.LOW);
                            }

                            if (isValid) {
                                result = "Firma/e valida/e.<br/>";
                            } else {
                                result = "Firma/e non valida/e.<br/>";
                            }
                            result += vr.toString();
                            tmpl.setParam("messaggio", result);
                        } catch (Exception e) {
                            try {
                               // ad.disconnect();
                            } catch (Exception e1) {
                            }
                            if (Parametri.DEBUG.equalsIgnoreCase("0")) {
                                corpoHtml = "Attenzione si è verificato un errore.";
                            } else {
                                corpoHtml = e.toString();
                            }
                            e.printStackTrace();
                            logger.error("Visualizza - Errore fine firma: " + e.toString());
                            errore = true;
                            return;
                        }
                    }
                    try {
                        //ad.disconnect();
                    } catch (Exception e1) {
                    }
                    corpoHtml = tmpl.output();
                    errore = true;
                    return;
                }
            }
            if (firma.length() == 0 || scelta.length() != 0) {
                if (iddoc.length() != 0 && nome.length() > 0) {
                    try {
                        if (scelta.equalsIgnoreCase("3")) {
                            if (allFirm == null) {
                                allFirm = SignedObjectReaderFactory.getSignedObjectReader(bis);
                            }
                            // PKCS7Reader reader = new PKCS7Reader(bis);
                            bis = allFirm.getOriginalContent();
    //						bis.reset();
                            nome = nome.replaceAll(".p7m", "");
                            nome = nome.replaceAll(".P7M", "");
                        }
                        if (scelta.equalsIgnoreCase("4")) {
                            nome = nome.replaceAll(".p7m", "");
                            nome = nome.replaceAll(".P7M", "");
                            formato = "PDF";
                        }
                        if (formato.length() == 0) {
                            formato = request.getParameter("formato");
                            if (formato == null) {
                                formato = "";
                            } else {
                                nome = "doc." + formato;
                            }
                        }
                        if (formato.length() == 0) {
                            formato = formatoFile(nome);
                            if (formato.length() == 0) {
                                formato = formatoOggettoFile;
                                nome = nome + "." + formato;
                            }
                            FormatiFile ff = new FormatiFile();
                            String fm = ff.getFileMime(formato);
                            if (fm.length() > 0 && Parametri.ALLEGATI_AUTO_OPEN.equalsIgnoreCase("S") && !ie) {
                                response.setContentType(ff.getFileMime(formato));
                                response.setHeader("Content-Disposition", "inline;filename=\"" + nome + "\"");
                            } else {
                                response.setHeader("Content-Disposition", "attachment;filename=\"" + nome + "\"");
                            }
                        } else {
                            response.setContentType("application/" + formato.toLowerCase());
                            response.setHeader("Content-Disposition", "inline;filename=\"" + nome + "\"");
                        }
                        sosStream = response.getOutputStream();
                        if (scelta.equalsIgnoreCase("4")) {
                            if (allFirm == null) {
                                allFirm = SignedObjectReaderFactory.getSignedObjectReader(bis);
                            }
                            InputStream data;
                            data = allFirm.getOriginalContent();
                            PdfReader pdfreader = new PdfReader(data);
                            int n = pdfreader.getNumberOfPages();
                            PdfStamper stamp = new PdfStamper(pdfreader, sosStream);
                            int i = 0;
                            PdfContentByte over;
                            int fontSize = 8;
                            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);
                            while (i < n) {
                                i++;
                                over = stamp.getOverContent(i);
                                over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_STROKE);
                                over.beginText();
                                over.setColorStroke(Color.GRAY);
                                if (Parametri.COPIA_CONF_DIAGONALE.equalsIgnoreCase("S")) {
                                    over.setFontAndSize(bf, 42);
                                    over.showTextAligned(Element.ALIGN_CENTER, Parametri.COPIA_CONF_TESTO,
                                        pdfreader.getPageSize(i).getWidth() / 2,
                                        pdfreader.getPageSize(i).getHeight() / 2, 60);
                                }
                                if (Parametri.COPIA_CONF_ALTO_SX.equalsIgnoreCase("S")) {
                                    over.setFontAndSize(bf, 24);
                                    over.showTextAligned(Element.ALIGN_LEFT, Parametri.COPIA_CONF_TESTO, 30,
                                        pdfreader.getPageSize(i).getHeight() - 30, 0);
                                }
                                if (Parametri.COPIA_CONF_BASSO_SX.equalsIgnoreCase("S")) {
                                    over.setFontAndSize(bf, 24);
                                    over.showTextAligned(Element.ALIGN_LEFT, Parametri.COPIA_CONF_TESTO, 30, 30, 0);
                                }
                                if (Parametri.COPIA_CONF_BASSO_DX.equalsIgnoreCase("S")) {
                                    over.setFontAndSize(bf, 24);
                                    over.showTextAligned(Element.ALIGN_RIGHT, Parametri.COPIA_CONF_TESTO,
                                        pdfreader.getPageSize(i).getWidth() - 30, 30, 0);
                                }
                                if (Parametri.COPIA_CONF_ALTO_DX.equalsIgnoreCase("S")) {
                                    over.setFontAndSize(bf, 24);
                                    over.showTextAligned(Element.ALIGN_RIGHT, Parametri.COPIA_CONF_TESTO,
                                        pdfreader.getPageSize(i).getWidth() - 30,
                                        pdfreader.getPageSize(i).getHeight() - 30, 0);
                                }
    //							if (Parametri.COPIA_CONF_PIE_PAG.equalsIgnoreCase("S")) {
                                getPiePagina();
                                if (pie_pagina.equalsIgnoreCase("S")) {
                                    BaseFont bf2 = BaseFont
                                        .createFont(BaseFont.COURIER_BOLDOBLIQUE, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                                    over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
                                    over.setFontAndSize(bf2, 8);
                                    Font f = new Font(bf2);
                                    f.setSize(8);
                                    ColumnText ct = new ColumnText(over);
                                    Phrase myText = new Phrase(Parametri.COPIA_CONF_TESTO, f);
                                    int altezza = (int) Math
                                        .ceil(ct.getWidth(myText) / pdfreader.getPageSize(i).getWidth());
                                    altezza = (altezza * 12) + bottom;
                                    ct.setSimpleColumn(myText, border, bottom, pdfreader.getPageSize(i).getWidth() - border,
                                        altezza, 10,
                                        Element.ALIGN_LEFT);
                                    ct.go();
                                }
                                over.endText();
                            }
                            stamp.close();

                            return;
                        } else {
                            int ibit = bis.read();
                            while ((ibit) >= 0) {
                                sosStream.write(ibit);
                                ibit = bis.read();
                            }
                            sosStream.flush();
                            sosStream.close();
                            bis.close();
                        }
                        try {
                           // ad.disconnect();
                        } catch (Exception e1) {
                        }
                    } catch (Exception e) {
                        try {
                           // ad.disconnect();
                        } catch (Exception e1) {
                        }
                        if (Parametri.DEBUG.equalsIgnoreCase("0")) {
                            corpoHtml = "Attenzione si è verificato un errore.";
                        } else {
                            corpoHtml = e.toString();
                        }
                        e.printStackTrace();
                        logger.error("Visualizza - Errore in scrittura finale file su response: " + e.toString());
                        errore = true;
                        return;
                    }
                } else {
                    nome = ca;
                    String query;

                    query =
                        "SELECT ALLEGATO,  nvl(dbms_lob.getlength(ALLEGATO),0) dimallegato,nvl(percorso,'') perc   FROM ALLEGATI_TEMP,ALLEGATI_TEMP_PERCORSI "
                            + "WHERE ALLEGATI_TEMP.AREA = :AREA AND "
                            + "ALLEGATI_TEMP.CODICE_RICHIESTA = :CR AND " + "ALLEGATI_TEMP.CODICE_MODELLO = :CM AND "
                            + "ALLEGATI_TEMP.NOMEFILE = :NF AND " +
                            " ALLEGATI_TEMP.AREA=ALLEGATI_TEMP_PERCORSI.AREA (+) AND " +
                            " ALLEGATI_TEMP.CODICE_RICHIESTA=ALLEGATI_TEMP_PERCORSI.CODICE_RICHIESTA (+) AND " +
                            " ALLEGATI_TEMP.CODICE_MODELLO=ALLEGATI_TEMP_PERCORSI.CODICE_MODELLO (+) AND " +
                            " ALLEGATI_TEMP.NOMEFILE=ALLEGATI_TEMP_PERCORSI.NOMEFILE (+) ";

                    try {
                        dbOp = vu.getDbOp();
                        dbOp.setStatement(query);
                        dbOp.setParameter(":AREA", ar);
                        dbOp.setParameter(":CM", cm);
                        dbOp.setParameter(":CR", cr);
                        dbOp.setParameter(":NF", nome);
                        dbOp.execute();
                        rst = dbOp.getRstSet();
                        if (rst.next()) {
                            InputStream srcBlob = null;
                            if (rst.getLong(2) > 0) {
                                srcBlob = dbOp.readBlob("ALLEGATO");
                            } else {
                                String pathFS = Global.nvl(rst.getString(3), "");
                                if (pathFS.equals("")) {
                                    throw new Exception(
                                        "Attenzione! il blob dell'allegati_temp è vuoto e anche il percorso su FS non è presente!");
                                }

                                try {
                                    LetturaScritturaFileFS fs = new LetturaScritturaFileFS(pathFS);

                                    srcBlob = fs.leggiFile();
                                } catch (Exception e) {
                                    throw new Exception(
                                        "Attenzione! Impossibile leggere l'allegato_temp dal path " + pathFS);
                                }
                            }
                            response.setHeader("Content-Disposition", "attachment;filename=\"" + nome + "\"");
                            sosStream = response.getOutputStream();
                            int ibit = srcBlob.read();
                            while ((ibit) >= 0) {
                                sosStream.write(ibit);
                                ibit = srcBlob.read();
                            }
                            sosStream.flush();
                            sosStream.close();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();

                        logger.error("Visualizza - Errore da allegati temp: " + e.toString());
                    }
                }

                path = completeContextURL + "visualizza/" + cr + "/" + nome;
                corpoHtml = "<html>";
                corpoHtml += "<head><title>ServletVisualizza</title>";
                corpoHtml += "<meta http-equiv='refresh' content='0; URL=" + path + "'>";
                corpoHtml += "</head><body>";
                corpoHtml += "</body></html>";
            }
        }
        finally {
            //Chiusura della connessione
            try {
                vu.setDbOpRestaConnessa(false);
                vu.disconnectClose();
            }
            catch (Exception e) {
            }
        }

        errore = false;
        return;
    }

    private void initVu(String p_user, String p_ruolo) {
        try {
            vu = new Environment(p_user, null, "MODULISTICA", "ADS", null, (new JNDIParameter("jdbc/gdm")));
            vu.setRuolo(p_ruolo);
        } catch (Exception e) {
            logger.error("Upload::initVu - " + e.toString());
        }
    }

    /**
     *
     */
    private void free(IDbOperationSQL dbOp) {
        try {
            dbOp.close();
        } catch (Exception e) {
        }
    }

    /**
     *
     */
    public boolean getCreaCorpo() {
        return errore;
    }

    /**
     *
     */
    public String getValue() {
        return corpoHtml;
    }

    /**
     *
     */
    protected void freeConn() {
        // try {
        // SessioneDb.getInstance().closeFreeConnection();
        // } catch (Exception e) {
        // logger.error("ServletEditing::freeConn() - Attenzione! Errore in fase
        // di rilascio connnessioni: "+e.toString());
        // corpoHtml += "<H2>Attenzione! Errore in fase di rilascio
        // connnessioni.</h2>";
        // if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        // corpoHtml += e.toString();
        // }
        // if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        // corpoHtml += e.getStackTrace().toString();
        // }
        // }
    }

    /**
     *
     */
    private String cercaUtente(String nominativo) {
        String retval = null;
        IDbOperationSQL dbOp = null;
        ResultSet rst = null;
        String query;

        // RICAVO IL RUOLO DELL'UTENTE
        query = "select utente from ad4_utenti where nominativo = :NOMINATIVO";
        try {
            dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);

            dbOp.setStatement(query);
            dbOp.setParameter(":NOMINATIVO", nominativo);
            dbOp.execute();
            rst = dbOp.getRstSet();

            if (rst.next()) {
                retval = rst.getString(1);
            }
            free(dbOp);
            return retval;
        } catch (Exception e) {
            logger.error("ServletModulistica::cercaUtente - " + e.toString(), e);
            free(dbOp);
            return "";
        }
    }

    /**
     *
     */
    private String creaUtente(HttpSession httpSess) {
        String ret = "GUEST";
        // httpSess.setAttribute("UtenteGDM",ret);
        // httpSess.setAttribute("RuoloGDM",ret);
        return ret;
    }

    /**
     *
     */
    private String caricaRuolo(String p_user) {
        return "GDM";
    }

    private String ricercaIdDocumento(HttpServletRequest request) {
        Vector ll;
        String iddoc = null;
        String // ar,
            cr/*
         * , cm
         */;

        cr = request.getParameter("cr");

        try {
            RicercaDocumento rd = new RicercaDocumento(id_tipodoc, vu);
            rd.settaFileName(noBody);
            rd.settaCodiceRichiesta(cr);
            ll = rd.ricercaBozza();
            if (ll.size() == 1) {
                iddoc = (String) ll.firstElement();
            } else {
                if (ll.size() > 1) {
                    logger.error("ServletVisualizza::ricercaIdDocumento() - Errore:  Nel numero documenti trovati!!");
                }
            }
        } catch (Exception e) {
            logger.error("ServletVisualizza::ricercaIdDocumento() - Errore:  [" + e.toString() + "]");
        }
        return iddoc;
    }

    /**
     *
     */
    private String ricavaIdtipodoc(String ar, String cm) {
        IDbOperationSQL dbOp = null;
        ResultSet rst = null;
        String query;
        String idtipodoc = null;
        String codmod = null;

        query = "SELECT ID_TIPODOC, CODICE_MODELLO_PADRE " + " FROM MODELLI" + " WHERE AREA = :AREA"
            + "   AND CODICE_MODELLO = :CM";
        try {
            dbOp = vu.getDbOp();//SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME, 0);

            dbOp.setStatement(query);
            dbOp.setParameter(":AREA", ar);
            dbOp.setParameter(":CM", cm);
            dbOp.execute();
            rst = dbOp.getRstSet();

            if (rst.next()) {
                idtipodoc = "" + rst.getInt("ID_TIPODOC");
                codmod = rst.getString("CODICE_MODELLO_PADRE");
            } else {
                return "";
            }

            if (idtipodoc == null) {
                idtipodoc = "";
            }

            if (idtipodoc.length() == 0) {
                dbOp.setParameter(":AREA", ar);
                dbOp.setParameter(":CM", codmod);
                dbOp.execute();
                rst = dbOp.getRstSet();

                if (rst.next()) {
                    idtipodoc = rst.getString("ID_TIPODOC");
                }
            }
            return idtipodoc;
        } catch (Exception e) {
            logger.error("ServletVisualizza::ricavaIdtipodoc - " + e.toString());
            return "";
        }
    }

    private void copiaConformeMulitplo(String iddocs, HttpServletResponse response) {
        String nextTk = "";
        String prefisso = "";
        String iddoc = "";
        String ca = "";
        String formato = "";
        String nome = "";
        AccediDocumento ad = null;
        InputStream bis = null;
        ServletOutputStream sosStream = null;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        StringTokenizer st = new StringTokenizer(iddocs, "@");

        int pageOffset = 0;
        ArrayList master = new ArrayList();
        int f = 0;
        Document document = null;
        PdfCopy writer = null;

        try {
            sosStream = response.getOutputStream();
            while (st.hasMoreTokens()) {
                nextTk = st.nextToken();
                prefisso = nextTk.substring(0, 1);
                iddoc = nextTk.substring(1);
                if (prefisso.equalsIgnoreCase("D")) {
                    ad = new AccediDocumento(iddoc, vu);
                    ad.accediDocumentoAllegati();
                    //ad.connect();
                    int numAll = ad.listaIdOggettiFile().size();
                    if (numAll != 0) {
                        for (int i = 0; i < numAll; i++) {
                            if (ad.isOggettoFileVisibile(ad.listaIdOggettiFile().get(i).toString())) {
                                ca = ad.listaIdOggettiFile().get(i).toString();
                                formato = ad.nomeFormatoOggettoFile(ca);
                                nome = ad.nomeOggettoFile(ca);
                                if (nome.toUpperCase().indexOf(".PDF") > -1) {
                                    bis = ad.leggiOggettoFile(ca);
                                    SignedObjectReaderI allFirma = SignedObjectReaderFactory.getSignedObjectReader(bis);
                                    if (allFirma.isSigned()) {
                                        InputStream data;
                                        data = allFirma.getOriginalContent();
                                        nome = nome.replaceAll(".p7m", "");
                                        nome = nome.replaceAll(".P7M", "");
                                        formato = "PDF";

                                        PdfReader pdfreader = new PdfReader(data);
                                        pdfreader.consolidateNamedDestinations();
                                        // we retrieve the total number of pages
                                        int n = pdfreader.getNumberOfPages();
                                        List bookmarks = SimpleBookmark.getBookmark(pdfreader);
                                        if (bookmarks != null) {
                                            if (pageOffset != 0) {
                                                SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
                                            }
                                            master.addAll(bookmarks);
                                        }
                                        pageOffset += n;

                                        if (f == 0) {
                                            // step 1: creation of a
                                            // document-object
                                            document = new Document(pdfreader.getPageSizeWithRotation(1));
                                            // step 2: we create a writer that
                                            // listens to the document
                                            writer = new PdfCopy(document, outStream);
                                            // step 3: we open the document
                                            document.open();
                                        }
                                        // step 4: we add content
                                        PdfImportedPage page;
                                        for (int i1 = 0; i1 < n; ) {
                                            ++i1;
                                            page = writer.getImportedPage(pdfreader, i1);
                                            writer.addPage(page);
                                        }
                                        PRAcroForm form = pdfreader.getAcroForm();
                                        if (form != null) {
                                            writer.copyAcroForm(pdfreader);
                                        }

                                        f++;
                                    }
                                }
                            }
                        }
                    }
                    //ad.disconnect();
                }
            }
            if (!master.isEmpty()) {
                writer.setOutlines(master);
            }
            // step 5: we close the document
            document.close();

            ByteArrayInputStream data = new ByteArrayInputStream(outStream.toByteArray());
            PdfReader pdfreader = new PdfReader(data);
            int n = pdfreader.getNumberOfPages();
            PdfStamper stamp = new PdfStamper(pdfreader, sosStream);
            int i = 0;
            PdfContentByte over;
            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);
            while (i < n) {
                i++;
                over = stamp.getOverContent(i);
                over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_STROKE);
                over.beginText();
                over.setColorStroke(Color.GRAY);
                if (Parametri.COPIA_CONF_DIAGONALE.equalsIgnoreCase("S")) {
                    over.setFontAndSize(bf, 42);
                    over.showTextAligned(Element.ALIGN_CENTER, Parametri.COPIA_CONF_TESTO,
                        pdfreader.getPageSize(i).getWidth() / 2, pdfreader.getPageSize(i).getHeight() / 2, 60);
                }
                if (Parametri.COPIA_CONF_ALTO_SX.equalsIgnoreCase("S")) {
                    over.setFontAndSize(bf, 24);
                    over.showTextAligned(Element.ALIGN_LEFT, Parametri.COPIA_CONF_TESTO, 30,
                        pdfreader.getPageSize(i).getHeight() - 30, 0);
                }
                if (Parametri.COPIA_CONF_BASSO_SX.equalsIgnoreCase("S")) {
                    over.setFontAndSize(bf, 24);
                    over.showTextAligned(Element.ALIGN_LEFT, Parametri.COPIA_CONF_TESTO, 30, 30, 0);
                }
                if (Parametri.COPIA_CONF_BASSO_DX.equalsIgnoreCase("S")) {
                    over.setFontAndSize(bf, 24);
                    over.showTextAligned(Element.ALIGN_RIGHT, Parametri.COPIA_CONF_TESTO,
                        pdfreader.getPageSize(i).getWidth() - 30, 30, 0);
                }
                if (Parametri.COPIA_CONF_ALTO_DX.equalsIgnoreCase("S")) {
                    over.setFontAndSize(bf, 24);
                    over.showTextAligned(Element.ALIGN_RIGHT, Parametri.COPIA_CONF_TESTO,
                        pdfreader.getPageSize(i).getWidth() - 30, pdfreader.getPageSize(i).getHeight() - 30, 0);
                }
//				if (Parametri.COPIA_CONF_PIE_PAG.equalsIgnoreCase("S")) {
                getPiePagina();
                if (pie_pagina.equalsIgnoreCase("S")) {
                    BaseFont bf2 = BaseFont
                        .createFont(BaseFont.COURIER_BOLDOBLIQUE, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                    over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
                    over.setFontAndSize(bf2, 8);
                    Font font = new Font(bf2);
                    font.setSize(8);
                    ColumnText ct = new ColumnText(over);
                    Phrase myText = new Phrase(Parametri.COPIA_CONF_TESTO, font);
                    int altezza = (int) Math.ceil(ct.getWidth(myText) / pdfreader.getPageSize(i).getWidth());
                    altezza = (altezza * 12) + bottom;
                    ct.setSimpleColumn(myText, border, bottom, pdfreader.getPageSize(i).getWidth() - border, altezza,
                        10,
                        Element.ALIGN_LEFT);
                    ct.go();
                }
                over.endText();
            }
            stamp.close();
        } catch (Exception e) {
            try {
                //ad.disconnect();
            } catch (Exception e1) {
            }
            if (Parametri.DEBUG.equalsIgnoreCase("0")) {
                corpoHtml = "Attenzione si è verificato un errore.";
            } else {
                corpoHtml = e.toString();
            }
            e.printStackTrace();
            logger.error("Visualizza - Errore copiaConformeMulitplo: " + e.toString(), e);
            errore = true;
            return;
        }
    }

    private String formatoFile(String nomeFile) {
        int pos = 0;
        int k = 0;
        if (nomeFile.length() > 0 && nomeFile.indexOf(".") > -1) {
            int j = nomeFile.toLowerCase().indexOf(".p7m");
            if (j > -1) {
                while (k < j) {
                    pos = k;
                    k = nomeFile.toLowerCase().indexOf(".", k + 1);
                }
                return nomeFile.substring(pos + 1, j) + ".p7m";
            } else {
                while (k > -1) {
                    pos = k;
                    k = nomeFile.toLowerCase().indexOf(".", k + 1);
                }
                return nomeFile.substring(pos + 1);
            }
        } else {
            return "";
        }
    }

    private void getPiePagina() {
        IDbOperationSQL dbOp = null;
        ResultSet rst = null;
        String query;

        query = "SELECT NVL(f_valore_parametri('COPIA_CONF_PIE_PAG@STANDARD'),'N'),NVL(f_valore_parametri('COPIA_BORDER@STANDARD'),'40'),NVL(f_valore_parametri('COPIA_BOTTOM@STANDARD'),'40') FROM DUAL";
        try {
            dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);

            dbOp.setStatement(query);
            dbOp.execute();
            rst = dbOp.getRstSet();

            if (rst.next()) {
                pie_pagina = rst.getString(1);
                border = Integer.parseInt(rst.getString(2));
                bottom = Integer.parseInt(rst.getString(3));
            }

            free(dbOp);
        } catch (Exception e) {
            logger.error("ServletVisualizza::getPiePagina - " + e.toString());
            free(dbOp);
        }
    }

    public static void main(String[] args) {
        try {
            InputStream data;
            data = new FileInputStream("D:/FatturazioneElettronicaNew/DET_DETE_11_2016.pdf");
            FileOutputStream sosStream = new FileOutputStream("D:/FatturazioneElettronicaNew/copia.pdf");
            PdfReader pdfreader = new PdfReader(data);
            int n = pdfreader.getNumberOfPages();
            PdfStamper stamp = new PdfStamper(pdfreader, sosStream);
            int i = 0;
            String testo = "\"Copia analogica ai sensi dell'art. 3-bis D.Lgs n 82/2005 e s.m.i. di documento informatico sottoscritto con firma digitale predisposto e conservato negli archivi della scrivente amministrazione nel rispetto delle regole tecniche di cui all'art. 71 D.Lgs n 82/2005 e s.m.i.\".";

            PdfContentByte over;

            System.out.println(pdfreader.getPageSize(1).getWidth());
            System.out.println(pdfreader.getPageSize(1).getHeight());
            System.out.println(pdfreader.getPageSize(1).getBorderWidthBottom());
            int border = 40;
            int bottom = 10;
            BaseFont bf = BaseFont.createFont(BaseFont.COURIER_BOLDOBLIQUE, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            while (i < n) {
                i++;
                over = stamp.getOverContent(i);
                over.beginText();
                over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
                over.setFontAndSize(bf, 10);
                ColumnText ct = new ColumnText(over);
                Font f = new Font(bf);
                f.setSize(8);
                Phrase myText = new Phrase(testo, f);
                int altezza = (int) Math.ceil(ct.getWidth(myText) / pdfreader.getPageSize(i).getWidth());
//				int altezza = (int) (ct.getWidth(myText) / (pdfreader.getPageSize(i).getWidth() - 30));
                altezza = (altezza * 12) + bottom;
                ct.setSimpleColumn(myText, border, bottom, pdfreader.getPageSize(i).getWidth() - border, altezza, 10,
                    Element.ALIGN_LEFT);
                ct.go();
                over.endText();
            }
            stamp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}