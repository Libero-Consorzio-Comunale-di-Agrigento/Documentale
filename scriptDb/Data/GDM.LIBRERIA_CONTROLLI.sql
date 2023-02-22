Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'COPIA_OGGETTI', 'var ret=false; if(seq!="") {   if((seq.indexOf(''X'')==-1) && (seq.indexOf(''L'')==-1)) ret=true; else ret=false; } return ret;', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_CREADOCAUTO', 'popup_without_resize(''SceltaTipoDocumentoForNew.do?idCartProveninez=''+document.getElementById(''idCartella'').value+''&Provenienza=''+document.getElementById(''CartellaOrQuery'').value+''&idQueryProveninez=''+document.getElementById(''idQuery'').value+''&docAuto=Y'',500,400, 0, 50);', NULL, NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_CREA_DESKTOP', 'popup(''pageFlex.do?titolo=Inserimento&src=jwl_gestioneCategorie&idCartella=''+document.getElementById(''idCartella'').value,600,400,0,50);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'RIMUOVI_OGGETTI', 'var ret=false; if (CompOgg=="0")   return false;   if (tipoOggetto == ''Q'')  ret=false;  else  {  if(seq!="")   {     
         if(seq.indexOf(''C'')==-1 && seq.indexOf(''L'')==-1)          ret=true;      else      ret=false;     }   }  return ret;', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'CREA_QUERY', 'var ret=true;  if (tipoOggetto==''Q'')    ret=false; else	   {      	if(CompOgg=="1") 		  	  ret=true; 	    	else 		  	  ret=false;	     }   return ret;', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'SPOSTA_OGGETTI', 'var ret=false;  if (CompOgg=="0")   return false;   if (ruolo!=''AMM'' && ((seq.indexOf(''C'')!=-1) || (seq.indexOf(''X'')!=-1) || (seq.indexOf(''L'')!=-1)))   return false;   if (tipoOggetto==''Q'')   return false;  else  {    if(seq!="")      ret=true;    else        ret=false;  }  return ret;', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_CREADOC', 'popup_without_resize(''SceltaTipoDocumentoForNew.do?idCartProveninez=''+document.getElementById(''idCartella'').value+''&Provenienza=''+document.getElementById(''CartellaOrQuery'').value+''&idQueryProveninez=''+document.getElementById(''idQuery'').value,500,400, 0, 50);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_COPIA_CONFORME', 'window.open(''../common/ServletVisualizza.do?iddocs=''+document.getElementById(''ListaId'').value,''popup_ServletModulistica'',''toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 1,resizable= 1,copyhistory= 0,width=500,height=300,top=50,left=50'');', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_SPOSTA_OGGETTI', 'popup(''pageFlex.do?titolo=Sposta&src=treecopiasposta&idCartellaProvenienza=''+document.getElementById(''idCartella'').value+''&listaID=''+document.getElementById(''ListaId'').value+''&tipoOperazione=S&idQueryProvenienza=''+document.getElementById(''idQuery'').value+''&wrksp=''+document.getElementById(''idCartellaTree'').value,500,600, 0, 50);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_CREACART', 'popup(''CartellaMaint.do?idCartProveninez=''+document.getElementById(''idCartella'').value+''&Provenienza=C'',400,160,0,50);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'MARCA_DA_CONSERVARE', '<invocation class="it.finmatica.jdms.conservazione.Conservazione"> <method name="marcaDaConservare"> <params> <param type="String"><![CDATA[ :XML ]]></param> </params> </method> </invocation>', 'J', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_DELEGA_COMPETENZE', 'popup(''pageFlex.do?titolo=Delega&nbsp;Competenze&src=gestionedeleghe'',750,410,0,50);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'N', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_IMPORTA_DATI', 'popup_window(''importadati.html?idQuery=''+document.getElementById(''idQuery'').value+''&idCartella=''+document.getElementById(''idCartella'').value+''&wrksp=''+document.getElementById(''idCartellaTree'').value,''importazione'',700,450,0,0);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_COLLEGA', 'if(document.getElementById(''ListaId'').value!='''') {
popup_hidden(''Collegamenti.do?tipo=C&amp;seqC=''+document.getElementById(''ListaId'').value+''&amp;idQuery=''+document.getElementById(''idQuery'').value); }', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_INCOLLA', 'if(document.getElementById(''ListaId'').value!='''') { popup_hidden(''Collegamenti.do?tipo=I&amp;seqD=''+document.getElementById(''ListaId'').value+''&amp;idQuery=''+document.getElementById(''idQuery'').value); }', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'FIRMA_DOC', 'var ret=false; if((seq.indexOf(''D'')!=-1) && (seq.indexOf(''C'')==-1) && (seq.indexOf(''Q'')==-1) && (seq.indexOf(''X'')==-1) && (seq.indexOf(''L'')==-1))   return true; else 								   return false;', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_CREAQUERY', 'popup(''CartellaMaint.do?idCartProveninez=''+document.getElementById(''idCartella'').value+''&Provenienza=Q'',400,160, 0, 50);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'ELIMINA_OGGETTI', 'var ret=false; if(seq!="") {  if((seq.indexOf(''X'')==-1) && (seq.indexOf(''L'')==-1) )   ret=true; else   ret=false;		 } return ret;', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'CREA_DOC', 'var ret=true; if (CompOgg=="1")   ret=true;     else 	   ret=false;	 		    if (tipoOggetto==''Q'')     ret=true; 							  return ret;', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_COPIA_OGGETTI', 'popup(''pageFlex.do?titolo=Copia&src=treecopiasposta&idCartellaProvenienza=C''+document.getElementById(''idCartella'').value+''&listaID=''+document.getElementById(''ListaId'').value+''&tipoOperazione=C&idQueryProvenienza=''+document.getElementById(''idQuery'').value+''&wrksp=''+document.getElementById(''idCartellaTree'').value,500,600, 0, 50);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'CREA_CART', 'var ret=true;  if (tipoOggetto==''Q'')   ret=false;  else {						         if(CompOgg=="1") 		    ret=true; 	     else 		    ret=false;	     }  return ret;', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'COPIA_CONFORME', 'var ret=false;  

if(seq!='''') {
if((seq.indexOf(''C'')!=-1) || (seq.indexOf(''X'')!=-1) || (seq.indexOf(''L'')!=-1) || (seq.indexOf(''Q'')!=-1))
 ret=false;
else
 ret=true;
}
return ret;', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_LINK_DOC', 'var lista=document.getElementById(''ListaId'').value;
if(lista!='''') {
if(window.parent) {  
  var link=window.parent.window.parent;
link.location.href=''./richiamoDocumentale.do?sequenza=''+document.getElementById(''ListaId'').value+''&redirectAPP=''+document.getElementById(''redirectAPP'').value; 
}
}', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'CHIUDI_MODELLO', '<invocation class="it.finmatica.modutils.chiudimodello.ChiudiModello"> <method name="chiudiPagina">   <params>     <param type="String"><![CDATA[ :XML ]]></param>  </params> </method> </invocation>', 'J', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('02/22/2023 16:53:21', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_ELIMINA_OGGETTI', 'if(document.getElementById(''ListaId'').value){if (confirm(''Sei sicuro di voler eliminare gli Oggetti selezionati? '') == true) popup_hidden(''EliminaOggetti.do?listaId=''+document.getElementById(''ListaId'').value+''&amp;idCartella=''+document.getElementById(''idCartella'').value+''&amp;idQuery=''+document.getElementById(''idQuery'').value+''&amp;Prov=''+document.getElementById(''CartellaOrQuery'').value);}', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'FIRMA_STANDARD', 'var ret=false;  

if(seq!='''') {
if((seq.indexOf(''C'')!=-1) || (seq.indexOf(''X'')!=-1) || (seq.indexOf(''L'')!=-1) || (seq.indexOf(''Q'')!=-1))
 ret=false;
else
 ret=true;
}
return ret', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_FIRMA_STANDARD', 'if(document.getElementById(''ListaId'').value) { popup(''Gestionefirma.do?lista=''+document.getElementById(''ListaId'').value,600,600, 0, 50);
checkedALLCheckBox(false);
document.getElementById(''AllSeleziona'').checked=false;
}', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_VERIFICA_ALLEGATI', 'if(document.getElementById(''ListaId'').value) { popup(''VerificaAllegati.do?lista=''+document.getElementById(''ListaId'').value,600,600, 0, 50);
checkedALLCheckBox(false);
document.getElementById(''AllSeleziona'').checked=false;
}', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_RIMUOVI_OGGETTI', 'popup_hidden(''EliminaLinkDoc.do?idCartProveninez=''+document.getElementById(''idCartella'').value+''&listaID=''+document.getElementById(''ListaId'').value);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'COLLEGA', 'var ret=false;  

if(seq!='''') {
if((seq.indexOf(''C'')!=-1) || (seq.indexOf(''X'')!=-1) || (seq.indexOf(''L'')!=-1) || (seq.indexOf(''Q'')!=-1))
 ret=false;
else
 ret=true;
}
return ret;', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'N', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'LINK_ESTERNO', 'var ret=false;  if(ruolo!=''AMM'') ret=false; else ret=true; return ret;', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'LINK_DOC', 'var ret=false; if((seq.indexOf(''D'')!=-1) && (seq.indexOf(''C'')==-1) && (seq.indexOf(''Q'')==-1) && (seq.indexOf(''X'')==-1))   return true; else return false;', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'CONSERVA', '<invocation class="it.finmatica.jdms.conservazione.Conservazione"> <method name="daConservare"> <params> <param type="String"><![CDATA[ :XML ]]></param> <param type="String">JSUITE_CONSERVAZIONE_STD</param> </params> </method> </invocation>', 'J', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'N', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'INCOLLA', 'var ret=false;  

if(document.getElementById(''lista_collegamenti'')) { 

if(document.getElementById(''lista_collegamenti'').value!='''') {

ret=true;

if((seq.indexOf(''C'')!=-1) || (seq.indexOf(''X'')!=-1) || (seq.indexOf(''L'')!=-1) || (seq.indexOf(''Q'')!=-1))
 ret=false;
else
 ret=true;

}
else
 ret=false;

}

return ret;', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'N', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_FIRMA_DOCS', 'popup(''../restrict/GestioneFirmaFile.do?IDDOCUMENTI=''+document.getElementById(''ListaId'').value+''&idCartProveninez=''+document.getElementById(''idCartella'').value.substring(1,document.getElementById(''idCartella'').value.length),450,200, 0, 50);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'VERIFICA_ALLEGATI', 'var ret=false;  

if(seq!='''') {
if((seq.indexOf(''C'')!=-1) || (seq.indexOf(''X'')!=-1) || (seq.indexOf(''L'')!=-1) || (seq.indexOf(''Q'')!=-1))
 ret=false;
else {
 var num=contaOccorrenze(seq,''D'');
 if(num==1)
   ret=true;
 else
   ret=false;
}
}
return ret', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'P_CREA_LINK_ESTERNO', 'popup(''./linkesterni.html?id=&idCartella=''+document.getElementById(''idCartella'').value,700,300,0,50);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:23:43', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('TESTADS', 'AVANZA_FLUSSO_PLSQL', 'SYNC.EMPTYF()', 'P', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:24:03', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('TESTADS', 'CARICAMENTO_CODICE_JAVASCRIPT', 'function CARICAMENTO_CODICE_JAVASCRIPT(){

alert ("Il codice javascript ¿ stato caricato correttamente");
}', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('07/28/2021 10:24:03', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'AGGIUNGI_NOTIFICHE', '<invocation class="it.finmatica.jsuite.fatture.FatturaElettronicaPA"> <method name="aggiungiNotificheDaModello"> <params> <param type="String"><![CDATA[ :XML ]]></param> </params> </method> </invocation>', 'J', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('02/22/2023 16:53:21', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'CLOSEWIN', 'function CLOSEWIN() {
    window.close();  
}', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('02/22/2023 16:53:21', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'JQ_COPIA_OGGETTI', 'popup(''../treecopiaspostaServlet?idCartellaProvenienza=C''+document.getElementById(''idCartAppartenenza'').value+''&listaID=''+document.getElementById(''ListaId'').value+''&tipoOperazione=C&idQueryProvenienza=''+document.getElementById(''idQuery'').value+''&wrksp=''+document.getElementById(''wrksp'').value,700,500, 0, 50);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('02/22/2023 16:53:21', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'JQ_CREA_DESKTOP', 'popup(''../gestioneCategorieServlet?idCartella=''+document.getElementById(''idCartella'').value,600,400,0,50);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('02/22/2023 16:53:21', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'JQ_CREA_LINK_ESTERNO', 'popup(''../collegamentiEsterniServlet?id=&idCartella=''+document.getElementById(''idCartella'').value,700,300,0,50);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('02/22/2023 16:53:21', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'JQ_DELEGHE_COMPETENZE', 'popup(''../gestioneDelegheServlet'',800,650,0,50);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('02/22/2023 16:53:21', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'JQ_IMPORTA_DATI', 'popup_window(''../importaDatiServlet?idQuery=''+document.getElementById(''idQuery'').value+''&idCartella=''+document.getElementById(''idCartAppartenenza'').value+''&wrksp=''+document.getElementById(''wrksp'').value,''importazione'',800,500,0,0);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('02/22/2023 16:53:21', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'JQ_SPOSTA_OGGETTI', 'popup(''../treecopiaspostaServlet?idCartellaProvenienza=''+document.getElementById(''idCartella'').value+''&listaID=''+document.getElementById(''ListaId'').value+''&tipoOperazione=S&idQueryProvenienza=''+document.getElementById(''idQuery'').value+''&wrksp=''+document.getElementById(''idCartellaTree'').value,700,500, 0,50);', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('02/22/2023 16:53:21', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
Insert into LIBRERIA_CONTROLLI
   (AREA, CONTROLLO, CORPO, TIPO, DRIVER, 
    CONNESSIONE, UTENTE, PASSWD, MSG_ERRORE, CAMPI, 
    DSN, SBLOCCO_AUTOMATICO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, PERSONALIZZAZIONE)
 Values
   ('GDMSYS', 'REFRESH_PADRE', 'function REFRESH_PADRE () {
window.opener.location.reload();
}', 'S', NULL, 
    NULL, NULL, NULL, NULL, NULL, 
    NULL, 'S', TO_DATE('02/22/2023 16:53:21', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 'N');
COMMIT;
