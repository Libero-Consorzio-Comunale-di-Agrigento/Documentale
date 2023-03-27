package it.finmatica.dmServer.management;

/*
 * AGGIORNA UN DOCUMENTO
 *
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   14/09/2005
 *
 * */

import it.finmatica.dmServer.management.macroAction.AutomaticFolder;
import it.finmatica.dmServer.mapping.GDMapping;
import it.finmatica.dmServer.competenze.*;
import it.finmatica.dmServer.util.*;
import it.finmatica.dmServer.*;
import it.finmatica.jfc.io.*;
import it.finmatica.dmServer.check.CheckDocumento;

import java.util.*;
import java.io.*;

public class AggiornaDocumento extends ManageDocumento {

    String sStato;
    //Vettore di Allegati per HummingBird
    Vector hummObjAllegati;
    //HUMM_COD_ENTE -> CodEnte di HummingBird
    String HUMM_COD_ENTE;
    //HUMM_COD_AOO  -> A00 di HummingBird
    String HUMM_COD_AOO;
    boolean salvaSempre = false;
    private boolean bSkipUnknowField = false;
    private Vector<String> listSkipUnknowField = new Vector<String>();

    private ElapsedTime elpsTime;

    //Se messo a true significa che
    //lancerò l'update del documento,
    //altrimenti lancerò solo la salvaACL.
    boolean bIsModified = false;

    boolean dontRebuildOrdinamenti = false;

    boolean bPassaModificaCompetenze = true;

    boolean bAggiornaDataUltAggiornamento = true;

    boolean bForceMaintainPreBozza = false;

    private String sModello;

    private long nMaxNAllegati = -1;

    private boolean bogfilog = true;

    // ***************** METODI DI INIZIALIZZAZIONE ***************** //

    /*
     * METHOD:      Constructor(String,Environment)
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: Aggiornamento di un documento a partire
     *              dall'idDocumento e variabili di
     *              ambiente
     *
     * RETURN:      none
     */
    public AggiornaDocumento(String idDocument, Environment vEnv) throws Exception {

        super(idDocument, vEnv);

        // ************* SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA ************************ //
        /* */
        if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM)) {
            /* */
            try {
                /* */
                super.loadDocument();
                /* */
            }
            /* */ catch (Exception e)
                /* */ {
                /* */
                varEnv.disconnectClose();
                /* */
                throw new Exception(
                    "Modifica del documento (" + idDocument + ") - Controllo delle competenze\n" + e.getMessage());
                /* */
            }

            //Controllo check in / check out
            CheckDocumento chkDoc = null;
            try {
                chkDoc = new CheckDocumento(idDocument, varEnv.getDbOp().getConn());
            } catch (Exception e) {
                varEnv.disconnectClose();
                throw new Exception(
                    "Modifica del documento (" + idDocument + ") Fallita (errore di controllo check in).\n" + e
                        .getMessage());
            }

            int icheck = chkDoc.verificaCheck(varEnv.getUser());

            if (icheck == 1 || icheck == 2) {
                varEnv.disconnectClose();
                throw new Exception("Modifica al documento (" + idDocument + ") Fallita (errore check in).\n" + chkDoc
                    .getErrorMessage());
            }
            //Fine Controllo check in / check out

            /* */
            /* */
            sStato = aDocumento.getStatusDocumento().getStato();
            /* */
            /* */
            try {
                /* */
                aDocumento.retrieve(true, true, true, false, null);
                nMaxNAllegati = aDocumento.getTipoDocumento().getNMaxAllegati();
                /* */
                this.setUltAggiornamento(aDocumento.getUltAggiornamento());

                sModello = new DocUtil(varEnv).getModelloByIdDocumento(aDocumento.getIdDocumento());
                /* */
            }
            /* */ catch (Exception e)
                /* */ {
                varEnv.disconnectClose();
                /* */
                throw new Exception(
                    "Modifica del documento (" + idDocument + ") - Tentativo di recupero dati\n" + e.getMessage());
                /* */
            }
            /* */
        }
        // *************************************************************************************** //

        // *************  SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD *********************** //
        /* */
        else if (varEnv.Global.DM.equals(varEnv.Global.HUMMINGBIRD_DM)) {
            /* */
            hummObjAllegati = new Vector();
            /* */
            /* */
            try {
                /* */
                pantareiLogin();
                /* */
            }
            /* */ catch (Exception e)
                /* */ {
                /* */
                throw new Exception("AggiornaDocumento::Constructor() pantareiLogin\n" + e.getMessage());
                /* */
            }
            /* */
            /* */
            String formName, lngIDDoc;
            /* */
            lngIDDoc = idDocument.substring(0, idDocument.indexOf("@"));
            /* */
            formName = idDocument.substring(idDocument.indexOf("@") + 1, idDocument.length());
            /* */
            /* */
            try {
                /* */
                super.createDocument(formName, "", "0");
                /* */
            }
            /* */ catch (Exception e)
                /* */ {
                /* */
                throw new Exception("AggiornaDocumento::Constructor() creazione Documento HUMM\n" + e.getMessage());
                /* */
            }
            /* */
            /* */
            aDocumento.setIdDocumento(lngIDDoc); // QUESTO MI SERVE PER ANNULARE LA SEZIONE SEARCH_INFOS
            /* */                                               // SE MESSO A -1
            /* */
            try {
                /* */
                aDocumento.retrieve(true, true, false, false, null);
                /* */
            }
            /* */ catch (Exception e)
                /* */ {
                /* */
                throw new Exception("AggiornaDocumento::Constructor() retrieve Documento HUMM\n" + e.getMessage());
                /* */
            }
            /* */
            /* */
            int size = aDocumento.getValori().size();
            /* */
            for (int i = 0; i < size; i++)
                /* */ {
                /* */
                if (((A_Valori) aDocumento.getValori().elementAt(i)).getCampo().getNomeCampo().equals("COD_ENTE"))
                    /* */ {
                    HUMM_COD_ENTE = (String) ((A_Valori) aDocumento.getValori().elementAt(i)).getValore();
                }
                /* */
                if (((A_Valori) aDocumento.getValori().elementAt(i)).getCampo().getNomeCampo().equals("COD_AOO"))
                    /* */ {
                    HUMM_COD_AOO = (String) ((A_Valori) aDocumento.getValori().elementAt(i)).getValore();
                }
                /* */
            }
            /* */
            /* */
            aDocumento.annullaRiferimento();
            /* */
            aDocumento.svuotaListaOggettiFile();
            /* */
            aDocumento.svuotaListaValori();
            /* */
            aDocumento.annullaACL();
            /* */
            /* */
        }
        // *************************************************************************************** //

        elpsTime = new ElapsedTime("AGGIORNA_DOCUMENTO", varEnv);
    }

    // ***************** METODI DI GESTIONE DEI VALORI ***************** //

    public void aggiornaDati(Object campo, Object valore) throws Exception {
        aggiornaDati(campo, valore, false);
    }

    /*
     * METHOD:      aggiornaDati(Object,Object)
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: - Per Finmatica:
     *                   Data la coppia (campo,valore) questa viene aggiornata
     *                   sulla lista dei valori se campo esiste.
     *                   Se campo non esiste questo viene inserito.
     *              - Per HummingBird:
     *                   Data la coppia (campo,valore) questa viene aggiunta
     *                   sulla lista dei valori
     *
     * RETURN:      void
     */
    public void aggiornaDati(Object campo, Object valore, boolean append) throws Exception {
        String idCampo = "-1";
        String nomeTipoDoc;
        String sCampo = "";
        FieldInformation fi = null;

        bOnlySysPdf = 2;

        if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM)) {
            nomeTipoDoc = aDocumento.getTipoDocumento().getIdTipodoc();
        } else {
            nomeTipoDoc = varEnv.Global.MAPPING_TIPO_DOC_PANTAREI;
        }

        //Se passo il nome del campo, allora
        //devo recuperare l'idCampo
        if (campo instanceof String) {
            try {
                sCampo = varEnv.getGDMapping().getMappingCampo(sModello, true, campo.toString());
            } catch (Exception e) {
                varEnv.disconnectClose();
                throw new Exception("AggiornaDocumento::aggiornaDati() mapping\n" + e.getMessage());
            }

            //lookUp per recuperare idCampo
            try {
                fi = (new LookUpDMTable(varEnv)).lookUpInfoCampo(sCampo, nomeTipoDoc);

                idCampo = fi.getIdCampo();
            } catch (Exception e) {
                if (checkUnknowField(sCampo)) {
                    return;
                }

                varEnv.disconnectClose();
                throw new Exception("Modifica dei dati del documento (" + aDocumento.getIdDocumento()
                    + ") - Errore nel recupero della Informazioni Campo (" + sCampo + ") Documento\n" + e.getMessage());
            }
        }
        //Ho passato direttamente l'idCampo
        else {
            idCampo = campo.toString();

            fi = (new LookUpDMTable(varEnv)).lookUpInfoCampo(idCampo);
        }

        //Controllo se qualche campo inserito è non in uso
   	  /* if (!fi.getInUso().equals("Y")) {
   		   StringBuffer sMessaggio = new StringBuffer("");
   		   
   		   sMessaggio.append("[DMSERVER_WARNING] - Non è possibile l'aggiornamento del valore per il campo "+sCampo+" nel documento "+aDocumento.getIdDocumento()+" (tipo_documento="+aDocumento.getTipoDocumento().getIdTipodoc()+").\n");
   		   sMessaggio.append("Il campo "+sCampo+" non è IN USO");
   		   //throw new Exception(sMessaggio.toString());
   		   System.out.println(sMessaggio.toString());
   	   }*/

        sistemavalore(idCampo, campo, valore, append, sCampo, fi);
    }

    public void aggiornaDatiMultipla(Vector<String> campo, Vector<Object> valore, Vector<Object> valoriAppend)
        throws Exception {
        bOnlySysPdf = 2;
        String idTipoDoc;
        Vector<FieldInformation> fiLocalVector = null;
        HashMap<String, ValoreInfo> valoriHM = new HashMap<String, ValoreInfo>();

        if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM)) {
            idTipoDoc = aDocumento.getTipoDocumento().getIdTipodoc();
        } else {
            idTipoDoc = varEnv.Global.MAPPING_TIPO_DOC_PANTAREI;
        }

        String sqlInfoCampo = "";
        for (int i = 0; i < campo.size(); i++) {
            if (!sqlInfoCampo.equals("")) {
                sqlInfoCampo += " UNION ";
            }
            sqlInfoCampo += (new LookUpDMTable(varEnv)).getSQLInfoCampo(campo.get(i), idTipoDoc, null);

            boolean bAppend = false;
            if (valoriAppend.elementAt(i) != null && valoriAppend.elementAt(i).equals("S")) {
                bAppend = true;
            }
            valoriHM.put(campo.get(i), new ValoreInfo(valore.get(i), bAppend));
        }
        //System.out.println(sqlInfoCampo);
        if (!sqlInfoCampo.equals("")) {
            fiLocalVector = (new LookUpDMTable(varEnv)).lookUpVectorInfoCampo(sqlInfoCampo);

            if (fiLocalVector != null) {
                for (int i = 0; i < fiLocalVector.size(); i++) {
                    FieldInformation fi = fiLocalVector.get(i);

                    sistemavalore(fi.getIdCampo(), fi.getNomeCampo(), valoriHM.get(fi.getNomeCampo()).valore,
                        valoriHM.get(fi.getNomeCampo()).bAppend, fi.getNomeCampo(), fi);
                }
            }
        }
    }

    private void sistemavalore(String idCampo, Object campo, Object valore, boolean append, String sCampo,
        FieldInformation fi) throws Exception {
        // ************* SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA ************************* //
        /* */
        if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM)) {
            /* */
            A_Valori a;
            /* */
            try {
                /* */
                a = aDocumento.findValoreByCampi(idCampo);
                /* */
            }
            /* */ catch (Exception e)
                /* */ {
                /* */
                varEnv.disconnectClose();
                /* */
                throw new Exception("AggiornaDocumento::aggiornaDati() findValoreByCampi\n" + e.getMessage());
                /* */
            }
            /* */
            /* */     //Se il campo esiste già nel vettore, viene
            /* */     //aggiornato il relativo valore
            /* */
            if (a != null) {
                String sValoreSuDb;

                if (a.getValore() == null) {
                    sValoreSuDb = "";
                } else {
                    sValoreSuDb = "" + a.getValore();
                }

                /* */
                if (sValoreSuDb.equals(valore) && !append) {
                    return;
                }
                /* */
                if (valore instanceof String) {

                    //Se il vecchio valore era vuoto faccio
                    //in modo che si possa comunque modificare
                    //anche se su dati il campo è "SENZA_AGGIORNAMENTO"
                    if (sValoreSuDb.equals("")) {
                        fi.setSenzaAggiornamento("N");
                    }

                    String sVal = valore + "";

                    if (append && a.getValore() != null) {
                        sVal = a.getValore() + sVal;
                    }

                    if (sCampo.equals(_CART_AUTO_FIELD)) {
                        folderAutoString = "" + sVal;
                    }

                    a.setValore(sVal);
                } else {
                    a.setValore(valore);
                }
                /* */
                bIsModified = true;
                /* */
                aDocumento.aggiornaValore(a, fi);
                /* */
            }
            /* */     //Se il campo non esiste già nel vettore, viene
            /* */     //aggiunta la coppia (campo,valore) nel vettore
            /* */
            else {
                /* */
                try {
                    if (valore.equals("")) {
                        return;
                    }
                    /* */
                    bIsModified = true;
                    //Se il vecchio valore era vuoto faccio
                    //in modo che si possa comunque modificare
                    //anche se su dati il campo è "SENZA_AGGIORNAMENTO"
                    fi.setSenzaAggiornamento("N");

                    if (sCampo.equals(_CART_AUTO_FIELD)) {
                        folderAutoString = "" + valore;
                    }

                    /* */
                    aDocumento.addValore(campo.toString(), idCampo, valore, fi);
                    /* */
                }
                /* */ catch (Exception e)
                    /* */ {
                    /* */
                    varEnv.disconnectClose();
                    /* */
                    throw new Exception("AggiornaDocumento::aggiornaDati() addValore FINMATICA\n" + e.getMessage());
                    /* */
                }
                /* */
            }
            /* */
        }
        // *************************************************************************************** //

        // *************  SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD *********************** //
        /* */
        else if (varEnv.Global.DM.equals(varEnv.Global.HUMMINGBIRD_DM))
            /* */ {
            try {
                /* */
                aDocumento.addValore(idCampo, valore.toString());
                /* */
            }
            /* */ catch (Exception e)
                /* */ {
                /* */
                throw new Exception("AggiornaDocumento::aggiornaDati() addValore HUMM\n" + e.getMessage());
                /* */
            }
        }
        // *************************************************************************************** //
    }

    private boolean checkUnknowField(String sCampo) {
        boolean bCheck = false;

        if (bSkipUnknowField == true) {
            return true;
        }

        if (listSkipUnknowField == null || listSkipUnknowField.size() == 0) {
            bCheck = false;
        } else {
            //Controllo se è nella lista degli skip
            for (int j = 0; j < listSkipUnknowField.size(); j++) {
                if (sCampo.toLowerCase().equals(listSkipUnknowField.get(j).toLowerCase())) {
                    bCheck = true;
                    break;
                }
            }
        }

        return bCheck;
    }

    public void setAggiornaDataUltAggiornamento(boolean bFlag) {
        bAggiornaDataUltAggiornamento = bFlag;
    }

    // ***************** METODI DI GESTIONE DEGLI OGGETTI FILE ***************** //

    /*
     * METHOD:      aggiornaAllegato(InputStream,String)
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: istream    -> File da utilizzare per la sostituzione
     *              pathFile   -> Path da cui estrapolare il nome del file già esistente
     *                            di cui sostiture il contenuto con istream
     *
     *              Questo metodo può essere utilizzato tipicamente per
     *              sostituire il contenuto di un file esistente con un
     *              nuovo contenuto, conoscendo il nome del file di origine
     *
     * RETURN:      void
     */
    public void aggiornaAllegato(InputStream istream, String pathFile) throws Exception {
        String idAllegato;

        //Recupera l'id dell'allegato
        try {
            idAllegato = (new LookUpDMTable(varEnv)).lookUpOggettoByName(pathFile, aDocumento.getIdDocumento());
        } catch (Exception e) {
            varEnv.disconnectClose();
            throw new Exception("AggiornaDocumento::aggiornaAllegato(@,@) - lookUp\n" + e.getMessage());
        }

        try {
            //Se non l'ho trovato lo aggiungo
            if (idAllegato.equals(pathFile)) {
                aggiungiAllegato(null, istream, pathFile);
            }
            //Se l'ho trovato lo aggiorno
            else {
                aggiornaAllegato(idAllegato, istream, pathFile);
            }
        } catch (Exception e) {
            varEnv.disconnectClose();
            throw new Exception("AggiornaDocumento::aggiornaAllegato(@,@)\n" + e.getMessage());
        }
    }

    /*
     * METHOD:      aggiornaAllegato(String,InputStream,String)
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: idAllegato -> id dell'allegato da sostituire
     *              istream    -> File da utilizzare per la sostituzione
     *              pathFile   -> Path da cui estrapolare il nome del nuovo file
     *
     *              Questo metodo può essere utilizzato tipicamente per
     *              sostituire completamente un allegato esistente con
     *              un nuovo file, conoscendo l'id del file di origine
     *
     * RETURN:      void
     */
    public void aggiornaAllegato(String idAllegato, InputStream istream, String pathFile) throws Exception {
        try {
            this.aggiornaAllegato(idAllegato, istream, pathFile, "N");
        } catch (Exception e) {
            varEnv.disconnectClose();
            throw new Exception("AggiornaDocumento::aggiornaAllegato(@,@,@)\n" + e.getMessage());
        }
    }

    /*
     * METHOD:      aggiornaAllegato(String,InputStream,String,String)
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: idAllegato -> id dell'allegato da sostituire
     *              istream    -> File da utilizzare per la sostituzione
     *              pathFile   -> Path da cui estrapolare il nome del nuovo file
     *              isAllegato -> N o S
     *
     *              -Per Finmatica:
     *                 Viene richiamata la funzione aggiungiAllegato della classe
     *                 superiore
     *
     * RETURN:      void
     */

    private void aggiornaAllegato(String idAllegato, InputStream istream, String pathFile, String isAllegato)
        throws Exception {
        bOnlySysPdf = 2;

        try {
            // ************* SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA ************************** //
            /* */
            if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM)) {
                /* */
                A_Oggetti_File of = null;
                /* */
                /* */
                try {
                    /* */
                    of = aDocumento.findOggettoFile(idAllegato);
                    /* */
                }
                /* */ catch (Exception e)
                    /* */ {
                    /* */
                    throw new Exception(
                        "AggiornaDocumento::aggiornaAllegato(@,@,@,@) - findOggettoFile\n" + e.getMessage());
                    /* */
                }
                /* */
                /* */
                String sPath = Global.adjustsPath(varEnv.Global.WEB_SERVER_TYPE, pathFile);
                /* */
                /* */
                String sFileName = Global.lastTrim(sPath, "/", varEnv.Global.WEB_SERVER_TYPE);
                /* */
                String idFormato = null;
                /* */
                /* */
                try {
                    /* */
                    idFormato = (new LookUpDMTable(varEnv))
                        .lookUpFormato(Global.lastTrim(sFileName, ".", varEnv.Global.WEB_SERVER_TYPE));
                    /* */
                }
                /* */ catch (Exception e)
                    /* */ {
                    /* */
                    throw new Exception(
                        "AggiornaDocumento::aggiornaAllegato(@,@,@,@) - lookUpFormato\n" + e.getMessage());
                    /* */
                }
                /* */
                /* */
                of.setFile(istream);
                /* */
                of.setFileName(sFileName);
                /* */
                of.setIdFormato(idFormato);
                /* */
                of.setAllegato(isAllegato);
                /* */
                of.setOggettoFileTemp(true);
                /* */
                /* */
                try {
                    /* */
                    aDocumento.aggiornaOggettoFile(of);
                    /* */
                }
                /* */ catch (Exception e)
                    /* */ {
                    /* */
                    throw new Exception(
                        "AggiornaDocumento::aggiornaAllegato(@,@,@,@) - aggiornaOggettoFile\n" + e.getMessage());
                    /* */
                }
                /* */
            }
            // *************************************************************************************** //

            // *************  SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD *********************** //
            /* */
            else if (varEnv.Global.DM.equals(varEnv.Global.HUMMINGBIRD_DM)) {
                /* */
                try {
                    /* */
                    super.aggiungiAllegato(istream, pathFile, "N");
                    /* */
                }
                /* */ catch (Exception e)
                    /* */ {
                    /* */
                    throw new Exception(
                        "AggiornaDocumento::aggiornaAllegato(@,@,@,@) - aggiungiAllegato\n" + e.getMessage());
                    /* */
                }
                /* */
            }
            // *************************************************************************************** //
        } catch (Exception e) {
            throw new Exception("AggiornaDocumento::aggiornaAllegato(@,@,@,@)\n" + e.getMessage());
        }
    }

    /*
     * METHOD:      aggiungiAllegatoeImpronta(InputStream,InputStream,String)
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: Inserimento del file nella lista degli oggetti file
     *              Inserisce anche un oggetto file figlio che è
     *              l'impronta
     *
     *
     * RETURN:      void
     */
    public void aggiornaAllegatoeImpronta(InputStream istream,
        InputStream istreamImpronta,
        String pathFile) throws Exception {
        try {
            String idAllegato;

            //Recupera l'id dell'allegato
            try {
                idAllegato = (new LookUpDMTable(varEnv)).lookUpOggettoByName(pathFile, aDocumento.getIdDocumento());
            } catch (Exception e) {
                varEnv.disconnectClose();
                throw new Exception("AggiornaDocumento::aggiornaAllegato(@,@) - lookUp\n" + e.getMessage());
            }

            //Non lo trovo quindi la aggiungo
            if (idAllegato.equals(pathFile)) {
                bIsModified = true;
                super.aggiungiAllegatoeImpronta(istream, istreamImpronta, pathFile);
            } else {
                bIsModified = true;
                this.aggiornaAllegato(idAllegato, istream, pathFile);

                Impronta impronta = new Impronta("SHA1");

                byte[] bImpronta = impronta.dbHash(istreamImpronta, "SHA1");

                super.aggiungiAllegato((new ByteArrayInputStream(bImpronta)),
                    Global.replaceAll(pathFile, ".", "_") + ".SYS_HASH", "S");
            }
        } catch (Exception e) {
            varEnv.disconnectClose();
            throw new Exception("AggiungiDocumento::aggiungiAllegato()\n" + e.getMessage());
        }
    }

    /*
     * METHOD:      creaImprontaPerAllegato(String)
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: Inserimento dell'impronta per l'allegato "fileName"
     *
     * RETURN:      void
     */
    public void creaImprontaPerAllegato(String fileName) throws Exception {
        try {
            String idAllegato;
            A_Oggetti_File objFile;

            //Recupera l'id dell'allegato
            try {
                idAllegato = (new LookUpDMTable(varEnv)).lookUpOggettoByName(fileName, aDocumento.getIdDocumento());
            } catch (Exception e) {
                throw new Exception("Impossibile trovare l'allegato " + fileName + " sul documento\n" + e.getMessage());
            }

            if (idAllegato.equals(fileName)) {
                throw new Exception("Impossibile trovare l'allegato " + fileName + " sul documento");
            }
            //Fine recupero id allegato

            objFile = aDocumento.findOggettoFile(idAllegato);

            if (objFile == null) {
                throw new Exception("Errore nel recupero dell'allegato " + fileName + " dalla lista dei file");
            }

            this.bIsModified = true;

            Impronta impronta = new Impronta("SHA1");

            byte[] bImpronta = impronta.dbHash((InputStream) objFile.getFile(), "SHA1");

            super.aggiungiAllegato((new ByteArrayInputStream(bImpronta)),
                Global.replaceAll(fileName, ".", "_") + ".SYS_HASH", idAllegato, "S");
        } catch (Exception e) {
            varEnv.disconnectClose();
            throw new Exception("AggiungiDocumento::creaImprontaPerAllegato()\n" + e.getMessage());
        }
    }

    /*
     * METHOD:      aggiungiAllegatoConPadre(InputStream,String,String)
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: istream         -> File da inserire
     *              pathFile        -> nome del file
     *              filePadre   -> nome del file padre
     *
     *              -Per Finmatica:
     *                 Viene richiamata la funzione aggiungiAllegato della classe
     *                 superiore
     *              -Per HummingBird:
     *                 ............................
     *
     * RETURN:      void
     */
    public void aggiungiAllegatoConPadre(InputStream istream, String pathFile, String filePadre) throws Exception {
        this.aggiungiAllegato(istream, pathFile, (InputStream) null, filePadre, "S");
    }

    /*
     * METHOD:      aggiungiAllegato(InputStream,String)
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: istream    -> File da inserire
     *              pathFile   -> Path da cui estrapolare il nome del file
     *
     *              -Per Finmatica:
     *                 Viene richiamata la funzione aggiungiAllegato della classe
     *                 superiore
     *              -Per HummingBird:
     *                 ............................
     *
     * RETURN:      void
     */
    public void aggiungiAllegato(String contentType, InputStream istream, String pathFile) throws Exception {
        this.aggiungiAllegato(contentType, istream, pathFile, (InputStream) null, null, "N");
    }

    public void aggiungiAllegato(InputStream istream, String pathFile, InputStream istreamP7M, String filePadre,
        String allegato) throws Exception {
        aggiungiAllegato(null, istream, pathFile, istreamP7M, filePadre, allegato);
    }

    /*
     * METHOD:      aggiungiAllegato(InputStream,String,InputStream)
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: istream    -> File da inserire
     *              pathFile   -> Path da cui estrapolare il nome del file
     *              istreamP7M -> File p7m di HummingBird
     *
     *              -Per Finmatica:
     *                 Viene richiamata la funzione aggiungiAllegato della classe
     *                 superiore
     *              -Per HummingBird:
     *                 ............................
     *
     * RETURN:      void
     */
    public void aggiungiAllegato(String contentType, InputStream istream, String pathFile, InputStream istreamP7M,
        String filePadre, String allegato) throws Exception {
        try {
            // ************* SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA ************************** //
            if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM)) {
                bIsModified = true;
                super.aggiungiAllegato(istream, pathFile, filePadre, allegato, contentType);
            }

            // *************  SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD *********************** //
            // SE SONO COLLEGATO CON HUMMINGBIRD MI CREO UNA LISTA DI
            // FILE ALLEGATI E PER OGNUNA DI QUESTI CREERO' UN DOCUMENTO
            // EX NOVO CON LA AGGIUNGI_DOCUMENTO E RELATED QUESTO DOC
            else if (varEnv.Global.DM.equals(varEnv.Global.HUMMINGBIRD_DM)) {
                if (HUMM_COD_ENTE == null) {
                    throw new Exception("Non sono in grado di recuperare COD_ENTE dal documento padre per l'allegato");
                }
                //if (HUMM_COD_AOO==null) throw new Exception("Non sono in grado di recuperare COD_AOO dal documento padre per l'allegato");

                AggiungiDocumento ad = new AggiungiDocumento("DEF_DOC_GENERICO",
                    aDocumento.getArea(),
                    this.varEnv);

                ad.aggiungiDati("DOCNAME", Global.lastTrim(pathFile, "\\", varEnv.Global.WEB_SERVER_TYPE));
                ad.aggiungiDati("TYPE_ID", "DOCUMENTO");
                ad.aggiungiDati("STATO_PANTAREI", "1");
                ad.aggiungiDati("COD_ENTE", HUMM_COD_ENTE);
                //ad.aggiungiDati("COD_AOO",HUMM_COD_AOO);
                ad.aggiungiRiferimento(aDocumento.getIdDocumento(), Global.RIF_HUMM);

                //Vector v = (Vector)aDocumento.getACL();

               /*for(int i=0;i<v.size();i++)
                  ad.aggiungiACL(((ACL)v.elementAt(i)).getPersonGroup(),((ACL)v.elementAt(i)).getMask());*/

                ad.aggiungiAllegato(istream, pathFile);

                if (istreamP7M != null) {
                    ad.settaFileP7M(istreamP7M);
                }

                hummObjAllegati.addElement(ad);
            }
        } catch (Exception e) {
            varEnv.disconnectClose();
            throw new Exception("AggiornaDocumento::aggiungiAllegato(@,@,@)\n" + e.getMessage());
        }
    }

    /*
     * METHOD:      aggiungiAllegato(InputStream,String)
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: istream    -> File sotto forma di stringa
     *              pathFile   -> Path da cui estrapolare il nome del file
     *
     *              -Per Finmatica:
     *                 Viene richiamata la funzione aggiungiAllegato della classe
     *                 superiore
     *
     * RETURN:      void
     */
    public void aggiungiAllegato(String istream, String pathFile) throws Exception {
        try {
            super.aggiungiAllegato(new ByteArrayInputStream(istream.getBytes()), pathFile, "N");
        } catch (Exception e) {
            varEnv.disconnectClose();
            throw new Exception("AggiornaDocumento::aggiungiAllegato()\n" + e.getMessage());
        }
    }

    public void renameAllegato(FileStruct fs) throws Exception {
        try {
            // ************* SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA ************************** //
            if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM)) {

                String idAllegato;

                //Recupera l'id dell'allegato da sostituire
                try {
                    if (fs.getIdOggettoFile() == 0) {
                        idAllegato = (new LookUpDMTable(varEnv))
                            .lookUpOggettoByName(fs.getFileNameToRename(), aDocumento.getIdDocumento());
                    } else {
                        idAllegato = "" + fs.getIdOggettoFile();
                    }
                } catch (Exception e) {
                    varEnv.disconnectClose();
                    throw new Exception("Recupero idAllegato da nomeFile da sostituire\n" + e.getMessage());
                }

                //Controllo che sia il nuovo nome sia univoco
                //Recupera l'id dell'allegato da sostituire
                if (!fs.getFileName().equals(fs.getFileNameToRename())) {
                    try {
                        String idAllegatoDaSostituire;
                        idAllegatoDaSostituire = (new LookUpDMTable(varEnv))
                            .lookUpOggettoByName(fs.getFileName(), aDocumento.getIdDocumento());

                        if (!idAllegatoDaSostituire.equals(fs.getFileName())) {
                            throw new Exception("Nome Allegato da sostituire già esistente per il documento\n");
                        }
                    } catch (Exception e) {
                        varEnv.disconnectClose();
                        throw new Exception("Controllo univocità nome File da sostituire\n" + e.getMessage());
                    }
                }

                //Recupera l'oggetto file in memoria del file da sostituire
                //a partire dall'idAllegato di cui sopra
                A_Oggetti_File of = null;

                try {
                    of = aDocumento.findOggettoFile(idAllegato);
                } catch (Exception e) {
                    throw new Exception("findOggettoFile\n" + e.getMessage());
                }

                //Recupero formato nuovo file da sostituire al precedente
                String idFormato = null;

                try {
                    idFormato = (new LookUpDMTable(varEnv))
                        .lookUpFormato(Global.lastTrim(fs.getFileName(), ".", varEnv.Global.WEB_SERVER_TYPE));
                } catch (Exception e) {
                    throw new Exception("Recupero formato nuovo file da sostituire al precedente\n" + e.getMessage());
                }

                //Aggiorno l'oggetto file con i nuovi valori

                of.setOldFileName(fs.getFileName());
                of.setFileName(fs.getFileNameToRename());
                of.setIdFormato(idFormato);

                //of.setFile(fs.getFileToRename());
                of.setOldFile(fs.getFile());

                try {
                    aDocumento.aggiornaOggettoFile(of);
                    bIsModified = true;
                } catch (Exception e) {
                    throw new Exception("Aggiornamento OggettoFile in memoria\n" + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new Exception("AggiornaDocumento::renameAllegato\n" + e.getMessage());
        }
    }

    public void salvaAllegatiTemp(boolean bFlag) {
        this.bIsModified = true;
        aDocumento.setAllegatiTempModulistica(bFlag);
    }

    /*
     * METHOD:      setScannedDocument()
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: Gestisce gli allegati scannerizzati da KOFAX
     *
     * RETURN:      void
     */
    public void setScannedDocument() throws Exception {
        try {
            String sPathKFX = aDocumento.getPercorsoKFX();

            if (sPathKFX.equals("-1")) {
                return;
            }

            bIsModified = true;
            LetturaScritturaFileFS f = new LetturaScritturaFileFS(sPathKFX);
            super.aggiungiAllegato(f.leggiFile(), sPathKFX + ".KFX", "N");
        } catch (Exception e) {
            varEnv.disconnectClose();
            throw new Exception("AggiornaDocumento::setScannedDocument()\n" + e.getMessage());
        }
    }

    /*
     * METHOD:      setPadre()
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: Aggiungi il riferimento al documento padre
     *
     * RETURN:      void
     */
    public void setPadre(String idPadre) {
        if (idPadre == null) {
            return;
        }

        aDocumento.settaPadre(idPadre);
        bIsModified = true;
    }

    /*
     * METHOD:      cancellaAllegato(String)
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: allegato   -> Nome del file da cancellare
     *
     * RETURN:      void
     */
    public void cancellaAllegato(String allegato) throws Exception {
        bOnlySysPdf = 2;

        try {

            String idAlleg = (new LookUpDMTable(varEnv)).lookUpOggettoByName(allegato, aDocumento.getIdDocumento());
            bIsModified = true;
            aDocumento.cancellaOggettiFile(idAlleg, true);
        } catch (Exception e) {
            varEnv.disconnectClose();
            throw new Exception("AggiornaDocumento::cancellaAllegato()\n" + e.getMessage());
        }
    }

    // ***************** METODI DI GESTIONE DEI RIFERIMENTI ***************** //

    /*
     * METHOD:      aggiungiRiferimento(String,String)
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: Inserimento riferimento di tipo "tipoRif"
     *              fra il documento attuale ed il documento "docRif"
     *
     * RETURN:      void
     */
    public void aggiungiRiferimento(String docRif, String tipoRif) throws Exception {
        bOnlySysPdf = 2;

        try {
            bIsModified = true;
            aDocumento.aggiungiRiferimento(docRif, tipoRif);
        } catch (Exception e) {
            //Sono con una connessione Esterna DISTRUGGO IL SAVEPOINT
            if (varEnv.Global.CONNECTION != null) {
                varEnv.rollbackToSavePoint();
            }
            varEnv.disconnectRollback();
            throw new Exception("AggiornaDocumento::aggiungiRiferimento()\n" + e.getMessage());
        }
    }

    /*
     * METHOD:      eliminaRiferimento(String)
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: Elimina riferimento fra il
     *              documento attuale ed il
     *              documento "docRif"
     *
     * RETURN:      void
     */
    public void eliminaRiferimento(String docRif, String tipoRif) throws Exception {
        bOnlySysPdf = 2;

        try {
            bIsModified = true;
            aDocumento.eliminaRiferimento(docRif, tipoRif);
        } catch (Exception e) {
            //Sono con una connessione Esterna DISTRUGGO IL SAVEPOINT
            if (varEnv.Global.CONNECTION != null) {
                varEnv.rollbackToSavePoint();
            }
            varEnv.disconnectRollback();
            throw new Exception("AggiornaDocumento::eliminaRiferimento()\n" + e.getMessage());
        }
    }

    // ***************** METODI DI REGISTRAZIONE DEL DOCUMENTO ***************** //

    /*
     * METHOD:      salvaDocumentoBozza()
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: Inserimento del documento con stato BOZZA
     *
     * RETURN:      boolean
     */
    public boolean salvaDocumentoBozza() throws Exception {
        if (varEnv.Global.DM.equals(varEnv.Global.HUMMINGBIRD_DM)) {
            return salvaDocumentoCompleto();
        } else {
            return salvaDocumento(Global.STATO_BOZZA);
        }
    }

    /*
     * METHOD:      salvaDocumentoAnnullato()
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: Inserimento del documento con stato ANNULLATO
     *
     * RETURN:      boolean
     */
    public boolean salvaDocumentoAnnullato() throws Exception {
        return salvaDocumento(Global.STATO_ANNULLATO);
    }

    /*
     * METHOD:      salvaDocumentoCancellato()
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: Inserimento del documento con stato CANCELLATO
     *
     * RETURN:      boolean
     */
    public boolean salvaDocumentoCancellato() throws Exception {
        return salvaDocumento(Global.STATO_CANCELLATO);
    }

    public boolean salvaDocumentoPreBozza() throws Exception {
        return salvaDocumento(Global.STATO_PREBOZZA);
    }

    /*
     * METHOD:      salvaDocumentoCompleto()
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: Inserimento del documento con stato COMPLETO
     *
     * RETURN:      boolean
     */
    public boolean salvaDocumentoCompleto() throws Exception {
        // *************  SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA ****************** //
        /* */
        if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM))
            /* */ {
            return salvaDocumento(Global.STATO_COMPLETO);
        }
        // ******************************************************************************** //
        // *************  SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD **************** //
        /* */
        else {
            /* */         // PRIMA SALVO GLI ELEMENTI RELATED
            /* */
            for (int i = 0; i < hummObjAllegati.size(); i++)
                /* */ {
                if (((AggiungiDocumento) hummObjAllegati.elementAt(i)).salvaDocumentoCompleto() == false)
                    /* */ {
                    return false;
                }
            }
            /* */
            if (!aDocumento.updateDocument()) {
                return false;
            }
            /* */
            return true;
            /* */
        }
        // ******************************************************************************** //
    }

    /*
     * METHOD:      salvaDocumento()
     * SCOPE:       PUBLIC
     *
     * DESCRIPTION: Aggiorna il documento senza cambiare stato
     *
     * RETURN:      boolean
     */
    public boolean salvaDocumento() throws Exception {
        return salvaDocumento(null);
    }

    public void versionaDocumento(long lVersione) throws Exception {
        versionaDocumento(lVersione, false, null);
    }

    public void versionaDocumento(long lVersione, boolean bNonRipetereUguali, Date dataAggiornamentoLog)
        throws Exception {

        try {
            aDocumento.saveVersion(lVersione, bNonRipetereUguali, dataAggiornamentoLog);
        } catch (Exception e) {
            //Sono con una connessione Esterna DISTRUGGO IL SAVEPOINT
            if (varEnv.Global.CONNECTION != null) {
                try {
                    varEnv.rollbackToSavePoint();
                } catch (Exception ei) {
                }
            }

            varEnv.disconnectRollback();
            throw new Exception(
                "Versioning del documento (" + aDocumento.getIdDocumento() + ") - Errore:\n" + e.getMessage());
        }

        varEnv.disconnectCommit();
    }

    public void moveFile(String idObjFile) throws Exception {
        if (!varEnv.getByPassCompetenze()) {
            Abilitazioni abilitazione = new Abilitazioni(Global.ABIL_DOC, aDocumento.getIdDocumento(), "U");
            UtenteAbilitazione ua = new UtenteAbilitazione(varEnv.getUser(), varEnv.getGruppo(), varEnv.getRuolo(),
                varEnv.getPwd(), varEnv.getUser(), varEnv);
            if ((new GDM_Competenze(varEnv)).verifica_GDM_Compentenza(ua, abilitazione) == 0) {
                throw new Exception("Modifica del documento (" + aDocumento.getIdDocumento()
                    + ") Fallita - Utente non autorizzato. Controllare le competenze di modifica dell'utente ("
                    + varEnv.getUser() + ")");
            }
        }

        List<String> listaPathFile;
        try {
            listaPathFile=aDocumento.moveFile(idObjFile);
        } catch (Exception e) {
            //Sono con una connessione Esterna DISTRUGGO IL SAVEPOINT
            if (varEnv.Global.CONNECTION != null) {
                try {
                    varEnv.rollbackToSavePoint();
                } catch (Exception ei) {
                }
            }

            try {varEnv.disconnectRollback();} catch (Exception ei) {}
            throw new Exception(
                "moveFile del documento (" + aDocumento.getIdDocumento() + " per idObjFile="+idObjFile+") - Errore:\n" + e.getMessage());
        }

        varEnv.disconnectCommit();

        //Dopo il commit cancello il file e file log che ho spostato in tabella
        //System.out.println("MANNY LISTA CANCELLA "+listaPathFile);
        if (listaPathFile!=null) {
            for(int i=0;i<listaPathFile.size();i++) {
               // System.out.println("MANNY CANCELLA "+listaPathFile.get(i));
                File f = new File(listaPathFile.get(i));
                f.delete();
            }
        }
    }

    /*
     * METHOD:      salvaDocumento(String)
     * SCOPE:       PRIVATE
     *
     * DESCRIPTION: Aggiornamento del documento con stato "stato"
     *              Se la regitrazione è stata effettuata, il
     *              metodo effettua la commit, in caso contrario
     *              la rollback
     *
     * RETURN:      boolean
     */
    private boolean salvaDocumento(String status) throws Exception {

        try {
            //Controllo se ho le competenze di modifica
            if (!varEnv.getByPassCompetenze() && bIsModified) {
                Abilitazioni abilitazione = new Abilitazioni(Global.ABIL_DOC, aDocumento.getIdDocumento(), "U");
                UtenteAbilitazione ua = new UtenteAbilitazione(varEnv.getUser(), varEnv.getGruppo(), varEnv.getRuolo(),
                    varEnv.getPwd(), varEnv.getUser(), varEnv);
                if ((new GDM_Competenze(varEnv)).verifica_GDM_Compentenza(ua, abilitazione) == 0) {
                    throw new Exception("Modifica del documento (" + aDocumento.getIdDocumento()
                        + ") Fallita - Utente non autorizzato. Controllare le competenze di modifica dell'utente ("
                        + varEnv.getUser() + ")");
                }
            }

            long nAllegatiDoc = getNumAllegati();
            if (nMaxNAllegati != -1 && nAllegatiDoc > nMaxNAllegati) {
                throw new Exception("Attenzione! il modello può contenere al massimo " + nMaxNAllegati
                    + " allegato/i.\nSi sta cercando di inserirne " + nAllegatiDoc);
            }

            aDocumento.setAggiornaDataUltAgg(bAggiornaDataUltAggiornamento);

            if (nessunaModifica()) {
                //Al primo aggiornamento il prebozza diventa bozza
                if (sStato.equals(Global.STATO_PREBOZZA) && bForceMaintainPreBozza == false) {
                    status = Global.STATO_BOZZA;
                }

                aDocumento.setSkipReindexFullTextField(skipReindexFullTextField);

                if (updateDocumentInRepository(status)) {
                    //Cambio stato
                    if (status != null && !sStato.equals(status)) {
                        verificaCambioStato(status);

                        aDocumento.cambiaStatoDocumento(status);
                    }

                    try {
                        Vector<String> v = (new LookUpDMTable(varEnv))
                            .lookUpInfoAr_Cm_Cr_Area(aDocumento.getIdDocumento(), null, false);

                        aDocumento.setPathFileArea(v.get(0));
                        aDocumento.setArcmcr(v.get(1));
                    } catch (Exception e) {
                        throw new Exception("lookUpInfoAr_Cm_Cr_Area\n" + e.getMessage());
                    }

                    //Sincronizzo i file gestiti da FS
                    aDocumento.syncroFS(false);

                    aDocumento.finalizzaGestioneAllegatiTemp();
                    aDocumento.disconnectDbOpAllegatiTemp();

                    //Gestisco le impronte dei file LOG per il log creato (se ne ho)
                    DMActivity_Log dmL = aDocumento.getDmALog();
                    if (dmL != null) {
                        try {
                            GD4_Oggetti_File_Log gd4Ogfi = new GD4_Oggetti_File_Log(varEnv.getDbOp());
                            gd4Ogfi.retrieveOggettiFileLog(aDocumento.getIdDocumento(), "" + dmL.getId_log());
                            gd4Ogfi.generaImpronte();
                        } catch (Exception e) {
                            throw new Exception(
                                "Errore in generazione impronte per oggetti_file_log. Errore = " + e.getMessage());
                        }
                    }

                    //Gestisco le impronte dei file principali che trovo dentro la lista (se il parametro è impostato)
                    super.gestisciImpronte();

                    this.ultAggiornamento = aDocumento.getUltAggiornamento();
                }
            } else {
                throw new Exception("Documento già modificato da un altro utente");
            }
        } catch (Exception e) {

            //Sono con una connessione Esterna DISTRUGGO IL SAVEPOINT
            if (varEnv.Global.CONNECTION != null) {
                try {
                    varEnv.rollbackToSavePoint();
                } catch (Exception ei) {
                }
            }

            aDocumento.finalizzaGestioneAllegatiTemp();
            aDocumento.disconnectDbOpAllegatiTemp();

            try {
                varEnv.disconnectRollback();
            } catch (Exception ei) {
            }
            throw new Exception("Modifica dei dati del documento (" + aDocumento.getIdDocumento()
                + ") - Errore nel salvataggio del Documento\n" + e.getMessage());
        }

        varEnv.disconnectCommit();

        //Gestione SysIntegration
        /*try {  
          manageSysIntegration();
        }
        catch (Exception e) { 
          varEnv.disconnectRollback();
          codeErrorPostSave=Global.CODERROR_SYNCRO_INTEGRATION_ERROR;
          descrCodeErrorPostSave="Errore in sinconizzazione con altri sistemi";  
          System.out.println("DMServer - AggiungiDocumento post save error: "+descrCodeErrorPostSave);;  
          return true;
        }  */

        //GESTIONE DELL'AUTOMATIC FOLDER CON RELATIVA CATCH DELLA varEnv.disconnectRollback(); nel caso di errore o commit se ok.
        if (folderAutoString != null) {
            try {
                AutomaticFolder am = new AutomaticFolder(aDocumento.getIdDocumento(), folderAutoString, varEnv);

                am.make();
                varEnv.disconnectCommit();
            } catch (Exception e) {
                codeErrorPostSave = Global.CODERROR_POSTSAVEDOCUMENT_FOLDERAUTO;
                descrCodeErrorPostSave = Global.DESCRERROR_POSTSAVEDOCUMENT_FOLDERAUTO + "\n" + e.getMessage();
                System.out.println("DMServer - AggiungiDocumento post save error: " + descrCodeErrorPostSave);
                varEnv.disconnectRollback();
            }
        }

        folderAutoString = null;
        //FINE GESTIONE DELL'AUTOMATIC FOLDER

        return true;
    }

    /*
     * METHOD:      updateDocumentInRepository()
     * SCOPE:       PRIVATE
     *
     * DESCRIPTION: Richiamo delle funzioni interne di aggiornamento
     *              di un documento
     *
     * RETURN:      boolean
     */
    private boolean updateDocumentInRepository(String newStato) throws Exception {

        if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM)) {

            //Se il documento non è stato modificato in alcun
            //attributo, salvo le sole ACL ed esco
            if (!this.bIsModified) {
                salvaACL("U");
                salvaCompetenze("U");
                return true;
            }

            if ((sStato.equals(Global.STATO_CANCELLATO) ||
                sStato.equals(Global.STATO_REVISIONATO) ||
                sStato.equals(Global.STATO_ATTESAREV) ||
                sStato.equals(Global.STATO_CHECKIN) ||
                sStato.equals(Global.STATO_ANNULLATO) ||
                (sStato.equals(Global.STATO_COMPLETO) && bOnlySysPdf == 2))) {

                if (sStato.equals(Global.STATO_ANNULLATO) && newStato.equals(Global.STATO_CANCELLATO)) {
                    return true;
                }

                throw new Exception(
                    "AggiornaDocumento::Constructor() Impossibile aggiornare il documento, Status: " + sStato);
            }

            try {

                elpsTime
                    .start("********* UPDATE DOCUMENT *********", "AGGIORNAMENTO DOCUMENTO CON VALORI ED OGGETTI FILE");
                aDocumento.setOgfiLog(bogfilog);
                aDocumento.creaVersione(creaVersione);

                if (aDocumento.updateDocument()) {
                    //Gestisto la cancellazione degli allegati
                    ultimaVersione = aDocumento.getUltimaVersione();

                    //Rifaccio la retrieve della lista di oggetti file per metterla a posto
                    //in base alle insert, delete, update effettuate
                    aDocumento.retrieve(false, false, true, false, null);

                    salvaACL("U");

                    salvaCompetenze("U");

                    if (!dontRebuildOrdinamenti) {
                        GestioneOrdinamentiCartelle ord = new GestioneOrdinamentiCartelle(varEnv,
                            aDocumento.getIdDocumento(), "D");
                        ord.rebuild(false);
                    }
                }

                elpsTime.stop();
            } catch (Exception e) {
                codeError = aDocumento.getCodeErrorSaveDoc();
                descrCodeError = aDocumento.getDescrErrorSaveDoc();
                throw new Exception("AggiornaDocumento::updateDocumentInRepository()\n" + e.getMessage());
            }
        }

        return true;
    }

    private void verificaCambioStato(String newStato) throws Exception {
        Abilitazioni abilitazione = new Abilitazioni(Global.ABIL_DOC, aDocumento.getIdDocumento(), "");
        UtenteAbilitazione ua = new UtenteAbilitazione(varEnv.getUser(), varEnv.getGruppo(), varEnv.getRuolo(),
            varEnv.getPwd(), varEnv.getUser(), varEnv);

        boolean bControlla = false;

        //Posso riesumare un documento dallo stato cancellato
        if (sStato.equals(Global.STATO_CANCELLATO) && newStato.equals(Global.STATO_BOZZA)) {
            return;
        }

        //Dallo stato bozza posso passare solo allo stato
        //Completo,Annullato,Cancellato
        if (sStato.equals(Global.STATO_BOZZA)) {
            //Per passare allo stato di Completo o Annullato
            //ho bisogno di controllare le competenze di modifica
            if (newStato.equals(Global.STATO_COMPLETO) ||
                newStato.equals(Global.STATO_ANNULLATO)) {
                abilitazione.setTipoAbilitazione(Global.ABIL_MODI);
                bControlla = true;
            }
            //Per passare allo stato di Cancellato
            //ho bisogno di controllare le competenze di Cancellazione
            if (newStato.equals(Global.STATO_CANCELLATO)) {
                abilitazione.setTipoAbilitazione(Global.ABIL_CANC);
                bControlla = true;
            }
        }

        //Dallo stato completo posso passare solo allo stato
        //Annullato,Cancellato
        if (sStato.equals(Global.STATO_COMPLETO)) {
            //Per passare allo stato di Annullato
            //ho bisogno di controllare le competenze di modifica
            if (newStato.equals(Global.STATO_ANNULLATO)) {
                abilitazione.setTipoAbilitazione(Global.ABIL_MODI);
                bControlla = true;
            }

            //Per passare allo stato di Cancellato
            //ho bisogno di controllare le competenze di Cancellazione
            if (newStato.equals(Global.STATO_CANCELLATO)) {
                abilitazione.setTipoAbilitazione(Global.ABIL_CANC);
                bControlla = true;
            }
        }

        //Dallo stato Annullato posso passare solo allo stato
        //Cancellato
        if (sStato.equals(Global.STATO_ANNULLATO)) {
            //Per passare allo stato di Cancellato
            //ho bisogno di controllare le competenze di Cancellazione
            if (newStato.equals(Global.STATO_CANCELLATO)) {
                abilitazione.setTipoAbilitazione(Global.ABIL_CANC);
                bControlla = true;
            }
        }

        //Dallo stato PreBozza posso passare solo allo stato BOZZA
        if (sStato.equals(Global.STATO_PREBOZZA) && newStato.equals(Global.STATO_BOZZA)) {
            return;
        }

        if (bControlla) {
            try {
                if (varEnv.getByPassCompetenze()) {
                    return;
                }

                if ((new GDM_Competenze(varEnv)).verifica_GDM_Compentenza(ua, abilitazione) == 1) {
                    return;
                } else {
                    throw new Exception("verificaCambioStato(" + newStato + ")\n" +
                        "Impossibile cambiare in stato " + newStato + " non si possiedono " +
                        "le competenze di cancellazione");
                }
            } catch (Exception e) {
                throw new Exception("verificaCambioStato(" + newStato + ")\n" + e.getMessage());
            }
        }

        throw new Exception(
            "verificaCambioStato(" + newStato + ")\nImpossibile cambiare stato da (" + sStato + ") a (" + newStato
                + ") ");
    }

    // ***************** METODI DI GET E SET ***************** //

    public boolean getSalvaSempre() throws Exception {
        return salvaSempre;
    }

    public void setSalvaSempre(boolean newSalva) throws Exception {
        salvaSempre = newSalva;
    }

    public void setModified(boolean isMod) throws Exception {
        bIsModified = isMod;
    }

    /*
     * METHOD:      nessunaModifica()
     * SCOPE:       PRIVATE
     *
     * DESCRIPTION:
     *
     * RETURN:      boolean
     */
    private boolean nessunaModifica() throws Exception {
        if (getSalvaSempre()) {
            return true;
        }

        try {
            String ultAgg = aDocumento.getUltAggiornamento();

            if (ultAgg.compareTo(this.getUltAggiornamento()) > 0)
            //è avvenuta una modifica esterna
            {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            throw new Exception("AggiornaDocumento::nessunaModifica()\n" + e.getMessage());
        }
    }

    public void setConservazione(String sConservazione) {
        super.setConservazione(sConservazione);

        bIsModified = true;
    }

    public void setArchiviazione(String sArchiviazione) {
        super.setArchiviazione(sArchiviazione);

        bIsModified = true;
    }

    public boolean isBIsModified() {
        return bIsModified;
    }

    public void setBIsModified(boolean isModified) {
        bIsModified = isModified;
    }

    public boolean isDontRebuildOrdinamenti() {
        return dontRebuildOrdinamenti;
    }

    public void setDontRebuildOrdinamenti(boolean dontRebuildOrdinamenti) {
        this.dontRebuildOrdinamenti = dontRebuildOrdinamenti;
    }

    public boolean isBForceMaintainPreBozza() {
        return bForceMaintainPreBozza;
    }

    public void setBForceMaintainPreBozza(boolean forceMaintainPreBozza) {
        bForceMaintainPreBozza = forceMaintainPreBozza;
    }

    public void setBSkipUnknowField(boolean skipUnknowField) {
        bSkipUnknowField = skipUnknowField;
    }

    public void setListSkipUnknowField(Vector<String> listSkipUnknowField) {
        this.listSkipUnknowField = listSkipUnknowField;
    }

    public boolean isOgfiLog() {
        return bogfilog;
    }

    public void setOgfiLog(boolean bogfilog) {
        this.bogfilog = bogfilog;
    }

    public int getIdLog() {
        return aDocumento.getDmALog().getId_log();
    }
}

class ValoreInfo {

    public Object valore;
    public boolean bAppend = false;

    public ValoreInfo(Object valore, boolean bAppend) {
        this.valore = valore;
        this.bAppend = bAppend;
    }
}