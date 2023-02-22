CREATE OR REPLACE FUNCTION F_Download_Upload_File (
             P_IDDOCUMENTI IN VARCHAR2,
             P_RETURN_PAGE IN VARCHAR2,
             P_TIPOFIRMA IN VARCHAR2,
													P_PIN IN VARCHAR2,
													P_TEMP IN VARCHAR2
													)
RETURN VARCHAR2 IS
/******************************************************************************
 NOME:        GET_CARICA_SCARICA_FILE
 DESCRIZIONE: Restituisce la stringa formattata HTML
              che lancia l'applet e le funzioni necessarie
              per caricare e scaricare i file da CLIENT a DB e viceversa
			  Utilizzo: FIRMA DEI DOCUMENTI
 PARAMETRI:   --
 RITORNA:
 ******************************************************************************/
 d_return              VARCHAR2(32000);
 d_temp                VARCHAR2(200);
 d_qryStr              VARCHAR2(1000);

BEGIN
   --Se non sono stati selezionati documenti per la firma
   --reindirizzo sulla pagina di chiusura
   IF NVL(P_IDDOCUMENTI,' ') = ' ' THEN
      d_return  := '<script language="JavaScript1.2">'||
                   'document.location.href="../common/ClosePage.do"'||
                   '</script>';
   ELSE

		 d_return :=
			'<SCRIPT language="JavaScript1.2">'||
		 		 'function getAppletObject() {'||
					 '  if (navigator.appName.indexOf("Microsoft Internet")!=-1) {'||
					 '    return document.getElementById("UploadDownload"); }'||
					 '  else {'||
					 '    return window.document.UploadDownload; }'||
					 '}'||
      'function firma (appletName, fileOrDirectory, pin) {'||
		    '  alert("FIRMA!");'||
      '  if (!navigator.javaEnabled()) {'||
		    '    alert ("Java è disabilitato!");'||
      ' 	  return false;}'||
      '  var applet = document.applets[appletName];'||
      '  try {'||
		    '    alert ("inizio la firma in "+fileOrDirectory);'||
		    '    applet.setSomethingToSign (fileOrDirectory);'||
		    '    alert ("con pin="+pin);'||
		    '    applet.setPin (pin);'||
		    '    applet.sign ();'||
		    '    alert ("firma dei documenti completata");'||
      '    return true;	}'||
	     '  catch (e) {'||
      ' 		if (e instanceof Error)'||
      ' 		{'||
      ' 			 var msg = e.message;'||
      ' 			 msg = msg.substring (msg.indexOf (":")+1, msg.length);'||
      ' 			 msg = msg.substring (msg.indexOf (":")+1, msg.length);'||
      ' 			 alert (msg);'||
      ' 		}'||
      ' 		else'||
      ' 			 alert (getTheRealException(e).getMessage ());			'||
		    '   return false;	}'||
	     '}'||
      'function eventHandler (message) {'||
      '}'||
      'function getTheRealException (exception) {'||
	     '  if (exception.getClass() == "class java.security.PrivilegedActionException" ||'||
		    '  exception.getClass() == "class java.lang.reflect.InvocationTargetException"){'||
		    '    return getTheRealException (exception.getCause ()); }'||
	     '  return exception;'||
      '}'||
      'function richiestaFirma() {'||
		      'host = document.location.host;'||
		      'url = "http://"+host+"/UploadDownload/dmservlet?docIds='||replace(P_IDDOCUMENTI,'D','')||'&dataSource=jdbc/gdm&user=GDM&pwd=GDM&TIPOFIRMA="+document.forms ["PIN"].TipoFirma.value;'||
        'alert("url= "+url);'||
		      'getAppletObject().setBaseDir("c:\\temp\\firma\\");getAppletObject().download(url);'||
        'alert("DEBUG: Fatto il download");'||
        'if (firma ("JSign", "c:\\temp\\firma\\", document.forms ["PIN"].PIN.value)) {'||
		      '  alert("Vado a fare upload!");'||
        '  try {'||
        '    alert("url= "+url);'||
		      '    getAppletObject().upload(url, "c:\\temp\\firma\\");'||
		      '    alert("Operazione eseguita con successo!"); }'||
	       '  catch (e) {'||
	       '    alert ("errore in upload");	} '||
        '}'||
	       'else {'||
	       '  alert ("errore nella firma");	}'||
        'document.location.href="../common/ClosePage.do"'||
      '}'||
 		'</SCRIPT>';

--		END IF;
     END IF;

   RETURN d_return;

END F_Download_Upload_File;
/

