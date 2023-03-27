package it.finmatica.modutils.modificamodello;
//import java.sql.*; 
import java.io.*; 
//import java.util.*;
//import it.finmatica.jfc.io.*;
import it.finmatica.modutils.informazionicampo.InformazioniCampo;
import it.finmatica.modutils.informazioniTab.InformazioniTab;
import it.finmatica.modutils.informazioniblocco.InformazioniBlocco;
import it.finmatica.modutils.informazioniSettore.InformazioniSettore;
import it.finmatica.modutils.informazioniPopup.InformazioniPopup;
import it.finmatica.modutils.attributicampo.AttributiCampo;
import it.finmatica.modutils.attributiblocco.AttributiBlocco;
import it.finmatica.modutils.attributisettore.AttributiSettore;
import it.finmatica.modutils.attributitab.AttributiTab;
import it.finmatica.modutils.attributipopup.AttributiPopup;
//import javax.swing.text.Element;
//import javax.swing.text.html.HTMLDocument;
//import org.w3c.dom.html.HTMLInputElement;

public class ModificaModello {

  public ModificaModello() {
  }

  /**
   * 
   */
  public String sostituisciTagAds(String oldHtml) {
    String newHtml = "";
//    String sHTML = "";

    newHtml = tagToCampi(oldHtml);
    oldHtml = newHtml;
    newHtml = tagBlocchi(oldHtml);
    oldHtml = newHtml;
    newHtml = tagSettore(oldHtml);
    oldHtml = newHtml;
    newHtml = tagPagina(oldHtml);
    oldHtml = newHtml;
    newHtml = tagPopup(oldHtml);
    
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
    int i,j;
    AttributiCampo ac = null;
    AttributiPopup ap = null;

    oldHtml = pOldHtml;
    //Sostistuisco i campi di tipo INPUT
    i = oldHtml.indexOf("<input");
    j=0;
    while (i > -1) {
      newHtml += oldHtml.substring(j,i);
      j = oldHtml.indexOf("/>",i);
      if (j > -1) {
        j = j + 2;
        sHTML = oldHtml.substring(i,j);
        if (sHTML.indexOf("type=\"image\"") > 0) {
          try {
            ap = new AttributiPopup(sHTML);
            adsFormat = ap.getAdsFormat();
          } catch (Exception e) {
            adsFormat = "[Errore: "+sHTML+" ]";
            System.out.println("Errore ripristino tag ads: "+e.toString());
          }
        } else {
          if (sHTML.indexOf("type=\"button\"") > 0) {
            adsFormat = sHTML;
          } else {
            try {
              ac = new AttributiCampo(sHTML);
              adsFormat = ac.getAdsFormat();
            } catch (Exception e) {
              adsFormat = "[Errore: "+sHTML+" ]";
              System.out.println("Errore ripristino tag ads: "+e.toString());
            }
          }
        }
        newHtml += adsFormat;
        i = oldHtml.indexOf("<input",j);
      } else {
        i = -1;
      }
    }
    newHtml += oldHtml.substring(j);
    oldHtml = newHtml;
    newHtml = "";

    //Sostistuisco i campi di tipo textarea
    i = oldHtml.indexOf("<textarea");
    j=0;
    while (i > -1) {
      newHtml += oldHtml.substring(j,i);
      j = oldHtml.indexOf("</textarea>",i);
      if (j > -1) {
        j = j + 11;
        sHTML = oldHtml.substring(i,j);
        try {
          ac = new AttributiCampo(sHTML);
          adsFormat = ac.getAdsFormat();
        } catch (Exception e) {
          adsFormat = "[Errore: "+sHTML+" ]";
          System.out.println("Errore ripristino tag ads: "+e.toString());
        }
        newHtml += adsFormat;
        i = oldHtml.indexOf("<textarea",j);
      } else {
        i = -1;
      }
    }
    newHtml += oldHtml.substring(j);
    oldHtml = newHtml;
    newHtml = "";

    //Sostistuisco i campi di tipo select
    i = oldHtml.indexOf("<select");
    j=0;
    while (i > -1) {
      newHtml += oldHtml.substring(j,i);
      j = oldHtml.indexOf("</select>",i);
      if (j > -1) {
        j = j + 9;
        sHTML = oldHtml.substring(i,j);
        try {
          ac = new AttributiCampo(sHTML);
          adsFormat = ac.getAdsFormat();
        } catch (Exception e) {
          adsFormat = "[Errore: "+sHTML+" ]";
          System.out.println("Errore ripristino tag ads: "+e.toString());
        }
        newHtml += adsFormat;
        i = oldHtml.indexOf("<select",j);
      } else {
        i = -1;
      }
    }
    newHtml += oldHtml.substring(j);
    oldHtml = newHtml;

    
    
//------------------------------------------------------------------------------------    
    newHtml = "";
    int iblocco = 0;
    int ipagina = 0;
    int ifpagina = 0;
    int isettore = 0;
    int ifsettore = 0;
    j = 0;
    int jlength = 0;
    int inizio = 0, fine = 0;

    while ((j != -1) && (!oldHtml.equals(""))) {
      jlength = oldHtml.length();
      iblocco = oldHtml.indexOf("tagblocco");
      ipagina = oldHtml.indexOf("tagtabpage");
      ifpagina = oldHtml.indexOf("tagpagina");
      isettore = oldHtml.indexOf("tagvisual");
      ifsettore = oldHtml.indexOf("tagfinevisual");

      if (iblocco == -1) {
        iblocco = jlength;
      }

      if (ipagina == -1) {
        ipagina = jlength;
      }

      if (ifpagina == -1) {
        ifpagina = jlength;
      }
      
      if (isettore == -1) {
        isettore = jlength;
      }
      
      if (ifsettore == -1) {
        ifsettore = jlength;
      }
      
      if ((iblocco != jlength)     || 
          (ipagina != jlength)  || 
          (ifpagina != jlength)   || 
          (isettore != jlength)     || 
          (ifsettore != jlength)) {

        if ((iblocco < ipagina) && 
            (iblocco < ifpagina) && 
            (iblocco < isettore) && 
            (iblocco < ifsettore)) {
          //E' un blocco multirecord
          try {
            inizio = cercaButtonIn(oldHtml,iblocco);
            fine = cercaButtonFi(oldHtml,inizio);
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
        }
     
        if ((ipagina < iblocco) && 
            (ipagina < ifpagina) && 
            (ipagina < isettore) && 
            (ipagina < ifsettore)) {
          //E' un inizio pagina
          try {
            inizio = cercaButtonIn(oldHtml,ipagina);
            fine = cercaButtonFi(oldHtml,inizio);
            newHtml += oldHtml.substring(0,inizio);
            sHTML = oldHtml.substring(inizio,fine);
            AttributiTab at = new AttributiTab(sHTML);
            adsFormat = at.getAdsFormat();
          } catch (Exception e) {
            adsFormat = "[Errore: "+sHTML+" ]";
            System.out.println("Errore ripristino tag ads: "+e.toString());
          }
          newHtml += adsFormat;
          oldHtml = oldHtml.substring(fine,jlength);
        }
     
        if ((ifpagina < iblocco) && 
            (ifpagina < ipagina) && 
            (ifpagina < isettore) && 
            (ifpagina < ifsettore)) {
          //E' una fine pagina
          try {
            inizio = cercaButtonIn(oldHtml,ifpagina);
            fine = cercaButtonFi(oldHtml,inizio);
            newHtml += oldHtml.substring(0,inizio);
            sHTML = oldHtml.substring(inizio,fine);
            adsFormat = "&lt;<!-- ADSTPB -->&lt;<!-- PAGEPROPERTY PAGINA PAGEFORMAT -->-----------------Fine Pagina-----------------&gt;<!-- ADSTPE -->&gt;";
          } catch (Exception e) {
            adsFormat = "[Errore: "+sHTML+" ]";
            System.out.println("Errore ripristino tag ads: "+e.toString());
          }
          newHtml += adsFormat;
          oldHtml = oldHtml.substring(fine,jlength);
        }
     
        if ((isettore < iblocco) && 
            (isettore < ifpagina) && 
            (isettore < ipagina) && 
            (isettore < ifsettore)) {
          //E' un inizio pagina
          try {
            inizio = cercaButtonIn(oldHtml,isettore);
            fine = cercaButtonFi(oldHtml,inizio);
            newHtml += oldHtml.substring(0,inizio);
            sHTML = oldHtml.substring(inizio,fine);
            AttributiSettore as = new AttributiSettore(sHTML);
            adsFormat = as.getAdsFormat();
          } catch (Exception e) {
            adsFormat = "[Errore: "+sHTML+" ]";
            System.out.println("Errore ripristino tag ads: "+e.toString());
          }
          newHtml += adsFormat;
          oldHtml = oldHtml.substring(fine,jlength);
        }
     
        if ((ifsettore < iblocco) && 
            (ifsettore < ipagina) && 
            (ifsettore < isettore) && 
            (ifsettore < ifpagina)) {
          //E' una fine pagina
          try {
            inizio = cercaButtonIn(oldHtml,ifsettore);
            fine = cercaButtonFi(oldHtml,inizio);
            newHtml += oldHtml.substring(0,inizio);
            sHTML = oldHtml.substring(inizio,fine);
            adsFormat = "&lt;<!-- ADSTSB -->&lt;<!-- VISUALFINEPROPERTY FINE VISUALFINEFORMAT -->---------------------Fine Settore---------------------&gt;<!-- ADSTSE -->&gt;";
          } catch (Exception e) {
            adsFormat = "[Errore: "+sHTML+" ]";
            System.out.println("Errore ripristino tag ads: "+e.toString());
          }
          newHtml += adsFormat;
          oldHtml = oldHtml.substring(fine,jlength);
        }
     
      } else {
        newHtml += oldHtml;
        oldHtml = "";
      }

    }
    newHtml += oldHtml;
    
//  ------------------------------------------------------------------------------------    
//    newHtml = "";
//    int ib = oldHtml.indexOf("<div");
//    int ib2 = 0;
//    int jb = 0;
//    int kb = 0;
//    while (ib > -1) {
//      if (jb < ib) {
//        newHtml += oldHtml.substring(jb,ib);
//      }
//      jb = oldHtml.indexOf("</div>",ib);
//      if (jb > -1) {
//        jb = jb + 6;
//      }
//      ib2 = oldHtml.indexOf("<div",ib+1);
//      if (ib2 < jb && ib2 > -1) {
//        ib = ib2;
//      } else {
////        newHtml += oldHtml.substring(ib,jb);
//        kb = oldHtml.indexOf("type=\"tagblocco\"",ib);
//        if ((kb > ib) && (kb < jb)) {
//          
//          sHTML = oldHtml.substring(ib,jb);
//          try {
//            AttributiBlocco ab = new AttributiBlocco(sHTML);
//            adsFormat = ab.getAdsFormat();
//          } catch (Exception e) {
//            adsFormat = "[Errore: "+sHTML+" ]";
//            System.out.println("Errore ripristino tag ads: "+e.toString());
//          }
//        } else {
//          adsFormat = oldHtml.substring(ib,jb);
//        }
//        newHtml += adsFormat;
//        ib = oldHtml.indexOf("<div",jb);
//      }
//    }
//    newHtml += oldHtml.substring(jb);
//    oldHtml = newHtml;
//System.out.println("4----------------------------------------");
//System.out.println(oldHtml);
//
//    newHtml = "";
//    ib = oldHtml.indexOf("<span");
//    ib2 = 0;
//    jb = 0;
//    kb = 0;
//    while (ib > -1) {
//      if (jb < ib) {
//        newHtml += oldHtml.substring(jb,ib);
//      }
//      jb = oldHtml.indexOf("</span>",ib);
//      if (jb > -1) {
//        jb = jb + 7;
//      }
//      ib2 = oldHtml.indexOf("<span",ib+1);
//      if (ib2 < jb && ib2 > -1) {
//        ib = ib2;
//      } else {
//        kb = oldHtml.indexOf("type=\"tagvisual\"",ib);
//        if ((kb > ib) && (kb < jb)) {
//          sHTML = oldHtml.substring(ib,jb);
//          try {
//            AttributiSettore as = new AttributiSettore(sHTML);
//            adsFormat = as.getAdsFormat();
//          } catch (Exception e) {
//            adsFormat = "[Errore: "+sHTML+" ]";
//            System.out.println("Errore ripristino tag ads: "+e.toString());
//          }
//        } else {
//          adsFormat = oldHtml.substring(ib,jb);
//        }
//        newHtml += adsFormat;
//        ib = oldHtml.indexOf("<span",jb);
//      }
//    }
//    newHtml += oldHtml.substring(jb);
//    oldHtml = newHtml;
//
//    newHtml = "";
//    ib = oldHtml.indexOf("<span");
//    ib2 = 0;
//    jb = 0;
//    kb = 0;
//    while (ib > -1) {
//      if (jb < ib) {
//        newHtml += oldHtml.substring(jb,ib);
//      }
//      jb = oldHtml.indexOf("</span>",ib);
//      if (jb > -1) {
//        jb = jb + 7;
//      }
//      ib2 = oldHtml.indexOf("<span",ib+1);
//      if (ib2 < jb && ib2 > -1) {
//        ib = ib2;
//      } else {
////        newHtml += oldHtml.substring(ib,jb);
//        kb = oldHtml.indexOf("type=\"tagfinevisual\"",ib);
//        if ((kb > ib) && (kb < jb)) {
//          adsFormat = "&lt;<!-- ADSTSB -->&lt;<!-- VISUALFINEPROPERTY FINE VISUALFINEFORMAT -->---------------------Fine Settore---------------------&gt;<!-- ADSTSE -->&gt;";
//        } else {
//          adsFormat = oldHtml.substring(ib,jb);
//        }
//        newHtml += adsFormat;
//        ib = oldHtml.indexOf("<span",jb);
//      }
//    }
//    newHtml += oldHtml.substring(jb);
//    oldHtml = newHtml;
//
//
//    newHtml = "";
//    ib = oldHtml.indexOf("<div");
//    ib2 = 0;
//    jb = 0;
//    kb = 0;
//    while (ib > -1) {
//      if (jb < ib) {
//        newHtml += oldHtml.substring(jb,ib);
//      }
//      jb = oldHtml.indexOf("</div>",ib);
//      if (jb > -1) {
//        jb = jb + 6;
//      }
//      ib2 = oldHtml.indexOf("<div",ib+1);
//      if (ib2 < jb && ib2 > -1) {
//        ib = ib2;
//      } else {
////        newHtml += oldHtml.substring(ib,jb);
//        kb = oldHtml.indexOf("type=\"tagtabpage\"",ib);
//        if ((kb > ib) && (kb < jb)) {
//          sHTML = oldHtml.substring(ib,jb);
//          try {
//            AttributiTab at = new AttributiTab(sHTML);
//            adsFormat = at.getAdsFormat();
//          } catch (Exception e) {
//            adsFormat = "[Errore: "+sHTML+" ]";
//            System.out.println("Errore ripristino tag ads: "+e.toString());
//          }
//        } else {
//          adsFormat = oldHtml.substring(ib,jb);
//        }
//        newHtml += adsFormat;
//        ib = oldHtml.indexOf("<div",jb);
//      }
//    }
//    newHtml += oldHtml.substring(jb);
//    oldHtml = newHtml;
//
//    newHtml = "";
//    ib = oldHtml.indexOf("<div");
//    ib2 = 0;
//    jb = 0;
//    kb = 0;
//    while (ib > -1) {
//      if (jb < ib) {
//        newHtml += oldHtml.substring(jb,ib);
//      }
//      jb = oldHtml.indexOf("</div>",ib);
//      if (jb > -1) {
//        jb = jb + 6;
//      }
//      ib2 = oldHtml.indexOf("<div",ib+1);
//      if (ib2 < jb && ib2 > -1) {
//        ib = ib2;
//      } else {
////        newHtml += oldHtml.substring(ib,jb);
//        kb = oldHtml.indexOf("type=\"tagpagina\"",ib);
//        if ((kb > ib) && (kb < jb)) {
//          adsFormat = "&lt;<!-- ADSTPB -->&lt;<!-- PAGEPROPERTY PAGINA PAGEFORMAT -->-----------------Fine Pagina-----------------&gt;<!-- ADSTPE -->&gt;";
//        } else {
//          adsFormat = oldHtml.substring(ib,jb);
//        }
//        newHtml += adsFormat;
//        ib = oldHtml.indexOf("<div",jb);
//      }
//    }
//    newHtml += oldHtml.substring(jb);

    return newHtml;
  }

  /**
   * Main
   * @param args: usati per specificare alias, db, password, area, directory
   */
  public static void main(String[] args) {
    ModificaModello mm = new ModificaModello();

    StringBuffer  sb = new StringBuffer();
    FileReader    f = null;
    FileWriter    sf = null;
    
    try {
      f = new FileReader("c:\\test.html");
      int c = f.read();
    
      while (c != -1) {
        sb.append((char)c);
        c = f.read();
      }

      f.close();
    } catch (Exception e) {
      System.out.println(e.toString());
      try {f.close();} catch (Exception e2) {}
    }
    
    String mF = sb.toString();

/*    mF = mm.sostituisciTagAds(mF);
    try {
      sf = new FileWriter("c:\\nuovo.html");
      sf.write(mF);
      sf.close();
    } catch (Exception e) {
      System.out.println(e.toString());
      try {sf.close();} catch (Exception e2) {}
    }
*/
    mF = mm.ripristinaTagAds(mF);
    try {
      File fi = new File("c:\\vecchio.html");
      fi.delete();
      sf = new FileWriter("c:\\vecchio.html");
      sf.write(mF);
      sf.close();
    } catch (Exception e) {
      System.out.println(e.toString());
      try {sf.close();} catch (Exception e2) {}
    }
  }

  /**
   * 
   */
  private String tagToCampi(String modello) {
    // Ciclo di lettura e interpretazione del testo del modello:
    //    --> estrapolazione e creazione di tutti i campi
    int               j, jfield, jlength, k;
    String            subModello, campoHtml = "";
   
    subModello = modello;
    j = 0;

    while (j != -1) {
      jlength = subModello.length();
      jfield  = subModello.indexOf("&lt;<!-- ADSTFB -->");
      
      if (jfield == -1) {
        jfield = jlength;
      }

      if (jfield != jlength) {
        j = jfield;
        //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
        k = subModello.indexOf("<!-- ADSTFE -->",j);
        if (k != -1) {
          // ---> Da j a k+NAMEFIELDEND.length() è un CAMPO
          int startCh = 0,endChar;
          String sHtml = "";
          startCh = k + 15;
          endChar = startCh;
          try {
            if ((startCh + 10) <= jlength) {
              sHtml = subModello.substring(startCh,startCh + 10);
              if (sHtml.equalsIgnoreCase("<!-- style") ||
                  sHtml.equalsIgnoreCase("&gt;<!-- s")) {
                endChar = subModello.indexOf("-->",startCh);
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
            if (subModello.indexOf(fineChr,k) == endChar) {
              endChar = endChar+fineChr.length();
            } 
            campoHtml = subModello.substring(j, endChar);
            InformazioniCampo infoC = new InformazioniCampo(campoHtml,"ADSFORMAT -->");
            String popUp = infoC.getBlocco();
            String metaInf = infoC.getMetaData();
            if (!popUp.equalsIgnoreCase("")) {
              metaInf = metaInf.replaceFirst("!"+popUp,"");
            }
            if (infoC.getTipoCampo().equalsIgnoreCase("S") ||
                infoC.getTipoCampo().equalsIgnoreCase("H") ||
                infoC.getTipoCampo().equalsIgnoreCase("F")) {
              campoHtml = "<input type='text' class='"+infoC.getClassName()+"' style='"+infoC.getStile()+"' ";
              campoHtml += "title='"+infoC.getDato()+"' size='"+infoC.getLunghezza()+"' "; 
              campoHtml += "name='"+metaInf+"' value='"+infoC.getNomeDato()+"'/>";
            }
            if (infoC.getTipoCampo().equalsIgnoreCase("T") ||
                infoC.getTipoCampo().equalsIgnoreCase("Z")) {
              campoHtml = "<textarea class='"+infoC.getClassName()+"' style='"+infoC.getStile()+"' ";
              campoHtml += "title='"+infoC.getDato()+"' name='"+metaInf+"' ";
              campoHtml += "rows='"+infoC.getDecimali()+"' cols='"+infoC.getLunghezza()+"'>";
              campoHtml += infoC.getNomeDato()+"</textarea>";
            }
            if (infoC.getTipoCampo().equalsIgnoreCase("C")) {
              campoHtml = "<select type='text' class='"+infoC.getClassName()+"' style='"+infoC.getStile()+"' ";
              campoHtml += "size='"+infoC.getLunghezza()+"' name='"+metaInf+"'>"; 
              campoHtml += "<option>"+infoC.getDato()+"</option></select>";
            }
            if (infoC.getTipoCampo().equalsIgnoreCase("B")) {
              campoHtml = "<input type='checkbox' class='"+infoC.getClassName()+"' style='"+infoC.getStile()+"' ";
              campoHtml += "title='"+infoC.getDato()+"' name='"+metaInf+"' />"; 
            }
            if (infoC.getTipoCampo().equalsIgnoreCase("R")) {
              campoHtml = "<input type='radio' class='"+infoC.getClassName()+"' style='"+infoC.getStile()+"' ";
              campoHtml += "title='"+infoC.getDato()+"' name='"+metaInf+"' />"; 
            }
            if (!popUp.equalsIgnoreCase("")) {
              campoHtml += "<input type='image' id='"+popUp+"' title='' src='../common/images/gdm/dot.gif' />";
            }
          } catch(Exception ee) {
            System.out.println("Errore: "+ee.toString());
            subModello = subModello.substring(0,jfield) + "[Errore modifica campo ]" + subModello.substring(endChar);
          }
          // Mi resta da analizzare tutto quanto si trova dopo il TAGFIELDEND
          subModello = subModello.substring(0,jfield) + campoHtml + subModello.substring(endChar);
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
  private String tagPagina(String modello) {
    int               j, jtab, jpagina, jlength, k;
    int endChar;
    String            subModello, paginaHtml = "";
    String            sHtml = "";
    String            fineChr = "&gt;";
   
    subModello = modello;
    j = 0;

    while (j != -1) {
      jlength = subModello.length();
      jtab  = subModello.indexOf("&lt;<!-- ADSTTB -->");
      
      if (jtab == -1) {
        jtab = jlength;
      }

      if (jtab != jlength) {
        j = jtab;
        //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
        k = subModello.indexOf("<!-- ADSTTE -->",j);
        if (k != -1) {
          sHtml = "";
          endChar = k + 15 + fineChr.length();
          sHtml = subModello.substring(j, endChar);
          try {
            InformazioniTab infoTab = new InformazioniTab(sHtml,"&lt;<!-- TABPROPERTY","TABFORMAT -->");
//            paginaHtml = "<div type='tagtabpage' id='"+infoTab.getLabel();
            paginaHtml = "<button value='tagtabpage' id='"+infoTab.getLabel();
            if (!infoTab.getDominio().equalsIgnoreCase("")) {
              paginaHtml += "@"+infoTab.getDominio();
            }
            paginaHtml += "' title='Inizio Pagina ("+infoTab.getLabel()+")' "+
            "style='WIDTH:100%; BORDER-RIGHT: medium none; BORDER-TOP: medium none; BORDER-LEFT: medium none; BORDER-BOTTOM: 1px solid' align='center'>"+
            "Inizio Pagina ("+infoTab.getLabel()+")</button>";
          } catch(Exception ee) {
            System.out.println("Errore: "+ee.toString());
            subModello = subModello.substring(0,jtab) + "[Errore modifica tab pagina ]" + subModello.substring(endChar);
          }
          subModello = subModello.substring(0,jtab) + paginaHtml + subModello.substring(endChar);

//------------------------------
          jpagina  = subModello.indexOf("&lt;<!-- ADSTPB -->");
          if (jpagina == -1) {
            jpagina = jlength;
          }

          if (jpagina != jlength) {
            j = jpagina;
            //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
            k = subModello.indexOf("<!-- ADSTPE -->",j);
            if (k != -1) {
              sHtml = "";
              endChar = k + 15 + fineChr.length();
              sHtml = subModello.substring(j, endChar);
/*              paginaHtml = "<div type='tagpagina' title='Fine Pagina' "+
                "style='BORDER-RIGHT: medium none; BORDER-TOP: 1px solid; BORDER-LEFT: medium none; BORDER-BOTTOM: medium none'"+
                " align='center' >Fine Pagina</div>";*/
              paginaHtml = "<button value='tagpagina' title='Fine Pagina' "+
              "style='WIDTH: 100%; BORDER-RIGHT: medium none; BORDER-TOP: 1px solid; BORDER-LEFT: medium none; BORDER-BOTTOM: medium none'"+
              " align='center' >Fine Pagina</button>";
              subModello = subModello.substring(0,jpagina) + paginaHtml + subModello.substring(endChar);
            } else {
              j = -1;
            }
          } else {
            j = -1;  // Blocco la ricerca
          }
        } else {
          j = -1;
        }
      } else {
        j = -1;  // Blocco la ricerca
      }
    }
    return subModello;
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
            
/*            bloccoHtml = "<div type=\"tagblocco\" id=\""+myId;
            bloccoHtml += "@\" title=\"[ Blocco "+infoBlk.getBlocco()+" ]\" "+
              "style=\"BORDER-RIGHT: 1px solid; BORDER-TOP: 1px solid; BORDER-LEFT: 1px solid; BORDER-BOTTOM: 1px solid; BACKGROUND-COLOR: #ededed\" align=\"center\" />"+
              "<br/>[ Blocco "+sBlocco+" ]<br/><br/></div>";*/

            bloccoHtml = "<button value=\"tagblocco\" id=\""+myId;
            bloccoHtml += "@\" title=\"[ Blocco "+infoBlk.getBlocco()+" ]\" "+
              "style=\"WIDTH: 100%; BORDER-RIGHT: 1px solid; BORDER-TOP: 1px solid; BORDER-LEFT: 1px solid; BORDER-BOTTOM: 1px solid; BACKGROUND-COLOR: #ededed\" align=\"center\" />"+
              "<br/>[ Blocco "+sBlocco+" ]<br/><br/></button>";
          
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
  private String tagPopup(String modello) {
    int               j, jblocco, jlength, k, endChar=-1;
    String            subModello, popupHtml = "";
    String            sHtml = "";
    String            fineChr = "&gt;";
   
    subModello = modello;
    j = 0;

    while (j != -1) {
      jlength = subModello.length();
      jblocco  = subModello.indexOf("&lt;<!-- ADSTCB -->");
      
      if (jblocco == -1) {
        jblocco = jlength;
      }

      if (jblocco != jlength) {
        j = jblocco;
        //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
        k = subModello.indexOf("<!-- ADSTCE -->",j);
        if (k != -1) {
          sHtml = "";
          endChar = k + 15 + fineChr.length();
          sHtml = subModello.substring(j, endChar);
          try {
            InformazioniPopup infoPop = new InformazioniPopup(sHtml,"&lt;<!-- POPPROPERTY","POPFORMAT -->");

            String areaPopup = infoPop.getAreaBlocco();
            String myId = "";
            if (areaPopup.equalsIgnoreCase("")){
              myId = infoPop.getBlocco();
            } else {
              myId = areaPopup + "$" + infoPop.getBlocco();
            }
            
            String myName = infoPop.getNome();
            String myTitle = infoPop.getCampi();
            
            popupHtml = "<input type=\"image\" name= \""+myName+"\" id=\""+myId+"\"";
            popupHtml += " title=\""+myTitle+"\" src=\"../common/images/gdm/dot.gif\" />";
          } catch(Exception ee) {
            System.out.println("Errore: "+ee.toString());
            subModello = subModello.substring(0,jblocco) + "[Errore modifica tab pagina ]" + subModello.substring(endChar);
          }
          subModello = subModello.substring(0,jblocco) + popupHtml + subModello.substring(endChar);
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
  private String tagSettore(String modello) {
    int               j, jvisual, jfinevi, jlength, k;
    int endChar;
    String            subModello, visualHtml = "";
    String            sHtml = "";
    String            fineChr = "&gt;";
   
    subModello = modello;
    j = 0;

    while (j != -1) {
      jlength = subModello.length();
      jvisual  = subModello.indexOf("&lt;<!-- ADSTVB -->");
      
      if (jvisual == -1) {
        jvisual = jlength;
      }

      if (jvisual != jlength) {
        j = jvisual;
        //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
        k = subModello.indexOf("<!-- ADSTVE -->",j);
        if (k != -1) {
          sHtml = "";
          endChar = k + 15 + fineChr.length();
          sHtml = subModello.substring(j, endChar);
          try {
            InformazioniSettore infoSet = new InformazioniSettore(sHtml,"&lt;<!-- VISUALPROPERTY","VISUALFORMAT -->","<!-- ADSTVE -->");
            visualHtml = "<button value='tagvisual' id='"+infoSet.getDominio();
            visualHtml += "' title='Inizio Settore ("+infoSet.getSettore()+")' "+
            "style='BORDER-TOP: 1px solid; BORDER-LEFT: 1px solid; BORDER-BOTTOM: 1px solid; BACKGROUND-COLOR: #87cefa'>"+
            "Settore ("+infoSet.getSettore()+")</button>";
          } catch(Exception ee) {
            System.out.println("Errore: "+ee.toString());
            subModello = subModello.substring(0,jvisual) + "[Errore modifica settore ]" + subModello.substring(endChar);
          }
          subModello = subModello.substring(0,jvisual) + visualHtml + subModello.substring(endChar);

//------------------------------
          jfinevi  = subModello.indexOf("&lt;<!-- ADSTSB -->");
          if (jfinevi == -1) {
            jfinevi = jlength;
          }

          if (jfinevi != jlength) {
            j = jfinevi;
            //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
            k = subModello.indexOf("<!-- ADSTSE -->",j);
            if (k != -1) {
              sHtml = "";
              endChar = k + 15 + fineChr.length();
              sHtml = subModello.substring(j, endChar);
              visualHtml = "<button value='tagfinevisual' title='Fine Settore' "+
                "style='BORDER-TOP: 1px solid; BORDER-RIGHT:  1px solid; BORDER-BOTTOM:  1px solid; BACKGROUND-COLOR: #87cefa'>Fine Settore</button>";
              subModello = subModello.substring(0,jfinevi) + visualHtml + subModello.substring(endChar);
            } else {
              j = -1;
            }
          } else {
            j = -1;  // Blocco la ricerca
          }
        } else {
          j = -1;
        }
      } else {
        j = -1;  // Blocco la ricerca
      }
    }
    return subModello;
  }
  
  private int cercaButtonIn(String pHtml, int posTag) throws Exception {
    int retval = -1;
    int i = 0;
    while (i > -1) {
      i = pHtml.indexOf("<button",retval+1);
      if ((i > -1) && (i < posTag)) {
        retval = i;
      } else {
        i = -1;
      }
    }
    if (retval == -1) {
      throw new Exception ("Tag button iniziale non trovato!");
    }
    return retval;
  }

  private int cercaButtonFi(String pHtml, int posTag) throws Exception {
    int retval = -1;
    int j = pHtml.indexOf("</button>",posTag);
    int i = pHtml.indexOf("<button",posTag+1);
    retval = j;
    while ((i > -1) && (i < j)) {
      retval = j;
      i = pHtml.indexOf("<button",i+1);
      j = pHtml.indexOf("</button>",j+1);
    }
    if (retval == -1) {
      throw new Exception ("Tag button finale non trovato!");
    }
    return retval+9;
  }

/*  private int cercaDivIn(String pHtml, int posTag) throws Exception {
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

  private int cercaSpanIn(String pHtml, int posTag) throws Exception {
    int retval = -1;
    int i = 0;
    while (i > -1) {
      i = pHtml.indexOf("<span",retval+1);
      if ((i > -1) && (i < posTag)) {
        retval = i;
      } else {
        i = -1;
      }
    }
    if (retval == -1) {
      throw new Exception ("Tag span iniziale non trovato!");
    }
    return retval;
  }

  private int cercaSpanFi(String pHtml, int posDiv) throws Exception {
    int retval = -1;
    int j = pHtml.indexOf("</span>",posDiv);
    int i = pHtml.indexOf("<span",posDiv+1);
    retval = j;
    while ((i > -1) && (i < j)) {
      retval = j;
      i = pHtml.indexOf("<span",i+1);
      j = pHtml.indexOf("</span>",j+1);
    }
    if (retval == -1) {
      throw new Exception ("Tag span finale non trovato!");
    }
    return retval+7;
  }*/

}