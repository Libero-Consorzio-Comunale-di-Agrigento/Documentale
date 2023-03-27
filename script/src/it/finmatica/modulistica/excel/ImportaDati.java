package it.finmatica.modulistica.excel;

import it.finmatica.dmServer.management.ICartella;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.SOA.XMLError;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ImportaDati {
	private Workbook wkb = null;
	private int	numeroDiPagine = 0;
	private String fileExcell = "";
	private String errore = "";
	
	public ImportaDati(HttpServletRequest request, String file) {
		try {
  		String path = request.getSession().getServletContext().getRealPath("");
  		fileExcell = path+file;
  		wkb = Workbook.getWorkbook(new File(path+file));
  		numeroDiPagine = wkb.getNumberOfSheets();
		} catch (Exception e) {
			errore =  (new XMLError("Problemi durante l'operazione di caricamento dei documenti. \nErrore: "+e.getMessage())).getXML();
		}
	}
	public ImportaDati(String file) {
		try {
  		fileExcell = file;
  		wkb = Workbook.getWorkbook(new File(file));
  		numeroDiPagine = wkb.getNumberOfSheets();
  	} catch (Exception e) {
  		errore =  (new XMLError("Problemi durante l'operazione di caricamento dei documenti. \nErrore: "+e.getMessage())).getXML();
  	}
	}
	
	public String reportIniziale() {
		String retval = "";
		Element root, elp;

		if (errore.length() > 0) {
			return errore;
		}
		Document docOut = DocumentHelper.createDocument(); 
  	root = DocumentHelper.createElement("ROWS");
  	docOut.setRootElement(root);
  	for (int i=0; i < getNumeroPagine(); i++) {
  		elp = DocumentHelper.createElement("ROW");
      elp.addAttribute("num", ""+(i+1));
      aggFiglio(elp, "AREA", getAreaModello(i));
      aggFiglio(elp, "CODICE_MODELLO", getCodiceModello(i));
      aggFiglio(elp, "DOCUMENTI", ""+getRighePagina(i));
      aggFiglio(elp, "ELABORATI", "");
      aggFiglio(elp, "FALLITI", "");
      aggFiglio(elp, "ERRORI", "");
      root.add(elp);
  	}
  	retval = docOut.asXML();
		return retval;
	}
	
	public String caricaDocumenti(IDbOperationSQL dbOp, 
																String 					utente, 
																String 					creaLink, 
																String 					idCartella, 
																String 					wrksp, 
																String 					usaCR) {
		String retval = "";
		String errori = "";
		String area, cm, cr;
		String campo, valore;
		Element root, elp;
		Profilo p;
		int elaborati = 0,
				falliti = 0;	

		if (errore.length() > 0) {
			return errore;
		}
		Document docOut = DocumentHelper.createDocument(); 
  	root = DocumentHelper.createElement("ROWS");
  	docOut.setRootElement(root);
  	for (int i=0; i < getNumeroPagine(); i++) {
  		elaborati = 0;
  		falliti = 0;
  		errori = "";
  		elp = DocumentHelper.createElement("ROW");
      elp.addAttribute("num", ""+i);
      area = getAreaModello(i);
      cm = getCodiceModello(i);
      if (area.length() > 0 && cm.length() > 0) {
        for (int j=1; j <= getRighePagina(i); j++) {
        	if (usaCR.equalsIgnoreCase("S")) {
        		cr = wkb.getSheet(i).getCell(2, j).getContents();
        		p = new Profilo(cm, area, cr, utente, utente, "", dbOp.getConn());
        	} else {
        		p = new Profilo(cm, area, utente, utente, "", dbOp.getConn());
        	}
        	for (int k=3; k < getColonnePagina(i); k++) {
        		campo = wkb.getSheet(i).getCell(k, 0).getContents();
        		valore = wkb.getSheet(i).getCell(k, j).getContents();
        		p.settaValore(campo, valore);
        	}
        	if (p.salva()) {
        		elaborati++;
        		if (creaLink.equalsIgnoreCase("S")) {
        			ICartella ic;
							try {
								ic = new ICartella(idCartella);
          			ic.initVarEnv(utente, utente, dbOp.getConn());
          			ic.addInObject(p.getDocNumber(), "D");
          			ic.update();
							} catch (Exception e) {
								elaborati--;
								falliti++;
		        		errori += "Rigo: "+j+"\nDocumento non inserito in cartella\n"+e.getMessage()+"\n";
							}
        		}
        	} else {
        		falliti++;
        		errori += "Rigo: "+j+"\n"+p.getError()+"\n";
        	}
        	try {
        		dbOp.commit();
        	} catch (Exception e) {}
        }
      } else {
      	elaborati = 0;
      	falliti = getRighePagina(i);
      	errori = "Foglio "+wkb.getSheetNames()[i]+" non corretto\n";
      }
      
  		elp = DocumentHelper.createElement("ROW");
      elp.addAttribute("num", ""+i);
      aggFiglio(elp, "AREA", getAreaModello(i));
      aggFiglio(elp, "CODICE_MODELLO", getCodiceModello(i));
      aggFiglio(elp, "DOCUMENTI", ""+getRighePagina(i));
      aggFiglio(elp, "ELABORATI", ""+elaborati);
      aggFiglio(elp, "FALLITI", ""+falliti);
      aggFiglio(elp, "ERRORI", errori);
      root.add(elp);
  	}
  	retval = docOut.asXML();
		try {
			dbOp.close();
		} catch (Exception edb) {}
		File f = new File(fileExcell);
		f.delete();
		return retval;
}

	public int getNumeroPagine() {
		return numeroDiPagine;
	}
	
	public int getRighePagina(int pagina) {
		int righe = 0;
		if (pagina > numeroDiPagine) {
			return 0;
		}
		righe = wkb.getSheet(pagina).getRows();
		if (righe > 0) {
			righe--;
		} else {
			righe = 0;
		}
		return righe;
	}

	public int getColonnePagina(int pagina) {
		int colonne;
		if (pagina > numeroDiPagine) {
			return 0;
		}
		colonne = wkb.getSheet(pagina).getRow(0).length;
		return colonne;
	}
	
	public String getCodiceModello(int pagina) {
		String retval = "";
		if (pagina > numeroDiPagine) {
			return "";
		}
		try {
			retval = wkb.getSheet(pagina).getCell(1, 1).getContents();
		} catch (Exception e) {
			retval = "";
		}
		if (!retval.equals(wkb.getSheetNames()[pagina])) {
			retval = "";
		}
		return retval;
	}
	
	public String getAreaModello(int pagina) {
		String retval = "";
		Sheet s;
		Cell c;
		if (pagina > numeroDiPagine) {
			return "";
		}
		try {
			s = wkb.getSheet(pagina);
			c = s.getCell(0, 1);
  		if (c != null) {
  			retval = c.getContents();
  		}
		} catch (Exception e) {
			retval = "";
		}
		return retval;
	}
	
	private static Element aggFiglio(Element elp, String nome, String valore) {
    Element elf = DocumentHelper.createElement(nome);
    elf.setText(valore);
    elp.add(elf);
    return elf;
  }

  /**
   * 
   */
  public static void main(String[] args) {
  	IDbOperationSQL dbOp = null;
  	try {
  		SessioneDb.getInstance().addAlias("oracle.", "oracle.jdbc.driver.OracleDriver");
      dbOp = SessioneDb.getInstance().createIDbOperationSQL("oracle.", "jdbc:oracle:thin:@jvm-efesto:1521:orcl", "GDM", "GDM");
			ImportaDati id = new ImportaDati("c:/temp/prova2.xls");
			System.out.println("Numero di pagine: "+id.getNumeroPagine());
			System.out.println(id.reportIniziale());
			System.out.println(id.caricaDocumenti(dbOp, "GDM", "N", "", "", "S"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	
  }
}
