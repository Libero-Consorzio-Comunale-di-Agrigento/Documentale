CREATE OR REPLACE FORCE VIEW ALLEGATI_LISTA_HTML
(ID_DOCUMENTO, ID_DOCUMENTO_FIGLIO, ALLEGATO, URL, SRC, 
 ICONA, OGFI_ID_DOCUMENTO, ID_OGGETTO_FILE, FILENAME, NOME_FORMATO)
BEQUEATH DEFINER
AS 
SELECT ogfi.id_documento id_documento,
               0 id_documento_figlio,
                  '<div valign="middle" class="menuAllegatiLinks" onmouseover="this.style.color=''#FFFFFF'';this.style.background=''#0000A0'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.background=''#0000A0'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.color=''#FFFFFF''" style="BORDER-BOTTOM: 1px outset" onmouseout="this.style.color=''#000000'';this.style.background=''#CCCCCC'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.background=''#CCCCCC'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.color=''#000000''"><img alt="'
               || filename
               || '" src="../common/images/gdm/formati/'
               || NVL (icona, 'generico.gif')
               || '" width="16" height="16" />'
               || DECODE (
                     INSTR (UPPER (ogfi.filename), '.P7M'),
                     0,    '<a href="#" onclick="window.open(''../common/ServletVisualizza.do?ar='
                        || d.area
                        || CHR (38)
                        || 'cm='
                        || m.codice_modello
                        || CHR (38)
                        || 'cr='
                        || d.codice_richiesta
                        || CHR (38)
                        || 'ca='
                        || ogfi.id_oggetto_file
                        || CHR (38)
                        || 'iddoc='
                        || d.id_documento
                        || CHR (38)
                        || DECODE (
                              INSTR (UPPER (ogfi.filename), '.P7M.ZIP'),
                              0, DECODE (
                                    INSTR (UPPER (ogfi.filename), '.P7M'),
                                    0, 'firma=',
                                    'firma=S'),
                              'firma=')
                        || ''',''popup_ServletModulistica'',''toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 1,resizable= 1,copyhistory= 0,width=500,height=300,top=50,left=50'');" ><INPUT CLASS="notEnableFontInputAllegati" id=''OGF'
                        || ogfi.id_oggetto_file
                        || ''' onmouseover="this.style.color=''#FFFFFF'';" onmouseout="this.style.color=''#000000'';" readOnly value="'
                        || f_namefile_ext (filename)
                        || '"></input></a>',
                        '<a href="#" onclick="window.open(''../common/ServletVisualizza.do?ar='
                     || d.area
                     || CHR (38)
                     || 'cm='
                     || m.codice_modello
                     || CHR (38)
                     || 'cr='
                     || d.codice_richiesta
                     || CHR (38)
                     || 'ca='
                     || ogfi.id_oggetto_file
                     || CHR (38)
                     || 'iddoc='
                     || d.id_documento
                     || CHR (38)
                     || DECODE (
                           INSTR (UPPER (ogfi.filename), '.P7M.ZIP'),
                           0, DECODE (INSTR (UPPER (ogfi.filename), '.P7M'),
                                      0, 'firma=',
                                      'firma=S'),
                           'firma=')
                     || ''',''popup_ServletModulistica'',''toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 1,resizable= 1,copyhistory= 0,width=500,height=300,top=50,left=50'');" ><INPUT CLASS=notEnableFontInputAllegati id=''OGF'
                     || ogfi.id_oggetto_file
                     || ''' onmouseover="this.style.color=''#FFFFFF'';" onmouseout="this.style.color=''#000000'';" readOnly value="'
                     || f_namefile_ext (filename)
                     || '"></input></a>')
               || '</div>'
                  allegato,
                  '../common/ServletVisualizza.do?ar='
               || d.area
               || CHR (38)
               || 'cm='
               || m.codice_modello
               || CHR (38)
               || 'cr='
               || d.codice_richiesta
               || CHR (38)
               || 'ca='
               || ogfi.id_oggetto_file
               || CHR (38)
               || 'iddoc='
               || d.id_documento
               || CHR (38)
               || DECODE (
                     INSTR (UPPER (ogfi.filename), '.P7M.ZIP'),
                     0, DECODE (INSTR (UPPER (ogfi.filename), '.P7M'),
                                0, 'firma=',
                                'firma=S'),
                     'firma=')
                  url,
               f_namefile_ext (filename) src,
               NVL (icona, 'generico.gif') src_icona,
               d.id_documento ogfi_id_documento,
               ogfi.id_oggetto_file,
               ogfi.filename,
               fofi.nome
          FROM oggetti_file ogfi,
               formati_file fofi,
               documenti d,
               modelli m
         WHERE     ogfi.id_formato = fofi.id_formato
               AND fofi.visibile = 'S'
               AND d.id_documento = ogfi.id_documento
               AND d.id_tipodoc = m.id_tipodoc
               AND NOT EXISTS
                      (SELECT 1
                         FROM agspr_AGP_FILE_DOC_PRINCIPALE
                        WHERE d.id_documento = ID_DOC_ESTERNO_AGSPR_PADRE)
        UNION
        SELECT d.id_documento_padre,
               d.id_documento id_documento_figlio,
                  '<div valign="middle" class="menuAllegatiLinks" onmouseover="this.style.color=''#FFFFFF'';this.style.background=''#0000A0'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.background=''#0000A0'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.color=''#FFFFFF''" style="BORDER-BOTTOM: 1px outset" onmouseout="this.style.color=''#000000'';this.style.background=''#CCCCCC'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.background=''#CCCCCC'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.color=''#000000''"><img alt="'
               || filename
               || '" src="../common/images/allegati_doc_figli.png" width="16" heigth="16" />'
               || DECODE (
                     INSTR (UPPER (ogfi.filename), '.P7M'),
                     0,    '<a href="#" onclick="window.open(''../common/ServletVisualizza.do?ar='
                        || d.area
                        || CHR (38)
                        || 'cm='
                        || m.codice_modello
                        || CHR (38)
                        || 'cr='
                        || d.codice_richiesta
                        || CHR (38)
                        || 'ca='
                        || ogfi.id_oggetto_file
                        || CHR (38)
                        || 'iddoc='
                        || d.id_documento
                        || CHR (38)
                        || DECODE (
                              INSTR (UPPER (ogfi.filename), '.P7M.ZIP'),
                              0, DECODE (
                                    INSTR (UPPER (ogfi.filename), '.P7M'),
                                    0, 'firma=',
                                    'firma=S'),
                              'firma=')
                        || ''',''popup_ServletModulistica'',''toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 1,resizable= 1,copyhistory= 0,width=500,height=300,top=50,left=50'');" ><INPUT CLASS="notEnableFontInputAllegati" id=''OGF'
                        || ogfi.id_oggetto_file
                        || ''' onmouseover="this.style.color=''#FFFFFF'';" onmouseout="this.style.color=''#000000'';" readOnly value="'
                        || f_namefile_ext (filename)
                        || '"></input></a>',
                        '<a href="#" onclick="window.open(''../common/ServletVisualizza.do?ar='
                     || d.area
                     || CHR (38)
                     || 'cm='
                     || m.codice_modello
                     || CHR (38)
                     || 'cr='
                     || d.codice_richiesta
                     || CHR (38)
                     || 'ca='
                     || ogfi.id_oggetto_file
                     || CHR (38)
                     || 'iddoc='
                     || d.id_documento
                     || CHR (38)
                     || DECODE (
                           INSTR (UPPER (ogfi.filename), '.P7M.ZIP'),
                           0, DECODE (INSTR (UPPER (ogfi.filename), '.P7M'),
                                      0, 'firma=',
                                      'firma=S'),
                           'firma=')
                     || ''',''popup_ServletModulistica'',''toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 1,resizable= 1,copyhistory= 0,width=500,height=300,top=50,left=50'');" ><INPUT CLASS=notEnableFontInputAllegati id=''OGF'
                     || ogfi.id_oggetto_file
                     || ''' onmouseover="this.style.color=''#FFFFFF'';" onmouseout="this.style.color=''#000000'';" readOnly value="'
                     || f_namefile_ext (filename)
                     || '"></input></a>')
               || '</div>'
                  allegato,
                  '../common/ServletVisualizza.do?ar='
               || d.area
               || CHR (38)
               || 'cm='
               || m.codice_modello
               || CHR (38)
               || 'cr='
               || d.codice_richiesta
               || CHR (38)
               || 'ca='
               || ogfi.id_oggetto_file
               || CHR (38)
               || 'iddoc='
               || d.id_documento
               || CHR (38)
               || DECODE (
                     INSTR (UPPER (ogfi.filename), '.P7M.ZIP'),
                     0, DECODE (INSTR (UPPER (ogfi.filename), '.P7M'),
                                0, 'firma=',
                                'firma=S'),
                     'firma=')
                  url,
               f_namefile_ext (filename) src,
               'allegati_doc_figli.png' src_icona,
               d.id_documento,
               ogfi.id_oggetto_file,
               ogfi.filename,
               fofi.nome
          FROM documenti d,
               oggetti_file ogfi,
               formati_file fofi,
               modelli m,
               documenti dpadre
         WHERE     ogfi.id_formato = fofi.id_formato
               AND fofi.visibile = 'S'
               AND d.id_documento = ogfi.id_documento
               AND d.stato_documento NOT IN ('CA', 'RE', 'PB')
               AND dpadre.id_documento = d.id_documento_padre
               AND d.id_tipodoc = m.id_tipodoc
               AND d.id_documento_padre IS NOT NULL
               AND m.view_allegati_padre = 'Y'
               AND NOT EXISTS
                      (SELECT 1
                         FROM AGP_FILE_DOC_COLLEGATI_TUTTI
                        WHERE d.id_documento_padre =
                                 ID_DOC_ESTERNO_AGSPR_PADRE)
        UNION
        SELECT id_doc_esterno_agspr_padre,
               0 id_documento_figlio,
                  '<div valign="middle" class="menuAllegatiLinks" onmouseover="this.style.color=''#FFFFFF'';this.style.background=''#0000A0'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.background=''#0000A0'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.color=''#FFFFFF''" style="BORDER-BOTTOM: 1px outset" onmouseout="this.style.color=''#000000'';this.style.background=''#CCCCCC'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.background=''#CCCCCC'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.color=''#000000''"><img alt="'
               || filename
               || '" src="../common/images/gdm/formati/'
               || NVL (icona, 'generico.gif')
               || '" width="16" height="16" />'
               || DECODE (
                     INSTR (UPPER (ogfi.filename), '.P7M'),
                     0,    '<a href="#" onclick="window.open(''../common/ServletVisualizza.do?ar='
                        || m.area
                        || CHR (38)
                        || 'cm='
                        || m.codice_modello
                        || CHR (38)
                        || 'cr='
                        || fd.codice_richiesta
                        || CHR (38)
                        || 'ca='
                        || ogfi.id_oggetto_file
                        || CHR (38)
                        || 'iddoc='
                        || fd.id_documento
                        || CHR (38)
                        || DECODE (
                              INSTR (UPPER (ogfi.filename), '.P7M.ZIP'),
                              0, DECODE (
                                    INSTR (UPPER (ogfi.filename), '.P7M'),
                                    0, 'firma=',
                                    'firma=S'),
                              'firma=')
                        || ''',''popup_ServletModulistica'',''toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 1,resizable= 1,copyhistory= 0,width=500,height=300,top=50,left=50'');" ><INPUT CLASS="notEnableFontInputAllegati" id=''OGF'
                        || ogfi.id_oggetto_file
                        || ''' onmouseover="this.style.color=''#FFFFFF'';" onmouseout="this.style.color=''#000000'';" readOnly value="'
                        || f_namefile_ext (filename)
                        || '"></input></a>',
                        '<a href="#" onclick="window.open(''../common/ServletVisualizza.do?ar='
                     || m.area
                     || CHR (38)
                     || 'cm='
                     || m.codice_modello
                     || CHR (38)
                     || 'cr='
                     || fd.codice_richiesta
                     || CHR (38)
                     || 'ca='
                     || ogfi.id_oggetto_file
                     || CHR (38)
                     || 'iddoc='
                     || fd.id_documento
                     || CHR (38)
                     || DECODE (
                           INSTR (UPPER (ogfi.filename), '.P7M.ZIP'),
                           0, DECODE (INSTR (UPPER (ogfi.filename), '.P7M'),
                                      0, 'firma=',
                                      'firma=S'),
                           'firma=')
                     || ''',''popup_ServletModulistica'',''toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 1,resizable= 1,copyhistory= 0,width=500,height=300,top=50,left=50'');" ><INPUT CLASS=notEnableFontInputAllegati id=''OGF'
                     || ogfi.id_oggetto_file
                     || ''' onmouseover="this.style.color=''#FFFFFF'';" onmouseout="this.style.color=''#000000'';" readOnly value="'
                     || f_namefile_ext (filename)
                     || '"></input></a>')
               || '</div>'
                  allegato,
                  '../common/ServletVisualizza.do?ar='
               || m.area
               || CHR (38)
               || 'cm='
               || m.codice_modello
               || CHR (38)
               || 'cr='
               || fd.codice_richiesta
               || CHR (38)
               || 'ca='
               || ogfi.id_oggetto_file
               || CHR (38)
               || 'iddoc='
               || fd.id_documento
               || CHR (38)
               || DECODE (
                     INSTR (UPPER (ogfi.filename), '.P7M.ZIP'),
                     0, DECODE (INSTR (UPPER (ogfi.filename), '.P7M'),
                                0, 'firma=',
                                'firma=S'),
                     'firma=')
                  url,
               f_namefile_ext (filename) src,
               NVL (icona, 'generico.gif') src_icona,
               fd.id_documento,
               ogfi.id_oggetto_file,
               ogfi.filename,
               fofi.nome
          FROM agspr_AGP_FILE_DOC_PRINCIPALE,
               oggetti_file ogfi,
               formati_file fofi,
               documenti fd,
               modelli m
         WHERE     ogfi.id_oggetto_file = id_file_esterno
               AND ogfi.id_formato = fofi.id_formato
               AND fofi.visibile = 'S'
               AND fd.id_tipodoc = m.id_tipodoc
               AND agspr_AGP_FILE_DOC_PRINCIPALE.id_documento_esterno =
                      agspr_AGP_FILE_DOC_PRINCIPALE.id_doc_esterno_agspr_padre
               AND fd.id_documento = ogfi.id_documento
        UNION
        SELECT id_doc_esterno_agspr_padre,
               AGP_FILE_DOC_COLLEGATI_TUTTI.id_documento_esterno
                  id_documento_figlio,
                  '<div valign="middle" class="menuAllegatiLinks" onmouseover="this.style.color=''#FFFFFF'';this.style.background=''#0000A0'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.background=''#0000A0'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.color=''#FFFFFF''" style="BORDER-BOTTOM: 1px outset" onmouseout="this.style.color=''#000000'';this.style.background=''#CCCCCC'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.background=''#CCCCCC'';document.getElementById(''OGF'
               || ogfi.id_oggetto_file
               || ''').style.color=''#000000''"><img alt="'
               || ogfi.filename
               || '" src="../common/images/gdm/formati/'
               || NVL (icona, 'generico.gif')
               || '" width="16" height="16" />'
               || DECODE (
                     INSTR (UPPER (ogfi.filename), '.P7M'),
                     0,    '<a href="#" onclick="window.open(''../common/ServletVisualizza.do?ar='
                        || m.area
                        || CHR (38)
                        || 'cm='
                        || m.codice_modello
                        || CHR (38)
                        || 'cr='
                        || fd.codice_richiesta
                        || CHR (38)
                        || 'ca='
                        || ogfi.id_oggetto_file
                        || CHR (38)
                        || 'iddoc='
                        || fd.id_documento
                        || CHR (38)
                        || DECODE (
                              INSTR (UPPER (ogfi.filename), '.P7M.ZIP'),
                              0, DECODE (
                                    INSTR (UPPER (ogfi.filename), '.P7M'),
                                    0, 'firma=',
                                    'firma=S'),
                              'firma=')
                        || ''',''popup_ServletModulistica'',''toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 1,resizable= 1,copyhistory= 0,width=500,height=300,top=50,left=50'');" ><INPUT CLASS="notEnableFontInputAllegati" id=''OGF'
                        || ogfi.id_oggetto_file
                        || ''' onmouseover="this.style.color=''#FFFFFF'';" onmouseout="this.style.color=''#000000'';" readOnly value="'
                        || f_namefile_ext (ogfi.filename)
                        || '"></input></a>',
                        '<a href="#" onclick="window.open(''../common/ServletVisualizza.do?ar='
                     || m.area
                     || CHR (38)
                     || 'cm='
                     || m.codice_modello
                     || CHR (38)
                     || 'cr='
                     || fd.codice_richiesta
                     || CHR (38)
                     || 'ca='
                     || ogfi.id_oggetto_file
                     || CHR (38)
                     || 'iddoc='
                     || fd.id_documento
                     || CHR (38)
                     || DECODE (
                           INSTR (UPPER (ogfi.filename), '.P7M.ZIP'),
                           0, DECODE (INSTR (UPPER (ogfi.filename), '.P7M'),
                                      0, 'firma=',
                                      'firma=S'),
                           'firma=')
                     || ''',''popup_ServletModulistica'',''toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 1,resizable= 1,copyhistory= 0,width=500,height=300,top=50,left=50'');" ><INPUT CLASS=notEnableFontInputAllegati id=''OGF'
                     || ogfi.id_oggetto_file
                     || ''' onmouseover="this.style.color=''#FFFFFF'';" onmouseout="this.style.color=''#000000'';" readOnly value="'
                     || f_namefile_ext (ogfi.filename)
                     || '"></input></a>')
               || '</div>'
                  allegato,
                  '../common/ServletVisualizza.do?ar='
               || m.area
               || CHR (38)
               || 'cm='
               || m.codice_modello
               || CHR (38)
               || 'cr='
               || fd.codice_richiesta
               || CHR (38)
               || 'ca='
               || ogfi.id_oggetto_file
               || CHR (38)
               || 'iddoc='
               || fd.id_documento
               || CHR (38)
               || DECODE (
                     INSTR (UPPER (ogfi.filename), '.P7M.ZIP'),
                     0, DECODE (INSTR (UPPER (ogfi.filename), '.P7M'),
                                0, 'firma=',
                                'firma=S'),
                     'firma=')
                  url,
               f_namefile_ext (ogfi.filename) src,
               'allegati_doc_figli.png' src_icona,
               fd.id_documento,
               ogfi.id_oggetto_file,
               ogfi.filename,
               fofi.nome
          FROM AGP_FILE_DOC_COLLEGATI_TUTTI,
               oggetti_file ogfi,
               formati_file fofi,
               documenti d,
               documenti fd,
               modelli m
         WHERE     ogfi.id_oggetto_file =
                      AGP_FILE_DOC_COLLEGATI_TUTTI.id_file_esterno
               AND AGP_FILE_DOC_COLLEGATI_TUTTI.valido = 'Y'
               AND ogfi.id_formato = fofi.id_formato
               AND fofi.visibile = 'S'
               AND fd.id_documento = ogfi.id_documento
               AND fd.id_tipodoc = m.id_tipodoc
               AND d.id_documento =
                      AGP_FILE_DOC_COLLEGATI_TUTTI.id_documento_esterno;


