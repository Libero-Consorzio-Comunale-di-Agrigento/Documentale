package it.finmatica.dmServer.modulistica;

import it.finmatica.dmServer.util.ManageConnection;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.dmServer.Environment;
import java.sql.*;
import org.dom4j.*;

public class InformazioniModulistica {
  Environment en;
  IDbOperationSQL  dbOp;
  private boolean bIsNew=false;  

  public InformazioniModulistica(String utente, String ruolo, String inifile) throws Exception {
     en = new Environment(utente,utente,
                          "MODULISTICA", 
                          "MODULISTICA", 
                          null, inifile);
     en.setRuolo(ruolo);
  }

  public InformazioniModulistica(String utente, String ruolo, Connection cn) throws Exception {
     en = new Environment(utente,utente,
                          "MODULISTICA", 
                          "MODULISTICA", 
                          null, cn);
     en.setRuolo(ruolo);
  }

  private IDbOperationSQL connect() throws Exception {
        if (en.getDbOp()==null) {
           bIsNew=true;
           return (new ManageConnection(en.Global)).connectToDB();
        }
        
        return en.getDbOp();
  }
  
  private void close() throws Exception {
        if (bIsNew) (new ManageConnection(en.Global)).disconnectFromDB(dbOp,true,false);        
  }

  public Document getAree() throws Exception {
    ResultSet       rst = null;
    String          query;
    String          area = "", descrizione = "";
    Element         root, el;
    root = DocumentHelper.createElement("AREE");
    Document dDoc = DocumentHelper.createDocument();
    dDoc.setRootElement(root);
          
    query = "SELECT AREA, DESCRIZIONE "+
             "FROM AREE "+
             "ORDER BY AREA ASC";
    try {
      dbOp = connect();
      dbOp.setStatement(query);     
      dbOp.execute();
      rst = dbOp.getRstSet();

      while (rst.next()) { 
        area = rst.getString("AREA");
        descrizione = rst.getString("DESCRIZIONE");
        if (descrizione == null) {
          descrizione = "";
        }
        el = DocumentHelper.createElement(area);
        el.setText(descrizione);
        root.add(el);
        
      }
      
      close();
      return dDoc;
    } catch (Exception e) {
      close();
      throw new Exception("InformazioniModulistica::getAree" + e.getMessage());
    }
  }  

  public String getAreeAsXml() throws Exception {
    Document dAree = getAree();
    return dAree.asXML();
  }  

  /**
   * La funzione ritorna un documento XML contenente la lista dei modelli.
   * 
   * @param Area Area dei modelli dicui si vuole estrarre le informazioni
   * @param sComp Competenza che l'utente deve avere sul modello (L = lettura, 
   * U = modifica, C = creazione, T = tutti)
   * @author Marco Bonforte
   * @version 1.0
   */
   
  public Document getModelli(String area, String sComp) throws Exception {
    ResultSet       rst = null;
    String          query;
    String          competenze = "";
    String          cm = "", idTipodoc = "", cmp = "", valido = "";
    Element         root, elm, eld;
    root = DocumentHelper.createElement("MODELLI");
    Document dDoc = DocumentHelper.createDocument();
    dDoc.setRootElement(root);

    if (sComp.equalsIgnoreCase("T")) {
      competenze += "( GDM_COMPETENZA.GDM_VERIFICA('TIPI_DOCUMENTO',NVL(M1.ID_TIPODOC,M2.ID_TIPODOC),'L','"+en.getUser()+"','"+en.getRuolo()+"',TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1 OR ";
      competenze += "GDM_COMPETENZA.GDM_VERIFICA('TIPI_DOCUMENTO',NVL(M1.ID_TIPODOC,M2.ID_TIPODOC),'U','"+en.getUser()+"','"+en.getRuolo()+"',TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1 OR ";
      competenze += "GDM_COMPETENZA.GDM_VERIFICA('TIPI_DOCUMENTO',NVL(M1.ID_TIPODOC,M2.ID_TIPODOC),'C','"+en.getUser()+"','"+en.getRuolo()+"',TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1 OR ";
      competenze += "GDM_COMPETENZA.GDM_VERIFICA('TIPI_DOCUMENTO',NVL(M1.ID_TIPODOC,M2.ID_TIPODOC),'D','"+en.getUser()+"','"+en.getRuolo()+"',TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1 OR ";
      competenze += "GDM_COMPETENZA.GDM_VERIFICA('TIPI_DOCUMENTO',NVL(M1.ID_TIPODOC,M2.ID_TIPODOC),'X','"+en.getUser()+"','"+en.getRuolo()+"',TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1 OR ";
      competenze += "GDM_COMPETENZA.GDM_VERIFICA('TIPI_DOCUMENTO',NVL(M1.ID_TIPODOC,M2.ID_TIPODOC),'M','"+en.getUser()+"','"+en.getRuolo()+"',TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1) ";
    } else {
      competenze += "GDM_COMPETENZA.GDM_VERIFICA('TIPI_DOCUMENTO',NVL(M1.ID_TIPODOC,M2.ID_TIPODOC),'"+sComp+"','"+en.getUser()+"','"+en.getRuolo()+"',TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1 ";
    }
          
    query = "SELECT M1.CODICE_MODELLO, M1.ID_TIPODOC, "+
                  " M1.CODICE_MODELLO_PADRE, M1.VALIDO "  +   
             " FROM MODELLI M1, MODELLI M2 "+
            " WHERE M1.AREA = '"+area+"' "+
              " AND M1.AREA = M2.AREA(+) AND M1.CODICE_MODELLO_PADRE = M2.CODICE_MODELLO(+) "+
              " AND "+ competenze+
              "ORDER BY 1 ASC";

    try {
      dbOp = connect();
      dbOp.setStatement(query);     
      dbOp.execute();
      rst = dbOp.getRstSet();

      while (rst.next()) { 
        cm        = rst.getString("CODICE_MODELLO");
        idTipodoc = rst.getString("ID_TIPODOC");
        cmp       = rst.getString("CODICE_MODELLO_PADRE");
        valido    = rst.getString("VALIDO");
        if (idTipodoc == null) {
          idTipodoc = "";
        }
        if (cmp == null) {
          cmp = "";
        }
        if (valido == null) {
          valido = "";
        }
        elm = DocumentHelper.createElement(cm);
        eld = DocumentHelper.createElement("ID_TIPODOC");
        eld.setText(idTipodoc);
        elm.add(eld);
        eld = DocumentHelper.createElement("CODICE_MODELLO_PADRE");
        eld.setText(cmp);
        elm.add(eld);
        eld = DocumentHelper.createElement("VALIDO");
        eld.setText(valido);
        elm.add(eld);
        root.add(elm);
      }
      
      close();
      return dDoc;
    } catch (Exception e) {
      close();
      throw new Exception("InformazioniModulistica::getModelli" + e.getMessage());
    }
  }  

  /**
   * La funzione ritorna una stringa in formato XML contenente la lista dei modelli.
   * 
   * @param Area Area dei modelli dicui si vuole estrarre le informazioni
   * @param sComp Competenza che l'utente deve avere sul modello (L = lettura, 
   * U = modifica, C = creazione, T = tutti)
   * @author Marco Bonforte
   * @version 1.0
   */
   
  public String getModelliAsXml(String area, String sComp) throws Exception {
    Document dMod = getModelli(area, sComp);
    return dMod.asXML();
  }  

  public Document getDatiArea(String area) throws Exception {
    ResultSet       rst = null;
    String          query;
    String          dato, label, tipo, lunghezza, decimali, formato,
                    dominio, formula, visualizza;
    Element         root, eld,elf;
    root = DocumentHelper.createElement("DATI");
    Document dDoc = DocumentHelper.createDocument();
    dDoc.setRootElement(root);
          
    query = "SELECT DATO, LABEL, TIPO, "+
            " LUNGHEZZA, DECIMALI, FORMATO_DATA, "+
            " DOMINIO, DOMINIO_FORMULA, DOMINIO_VISUALIZZA "+
            " FROM DATI "+
            "WHERE AREA = '"+area+"' "+ 
            "ORDER BY 1 ASC";
    try {
      dbOp = connect();
      dbOp.setStatement(query);     
      dbOp.execute();
      rst = dbOp.getRstSet();

      while (rst.next()) { 
        dato = rst.getString("DATO");
        eld = DocumentHelper.createElement(dato);

        label = rst.getString("LABEL");
        if (label == null) {
          label = "";
        }
        elf = DocumentHelper.createElement("LABEL");
        elf.setText(label);
        eld.add(elf);

        tipo = rst.getString("TIPO");
        if (tipo == null) {
          tipo = "";
        }
        elf = DocumentHelper.createElement("TIPO");
        elf.setText(tipo);
        eld.add(elf);

        lunghezza = rst.getString("LUNGHEZZA");
        if (lunghezza == null) {
          lunghezza = "";
        }
        elf = DocumentHelper.createElement("LUNGHEZZA");
        elf.setText(lunghezza);
        eld.add(elf);

        decimali = rst.getString("DECIMALI");
        if (decimali == null) {
          decimali = "";
        }
        elf = DocumentHelper.createElement("DECIMALI");
        elf.setText(decimali);
        eld.add(elf);

        formato = rst.getString("FORMATO_DATA");
        if (formato == null) {
          formato = "";
        }
        elf = DocumentHelper.createElement("FORMATO_DATA");
        elf.setText(formato);
        eld.add(elf);

        dominio = rst.getString("DOMINIO");
        if (dominio == null) {
          dominio = "";
        }
        elf = DocumentHelper.createElement("DOMINIO");
        elf.setText(dominio);
        eld.add(elf);

        formula = rst.getString("DOMINIO_FORMULA");
        if (formula == null) {
          formula = "";
        }
        elf = DocumentHelper.createElement("DOMINIO_FORMULA");
        elf.setText(formula);
        eld.add(elf);

        visualizza = rst.getString("DOMINIO_VISUALIZZA");
        if (visualizza == null) {
          visualizza = "";
        }
        elf = DocumentHelper.createElement("DOMINIO_VISUALIZZA");
        elf.setText(visualizza);
        eld.add(elf);

        root.add(eld);
        
      }
      
      close();
      return dDoc;
    } catch (Exception e) {
      close();
      throw new Exception("InformazioniModulistica::getDatiArea" + e.getMessage());
    }
  }  

  public String getDatiAreaAsXml(String area) throws Exception {
    Document dDoc = getDatiArea(area);
    return dDoc.asXML();
  }  

  public Document getDatiModello(String area, String cm) throws Exception {
    ResultSet       rst = null;
    String          query;
    String          val;
    Element         root, eld,elf;
    root = DocumentHelper.createElement("DATI");
    Document dDoc = DocumentHelper.createDocument();
    dDoc.setRootElement(root);
          
    query = "SELECT DATO, AREA_DATO, ID_CAMPO, "+
            " LUNGHEZZA, DECIMALI, TIPO_CAMPO, "+
            " TIPO_ACCESSO, CAMPO_CALCOLATO, BLOCCO, IN_USO "+
            " FROM DATI_MODELLO "+
            "WHERE AREA = '"+area+"' "+ 
            " AND CODICE_MODELLO = '"+cm+"' "+ 
            "ORDER BY 1 ASC";
    try {
      dbOp = connect();
      dbOp.setStatement(query);     
      dbOp.execute();
      rst = dbOp.getRstSet();

      while (rst.next()) { 
        val = rst.getString("DATO");
        eld = DocumentHelper.createElement(val);

        val = rst.getString("AREA_DATO");
        if (val == null) {
          val = "";
        }
        elf = DocumentHelper.createElement("AREA_DATO");
        elf.setText(val);
        eld.add(elf);

        val = rst.getString("ID_CAMPO");
        if (val == null) {
          val = "";
        }
        elf = DocumentHelper.createElement("ID_CAMPO");
        elf.setText(val);
        eld.add(elf);

        val = rst.getString("LUNGHEZZA");
        if (val == null) {
          val = "";
        }
        elf = DocumentHelper.createElement("LUNGHEZZA");
        elf.setText(val);
        eld.add(elf);

        val = rst.getString("DECIMALI");
        if (val == null) {
          val = "";
        }
        elf = DocumentHelper.createElement("DECIMALI");
        elf.setText(val);
        eld.add(elf);

        val = rst.getString("TIPO_CAMPO");
        if (val == null) {
          val = "";
        }
        elf = DocumentHelper.createElement("TIPO_CAMPO");
        elf.setText(val);
        eld.add(elf);

        val = rst.getString("TIPO_ACCESSO");
        if (val == null) {
          val = "";
        }
        elf = DocumentHelper.createElement("TIPO_ACCESSO");
        elf.setText(val);
        eld.add(elf);

        val = rst.getString("CAMPO_CALCOLATO");
        if (val == null) {
          val = "";
        }
        elf = DocumentHelper.createElement("CAMPO_CALCOLATO");
        elf.setText(val);
        eld.add(elf);

        val = rst.getString("BLOCCO");
        if (val == null) {
          val = "";
        }
        elf = DocumentHelper.createElement("BLOCCO");
        elf.setText(val);
        eld.add(elf);

        val = rst.getString("IN_USO");
        if (val == null) {
          val = "";
        }
        elf = DocumentHelper.createElement("IN_USO");
        elf.setText(val);
        eld.add(elf);

        root.add(eld);
        
      }
      
      close();
      return dDoc;
    } catch (Exception e) {
      close();
      throw new Exception("InformazioniModulistica::getDatiArea" + e.getMessage());
    }
  }  

  public String getDatiModelloAsXml(String area, String cm) throws Exception {
    Document dDoc = getDatiModello(area, cm);
    return dDoc.asXML();
  }  

  public static void main(String[] args) throws Exception {
    InformazioniModulistica im = new InformazioniModulistica("GDM","GDM","C:/Programmi/Apache Software Foundation/Tomcat 5.5/webapps/jgdm/config/gd4dm.properties");
    System.out.println(im.getDatiAreaAsXml("AD4"));
  }
}