package it.finmatica.modutils.modificablocchi;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import it.finmatica.modutils.attributiblocco.AttributiBlocco;
import it.finmatica.modutils.attributicampoblocco.AttributiCampoBlocco;
import it.finmatica.modutils.informazioniblocco.InformazioniBlocco;
import it.finmatica.modutils.informazionicampo.InformazioniCampo;

public class ModificaBlocchi {

  public ModificaBlocchi() {
  }

  /**
   * 
   */
  public String sostituisciTagAds(String oldHtml) {
   
    String newHtml = "";
//    String sHTML = "";     
    
    newHtml = tagToCampi(oldHtml);
    
    oldHtml = newHtml;    

    newHtml = tagToSorter(oldHtml);
    
    oldHtml = newHtml;    
        
    newHtml = tagBlocchi(oldHtml);
    
    oldHtml = newHtml;    
        
    String inizio = "<div type='inizio' title='Inizio corpo' "+
            "style='BORDER-RIGHT: medium none; BORDER-TOP: medium none; BORDER-LEFT: medium none; BORDER-BOTTOM: 1px solid' align='center'>Inizio corpo</div>";	

    newHtml = oldHtml.replaceAll("<p><a href=\"#inizio\">--- Inizio corpo ---</a></p>",inizio);

    oldHtml = newHtml;

    String fine = "<div type='fine' title='Fine corpo' "+
            "style='BORDER-RIGHT: medium none; BORDER-TOP: 1px solid; BORDER-LEFT: medium none; BORDER-BOTTOM: medium none'"+
            " align='center'>Fine corpo</div>";	

    newHtml = oldHtml.replaceAll("<p><a href=\"#fine\">--- Fine corpo ---</a></p>",fine);

    oldHtml = newHtml;

    String elimina = "<input type='button' class='AFCButton' title='Elimina' size='8' name='tagelimina' value='Elimina'/>";

    newHtml = oldHtml.replaceAll("<a href=\"#tagelimina\">&lt;X&gt;</a>",elimina);

    return newHtml;

  }

  /**
   * 
   */
  public String ripristinaTagAds(String pOldHtml) {
    String newHtml = "";
    String oldHtml = "";
    String sHTML = "";
    String adsFormat = "";
    int i,j,k;
    AttributiCampoBlocco ac = null;

    oldHtml = pOldHtml;

    // 1. Ripristino i tag di inizio e fine corpo
    
    int ib = oldHtml.indexOf("<div");
    int ib2 = 0;
    int jb = 0;
    int kb = 0;
    while (ib > -1) {
      if (jb < ib) {
        newHtml += oldHtml.substring(jb,ib);
      }
      jb = oldHtml.indexOf("</div>",ib);
      if (jb > -1) {
        jb = jb + 6;
      }
      ib2 = oldHtml.indexOf("<div",ib+1);
      if (ib2 < jb && ib2 > -1) {
        ib = ib2;
      } else {
        kb = oldHtml.indexOf("type=\"inizio\"",ib);
        if ((kb > ib) && (kb < jb)) {
          sHTML = oldHtml.substring(ib,jb);          
            adsFormat = "<p><a href=\"#inizio\">--- Inizio corpo ---</a></p>";
        } else {
           kb = oldHtml.indexOf("type=\"fine\"",ib);
           if ((kb > ib) && (kb < jb)) {
             sHTML = oldHtml.substring(ib,jb);          
             adsFormat = "<p><a href=\"#fine\">--- Fine corpo ---</a></p>";
           } else {        
             adsFormat = oldHtml.substring(ib,jb);
           }
        }  
        newHtml += adsFormat;
        ib = oldHtml.indexOf("<div",jb);
      }
    }
    
    newHtml += oldHtml.substring(jb);

   // 2. Ripristino i tag delle etichette di ordinamento

    oldHtml = newHtml;

    newHtml = "";
    ib = oldHtml.indexOf("<a ");
    ib2 = 0;
    jb = 0;
    kb = 0;
    while (ib > -1) {
      if (jb < ib) {
        newHtml += oldHtml.substring(jb,ib);
      }
      jb = oldHtml.indexOf("</a>",ib);
      if (jb > -1) {
        jb = jb + 4;
      }
      ib2 = oldHtml.indexOf("<a ",ib+1);
      if (ib2 < jb && ib2 > -1) {
        ib = ib2;
      } else {
        String sorter = oldHtml.substring(ib,jb);     
        kb = oldHtml.indexOf("href=\"#sorter\"",ib);
        if ((kb > ib) && (kb < jb)) {
          String campo = "";
          String descr = "";

          int i1 = (sorter.indexOf("id=\""));
			    if (i1 > -1){
            int i2 = (sorter.indexOf("\"",i1+4));
			      int i21 = (sorter.indexOf(">",i1+4));
		        int i3 = (sorter.indexOf("</a>",i21+1));
			      campo = sorter.substring(i1+4,i2);
		        descr = sorter.substring(i21+1,i3);        
 			    }
          adsFormat = "<a class=\"AFCSorterLink\" href=\"#sorter\">&lt;<!-- "+campo+" -->"+descr+"&gt;</a>";
        } else {
          adsFormat = oldHtml.substring(ib,jb);
        }
        newHtml += adsFormat;
        ib = oldHtml.indexOf("<a ",jb);
        System.out.println("ib= " + ib);   
      }  
    }   
    newHtml += oldHtml.substring(jb);

    // 3. Infine rpristino i tag relativi al pulsante elimina e ai campi
 
    oldHtml = newHtml;
    newHtml = "";
     //Sostistuisco i campi di tipo INPUT
    i = oldHtml.indexOf("<input");
    j=0;
    while (i > -1) {
      newHtml += oldHtml.substring(j,i);
      j = oldHtml.indexOf("/>",i);
      if (j > -1) {
        j = j + 2;
        sHTML = oldHtml.substring(i,j);

        k = sHTML.indexOf("type=\"button\"");      
        if (k > -1) {
          adsFormat = "<a href=\"#tagelimina\">&lt;X&gt;</a>";
        }else{
          try {
            ac = new AttributiCampoBlocco(sHTML);
            adsFormat = ac.getAdsFormat();
          } catch (Exception e) {
            adsFormat = "[Errore: "+sHTML+" ]";
            System.out.println("Errore ripristino tag ads: "+e.toString());
          }
        }  
        newHtml += adsFormat;
        i = oldHtml.indexOf("<input",j);
      } else {
        i = -1;
      }
    }

    newHtml += oldHtml.substring(j);
   
    // 4. Infine rpristino i tag relativi ai blocchi nested
    
    oldHtml = newHtml;
    newHtml = "";
    int jlength = 0;
    int inizio = 0, fine = 0;
    int iblocco = oldHtml.indexOf("tagblocco");
    while ((iblocco != -1) && (!oldHtml.equals(""))) {
      jlength = oldHtml.length();
      try {
        inizio = cercaDivIn(oldHtml,iblocco);
        fine = cercaDivFi(oldHtml,inizio);
        newHtml += oldHtml.substring(0,inizio);
        sHTML = oldHtml.substring(inizio,fine);
        AttributiBlocco ab = new AttributiBlocco(sHTML);
        adsFormat = ab.getAdsFormat();
      } catch (Exception e) {
        adsFormat = "[Errore: "+sHTML+" ]";
        System.out.println("Errore ripristino tag ads: "+e.toString());
      }
      newHtml += adsFormat;
      oldHtml = oldHtml.substring(fine,jlength);
      iblocco = oldHtml.indexOf("tagblocco");
    }
    newHtml += oldHtml;
    
    return newHtml;
  
  }

  private int cercaDivIn(String pHtml, int posTag) throws Exception {
    int retval = -1;
    int i = 0;
    while (i > -1) {
      i = pHtml.indexOf("<div",retval+1);
      if ((i > -1) && (i < posTag)) {
        retval = i;
      } else {
        i = -1;
      }
    }
    if (retval == -1) {
      throw new Exception ("Tag div iniziale non trovato!");
    }
    return retval;
  }

  private int cercaDivFi(String pHtml, int posDiv) throws Exception {
    int retval = -1;
    int j = pHtml.indexOf("</div>",posDiv);
    int i = pHtml.indexOf("<div",posDiv+1);
    retval = j;
    while ((i > -1) && (i < j)) {
      retval = j;
      i = pHtml.indexOf("<div",i+1);
      j = pHtml.indexOf("</div>",j+1);
    }
    if (retval == -1) {
      throw new Exception ("Tag div finale non trovato!");
    }
    return retval+6;
  }

  /**
   * 
   */
  private String tagBlocchi(String modello) {
    int               j, jblocco, jlength, k, endChar=-1;
    String            subModello, bloccoHtml = "";
    String            sHtml = "";
    String            fineChr = "&gt;";
   
    subModello = modello;
    j = 0;

    while (j != -1) {
      jlength = subModello.length();
      jblocco  = subModello.indexOf("&lt;<!-- ADSTBB -->");
      
      if (jblocco == -1) {
        jblocco = jlength;
      }

      if (jblocco != jlength) {
        j = jblocco;
        //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
        k = subModello.indexOf("<!-- ADSTBE -->",j);
        if (k != -1) {
          sHtml = "";
          endChar = k + 15 + fineChr.length();
          sHtml = subModello.substring(j, endChar);
          try {
            InformazioniBlocco infoBlk = new InformazioniBlocco(sHtml);

            String areaBlocco = infoBlk.getAreaBlocco();
            String blocco = "";
            String sBlocco = "";
            if (areaBlocco.equalsIgnoreCase("")){
              sBlocco = infoBlk.getBlocco();
              blocco = sBlocco;
            } else {
              sBlocco =  "*" + infoBlk.getBlocco();
              blocco = areaBlocco + sBlocco;
            }
            
            String myId = "@"+blocco+
                          "@"+infoBlk.getNumeroRecord()+
                          "@"+infoBlk.getLegame()+
                          "@"+infoBlk.getOrdinamento()+
                          "@"+infoBlk.getAggiungi();
            //Calcolo ID
            
            bloccoHtml = "<div type=\"tagblocco\" id=\""+myId;
            bloccoHtml += "@\" title=\"[ Blocco "+infoBlk.getBlocco()+" ]\" "+
              "style=\"BORDER-RIGHT: 1px solid; BORDER-TOP: 1px solid; BORDER-LEFT: 1px solid; BORDER-BOTTOM: 1px solid; BACKGROUND-COLOR: #ededed\" align=\"center\" />"+
              "<br/>[ Blocco "+sBlocco+" ]<br/><br/></div>";
          } catch(Exception ee) {
            System.out.println("Errore: "+ee.toString());
            subModello = subModello.substring(0,jblocco) + "[Errore modifica tab pagina ]" + subModello.substring(endChar);
          }
          subModello = subModello.substring(0,jblocco) + bloccoHtml + subModello.substring(endChar);
        } else {
          // ERRORE: Manca il tag di fine campo
          // Blocco la ricerca
          j = -1;
        }
      } else {
        // NON vi sono campi di input, per cui costruisco
        // l'ultimo IElementoModello che è sempre un TESTO
        j = -1;  // Blocco la ricerca
      }
    }
    return subModello;
  }


  /**
   * 
   */
  private String tagToCampi(String blocco) {
    // Ciclo di lettura e interpretazione del blocco:
    //    --> estrapolazione e creazione di tutti i campi
    int               j, jfield, jlength, k;
    String            subBlocco, campoHtml = "";
   
    subBlocco = blocco;
    j = 0;

    while (j != -1) {
      jlength = subBlocco.length();
      //jfield  = subBlocco.indexOf("&lt;<!-- ADSTFB -->");
      jfield  = subBlocco.indexOf("<a href=\"#taglayout\">");
      if (jfield == -1) {
        jfield  = subBlocco.indexOf("<a href=\"#tagfunc\">");
      }
      if (jfield == -1) {
        jfield = jlength;
      }
      if (jfield != jlength) {
        j = jfield;
        //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
        k = subBlocco.indexOf("<!-- ADSTFE -->",j);
        if (k != -1) {
          // ---> Da j a k+NAMEFIELDEND.length() è un CAMPO
          int startCh = 0,endChar;
//          String sHtml = "";
          startCh = k + 15;
          endChar = startCh;
          try {
/*            if ((startCh + 10) <= jlength) {
              sHtml = subBlocco.substring(startCh,startCh + 10);
              if (sHtml.equalsIgnoreCase("<!-- style") ||
                  sHtml.equalsIgnoreCase("&gt;<!-- s")) {
                endChar = subBlocco.indexOf("-->",startCh);
                if (endChar != -1) {
                  endChar += 3;
                } else {
                  endChar = startCh;
                }
              }

              if (sHtml.equalsIgnoreCase("<!-- link=") ||
                  sHtml.equalsIgnoreCase("&gt;<!-- l")) {
                endChar = subBlocco.indexOf("-->",startCh);
                if (endChar != -1) {
                  endChar += 3;
                } else {
                  endChar = startCh;
                }
              }

              if (sHtml.equalsIgnoreCase("<!-- func=") ||
                  sHtml.equalsIgnoreCase("&gt;<!-- f")) {
                endChar = subBlocco.indexOf("-->",startCh);
                if (endChar != -1) {
                  endChar += 3;
                } else {
                  endChar = startCh;
                }
              }
              
            } else {
              endChar = startCh;
            }
            String fineChr = "&gt;";
            if (subBlocco.indexOf(fineChr,k) == endChar) {
              endChar = endChar+fineChr.length();
            } */
            endChar = subBlocco.indexOf("</a>",k);
            endChar = endChar + 4;
            campoHtml = subBlocco.substring(j, endChar);
            InformazioniCampo infoC = new InformazioniCampo(campoHtml,"ADSFORMAT -->");
            campoHtml = "<input type='text' class='"+infoC.getClassName()+"' style='"+infoC.getStile()+"' ";
            campoHtml += "title='"+infoC.getNomeDato()+"' size='"+infoC.getLunghezza()+"' "; 
            campoHtml += "name=\""+infoC.getMetaData()+"\" value='"+infoC.getNomeDato()+"'/>";
         } catch(Exception ee) {
            System.out.println("Errore: "+ee.toString());
            subBlocco = subBlocco.substring(0,jfield) + "[Errore modifica campo ]" + subBlocco.substring(endChar);
          }
          // Mi resta da analizzare tutto quanto si trova dopo il TAGFIELDEND
          subBlocco = subBlocco.substring(0,jfield) + campoHtml + subBlocco.substring(endChar);
        } else {
          // ERRORE: Manca il tag di fine campo
          // Blocco la ricerca
          j = -1;
        }
      } else {
        // NON vi sono campi di input, per cui costruisco
        // l'ultimo IElementoModello che è sempre un TESTO
        j = -1;  // Blocco la ricerca
      }
    }
    return subBlocco;
  }

  private String tagToSorter(String blocco){
    int               j, jsorter, jlength, k, jsortertag;
    String            subBlocco, sorterHtml = "", subApp;
   
    subBlocco = blocco;
    j = 0;

    while (j != -1) {
 
      jlength = subBlocco.length();
    
      jsorter  = subBlocco.indexOf("href=\"#sorter\"",j);          
      if (jsorter == -1) {  
        jsorter = jlength;
      }
      if (jsorter != jlength) {
        subApp = subBlocco.substring(0,jsorter);
        jsortertag = subApp.lastIndexOf("<a ");
        jsorter = jsortertag;
        j = jsorter;
        k = subBlocco.indexOf("</a>",j);
        if (k > -1){       
          String sSorter = subBlocco.substring(j,k+4);   
          int i1 = (sSorter.indexOf("<!-- "));                 
		      int i2 = (sSorter.indexOf(" -->",i1+1));       
          if (i1 > -1 && i2 > -1){
		        int i3 = (sSorter.indexOf("&gt;",i2+4));     
            if (i3 > -1){
              String campo = sSorter.substring(i1+5,i2);
		          String descr = sSorter.substring(i2+4,i3);
              sorterHtml = "<a class='AFCSorterLink' href='#sorter' id='"+campo+"' type=\"ordinaBlocco(this,'"+campo+"')\">"+descr+"</a>";
              subBlocco = subBlocco.substring(0,jsorter) + sorterHtml + subBlocco.substring(k+4);
            }  
          }
          j = subBlocco.indexOf("</a>",jsorter);
        }else{
          j = -1;
        }  
      }else{
        j = -1;
      }       
    } 
    return subBlocco;        
  }
  
  public static void main(String[] args) {
	  try {
		  FileInputStream fis = new FileInputStream("c:/temp/blocco.html");
		  ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		    int val; 
		    while ((val=fis.read()) != -1) { 
		        byteStream.write(val);
		    }

			String blocco = new String(byteStream.toByteArray());
			ModificaBlocchi m = new ModificaBlocchi();
			String nuovo = m.sostituisciTagAds(blocco);
			System.out.println(m.ripristinaTagAds(nuovo));
		  
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
	  
 
  }
}