package mailpack;

import xmlpack.*;
import it.finmatica.cim.*;
//import it.finmatica.jfc.authentication.*;
import it.finmatica.jfc.utility.*;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.io.*;
import org.apache.log4j.Logger;

import it.finmatica.modulistica.inoltro.*;

/**
 * Classe per l'inoltro via mail di oggetti di tipo OggettoXML.
 * Sintassi di codifica dei parametri:<br>
 * <br>
 * <code>
 * parametri = "[SERVER]<i>server</i>[PORTA]<i>porta</i>[LOGIN]<i>login</i>[PASSWORD]<i>password</i>[C_PASSWORD]<i>crypted password</i>[MITTENTE]<i>mittente</i>[MITTENTE_IND]<i>mittente_ind</i>[OGGETTO]<i>oggetto</i>[TESTO]<i>testo</i>[LISTA_DEST_IND]<i>lista_dest_ind</i>[LISTA_CC_IND]<i>lista_cc_ind</i>[LISTA_BC_IND]<i>lista_bc_ind</i>"<br>
 * <br>
 * </code>
 * dove [SERVER], [PORTA], ... , [LISTA_BC_IND] sono i marcatori di campo.<br>
 * L'ordine di definizione non � determinante ed i marcatori non sono <i>case sensitive</i>.<br><br>
 * <u>Descrizione dei parametri</u><br>
 * <b>[SERVER]</b> Nome del server smtp. Se non specificato verr� utilizzato quello presente nel file di configurazione. <br>
 * <b>[PORTA]</b> Numero della porta. Se non specificata verr� utilizzata quella presente nel file di configurazione. <br>
 * <b>[LOGIN]</b>              <br>
 * <b>[PASSWORD]</b> Password in chiaro. Se � specificata anche la password criptata, questo campo 
 *                   non verr� preso in considerazione e verr� utilizzata la password criptata.<br>
 * <b>[C_PASSWORD]</b> Password criptata. Si suppone che l'algoritmo di criptazione � quello 
 *                     utilizzato dalla classe <code>it.finmatica.jfc.authentication.Cryptable</code><br>
 * <b>[MITTENTE]</b> Nominativo del mittente.<br>
 * <b>[MITTENTE_IND]</b> Indirizzo del mittente.<br>
 * <b>[OGGETTO]</b> Oggetto della mail.<br>
 * <b>[TESTO]</b> Testo della mail.<br>
 * <b>[LISTA_DEST_IND]</b> Lista degli indirizzi dei destinatari. Gli indirizzi vanno separati dal carattere ";"<br>
 * <b>[LISTA_CC_IND]</b> Lista degli indirizzi dei destinatari in CC. Gli indirizzi vanno separati dal carattere ";"<br>
 * <b>[LISTA_BCC_IND]</b> Lista degli indirizzi dei destinatari in BCC. Gli indirizzi vanno separati dal carattere ";"<br><br>
 * 
 * 
 * @author       Antonio
 * @author       Sergio
 * @version      1.0
 *              
 * @see          Inoltro
 */

public class InoltroMail extends Inoltro {
//  private DateUtility du;            

  // Queste costanti rappresentano le posizioni dei rispettivi campi 
  // all'interno del vettore MARCATORI_DI_CAMPO[]  
  public final static int MCA_PORTA = 0;   
  public final static int MCA_SERVER = 1;
  public final static int MCA_LOGIN = 2;
  public final static int MCA_PASSWORD = 3;
  public final static int MCA_MITTENTE = 4;
  public final static int MCA_MITTENTE_IND = 5;
  public final static int MCA_OGGETTO = 6;
  public final static int MCA_TESTO = 7;
  public final static int MCA_LISTA_DEST_IND = 8;
  public final static int MCA_LISTA_CC_IND = 9;
  public final static int MCA_LISTA_BCC_IND = 10;
  public final static int MCA_NOME_FILE = 11;
  private static Logger logger = Logger.getLogger("it.finmatica.modulistica.inoltro.InoltroMail");

  public final static String MARCATORI_DI_CAMPO[] = {
     "[PORTA]",
     "[SERVER]",
     "[LOGIN]",
     "[PASSWORD]",
     "[MITTENTE]",
     "[MITTENTE_IND]",
     "[OGGETTO]",
     "[TESTO]",
     "[LISTA_DEST_IND]",
     "[LISTA_CC_IND]",
     "[LISTA_BCC_IND]",
     "[NOME_FILE]"
  };
  
  public final static String MARCATORE_DI_CRYPTING = "[_C_]"; 
                           
  /**
   * Costruttore vuoto necessario per invocare la Class.forName()
   * in tutte le classi che intendono allocare a runtime questo oggetto 
   * di inoltro ma che conoscono la sola interfaccia Inoltro.
   */
  public InoltroMail() {
  }


  /**
   * Metodo di inizializzazione. 
   * Vengono indicati tutti i parametri di inoltro.
   * Per questa particolare tipologia di inoltro i marcatori di campo sono stabiliti a priori.
   * Queste inizializzazioni avvengono attraverso l'invocazione del metodo 
   * <code>init(parametri, marcatoriDiCampo)</code> della classe base <code>Inoltro</code>
   * 
   * @author       Antonio
   * @version      1.0
   * @see          Inoltro
   * @param        parametri Stringa contenente i parametri di inoltro in forma codificata.
   * @param        OggettoXML Oggetto xml da allegare al messaggio.
   * @since        1.0
   * @return       void
   */
  public void init(String parametri) {
    init(parametri, MARCATORI_DI_CAMPO);
    try {
      setMarcatoreDiCrypting(MARCATORE_DI_CRYPTING);
    } catch (BadCryptingMarkerException e) {
      writeLog(e, null);
    }
  }

  /**
   * Metodo per l'inoltro effettivo della mail. Viene allegato l'oggetto XML 
   * indicato in fase di creazione.
   * 
   * @author       Antonio
   * @since        1.0
   * @return       void
   */
  public boolean doInoltro(OggettoXML oggettoXML) {
    Hashtable       hashParams = null;
    GenericMessage  gm;
    String          p_server, 
                    p_porta, 
//                    p_login, 
//                    p_password, 
                    p_mittente,
                    p_mittente_ind, 
                    p_oggetto, 
                    p_testo,
                    p_lista_dest_ind, 
                    p_lista_cc_ind, 
                    p_lista_bcc_ind,
                    p_nome_file;
    StringTokenizer st;
    int             num, j;
    
    // "Tiro su" i parametri in forma umana (hashtable)
    try {

      hashParams = getHashtableParametri();

    } catch (BadInitException e1) {
      writeLog(e1, null);
      return false;
    }

    p_server         = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_SERVER]);
    p_porta          = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_PORTA]);
//    p_login          = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_LOGIN]);
//    p_password       = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_PASSWORD]);
    p_mittente       = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_MITTENTE]);
    p_mittente_ind   = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_MITTENTE_IND]);
    p_oggetto        = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_OGGETTO]);
    p_testo          = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_TESTO]);
    p_lista_dest_ind = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_LISTA_DEST_IND]);
    p_lista_cc_ind   = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_LISTA_CC_IND]);
    p_lista_bcc_ind  = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_LISTA_BCC_IND]);
    p_nome_file      = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_NOME_FILE]);

    try {
      gm = Creator.create("mail");

    } catch (Exception e) {
      gm = null;
      writeLog(null,"InoltroMail::doInoltro() - Attenzione: errore in fase di creazione del GenericMessage.");
      errorMessage = "<div id='_gdm_error_small' style='display: block'>";
      errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_ext\").style.display =\"block\";";
      errorMessage += "document.getElementById(\"_gdm_error_small\").style.display =\"none\";'>";
      errorMessage += e.toString()+"</a></div>";
      errorMessage += "<div id='_gdm_error_ext' style='display: none'>";
      errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_small\").style.display =\"block\";";
      errorMessage += "document.getElementById(\"_gdm_error_ext\").style.display =\"none\";'>";
      errorMessage += e.toString()+"</a>";
      StackTraceElement[] st1 = e.getStackTrace();
      for (int i = 0;i < st1.length; i++) {
        errorMessage += "<br/>"+st1[i].toString();
      }
      errorMessage += "</div>";
      return false;
    }


    // Se si specifica un altro server, cambiare quello standard.
    if (p_server != null) {
      try {
        gm.setParam("TransportHost",p_server);

      } catch (Exception e) {
        writeLog(e,"InoltroMail::doInoltro() - Attenzione: il parametro TransportHost non � modificabile.");
        errorMessage = "<div id='_gdm_error_small' style='display: block'>";
        errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_ext\").style.display =\"block\";";
        errorMessage += "document.getElementById(\"_gdm_error_small\").style.display =\"none\";'>";
        errorMessage += e.toString()+"</a></div>";
        errorMessage += "<div id='_gdm_error_ext' style='display: none'>";
        errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_small\").style.display =\"block\";";
        errorMessage += "document.getElementById(\"_gdm_error_ext\").style.display =\"none\";'>";
        errorMessage += e.toString()+"</a>";
        StackTraceElement[] st1 = e.getStackTrace();
        for (int i = 0;i < st1.length; i++) {
          errorMessage += "<br/>"+st1[i].toString();
        }
        errorMessage += "</div>";
        return false;
      }
    }

    // Se si specifica un'altra porta, cambiare quella standard.
    if (p_porta != null) {
      try {
        gm.setParam("TransportPort",p_porta);

      } catch (Exception e) {
        writeLog(e,"InoltroMail::doInoltro() - Attenzione: il parametro TransportPort non � modificabile.");
        errorMessage = "<div id='_gdm_error_small' style='display: block'>";
        errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_ext\").style.display =\"block\";";
        errorMessage += "document.getElementById(\"_gdm_error_small\").style.display =\"none\";'>";
        errorMessage += e.toString()+"</a></div>";
        errorMessage += "<div id='_gdm_error_ext' style='display: none'>";
        errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_small\").style.display =\"block\";";
        errorMessage += "document.getElementById(\"_gdm_error_ext\").style.display =\"none\";'>";
        errorMessage += e.toString()+"</a>";
        StackTraceElement[] st1 = e.getStackTrace();
        for (int i = 0;i < st1.length; i++) {
          errorMessage += "<br/>"+st1[i].toString();
        }
        errorMessage += "</div>";
        return false;
      }
    }

    // Inizia il riempimento della mail
    try {
      // creazione sender
      Sender sender = new Sender();     
      Contact cSender = new Contact();    
      cSender.setName(p_mittente);
      cSender.setEmail(p_mittente_ind);
      sender.setContact(cSender);
      gm.setSender(sender);
  
      // creazione contatti lista destinatari. Deve esistere almeno un destinatario.
      if (p_lista_dest_ind == null) {
        writeLog(null,"InoltroMail::doInoltro() - Attenzione: � necessario almeno un destinatario !");
        return false;
      }
      
      st = new StringTokenizer(p_lista_dest_ind,";");
      num = st.countTokens();
      j = 0;
      Contact contact_dest[] = new Contact[num];   
      while (st.hasMoreTokens()) {
        contact_dest[j] = new Contact();
        contact_dest[j].setEmail(st.nextToken());
        gm.addRecipient(contact_dest[j]);
        j++;
      }

      // creazione contatti lista destinatari per conoscenza  
      if (p_lista_cc_ind != null) {
        st = new StringTokenizer(p_lista_cc_ind,";");
        num = st.countTokens();
        j = 0;
        Contact contact_dest_cc[] = new Contact[num];   
        while (st.hasMoreTokens()) {
          contact_dest_cc[j] = new Contact();
          contact_dest_cc[j].setEmail(st.nextToken());
          gm.addCc(contact_dest_cc[j]);
          j++;
        }
      }
     
      // creazione contatti lista destinatari nascosti  
      if (p_lista_bcc_ind != null) {
        st = new StringTokenizer(p_lista_bcc_ind,";");
        num = st.countTokens();
        j = 0;
        Contact contact_dest_bc[] = new Contact[num];   
        while (st.hasMoreTokens()) {
          contact_dest_bc[j] = new Contact();
          contact_dest_bc[j].setEmail(st.nextToken());
          gm.addBcc(contact_dest_bc[j]);
          j++;
        }
      }

      // creazione Oggetto
      gm.setSubject(p_oggetto);

      // creazione testo
      gm.setText(p_testo);

      // Aggiunta allegato file xml
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      try {
 
        oggettoXML.writeStream(os);

      } catch (Exception e2) {
        writeLog(e2, "InoltroMail::doInoltro() - Attenzione: non riesco a scrivere sull'OutputStream");
        return false;
      }
     
      if ( p_nome_file == null) {
        p_nome_file = "_default";
      }
      p_nome_file +="[" + DateUtility.getTodayStringDate("yyyy-MM-dd HH-mm-ss") + "]"+".xml";

      Attachment baisAtt = new Attachment(new ByteArrayInputStream(os.toByteArray()), p_nome_file); 

      gm.addAttachment(baisAtt);

      gm.send();

    } catch (Exception e3) {
      writeLog(e3, "InoltroMail::doInoltro() - Attenzione: non sono riuscito a spedire il messaggio");
      return false;
    }

    return true;
  }


  /**
   * Metodo unico per il logging.
   * Quando si individuer� una modalit� standard di logging baster� modificare solo questo metodo.
   * 
   * @param        e Exception che ha generato l'errore. Pu� essere null.
   * @param        messaggio Un messaggio aggiuntivo.
   * @since        1.0
   * @return       void
   */
  private void writeLog(Exception e, String messaggio) {
    if (e != null) 
      logger.error(messaggio,e);
  }

} // Class InoltroMail

