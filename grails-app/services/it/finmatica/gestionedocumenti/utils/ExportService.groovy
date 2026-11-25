package it.finmatica.gestionedocumenti.utils

import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.usermodel.HSSFRichTextString
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.zkoss.zul.Filedownload

@Service
class ExportService {

    /**
     * Crea e scarica un file di excel costruito usando le regole indicate dal parametro exportOptions.
     * Il file avrà un singolo foglio chiamato "Export" che conterrà le righe della listaDocumenti.
     * @param exportOptions - regole per costruire le right del file. costruita come mappa che indica per ogni dato della listaDocumenti se è da esportare, in che colonna, e che tipo di dato è.
     * ex: [ variabile1 : [esportabile: true/false, label: 'Nome Colonna 1', index: 0, columnType: 'TEXT']
     *      , variabile2 : [esportabile: true/false, label: 'Nome Colonna 2', index: 1, columnType: 'NUMBER']   ]
     * @param listaDocumenti - dati da esportare. strutturata come una lista di mappe (ogni mappa è una riga che associa ad ogni colonna un valore).
     * ex: [ [variabile1 : "A", variabile2 : 1], [variabile1 : "B", variabile2 : 2] ]
     */
    @Transactional(readOnly = true)
    void downloadExcel(def exportOptions, List listaDocumenti) {
        def listaFogli = [Export: listaDocumenti]
        byte[] fileExcel = createExcel(exportOptions, listaFogli)
        InputStream is = new ByteArrayInputStream(fileExcel)
        Filedownload.save(is, "application/vnd.ms-excel", "export_" + new Date().format("yyyyMMddHHmmss") + ".xls")
        is.close()
    }

    /**
     * Crea e scarica un file di excel costruito usando le regole indicate dal parametro exportOptions.
     * Il file può avere multipli fogli, indicati dalle chiavi della mappa listaFogli, i dati di ogni foglio sono contenuti nella lista di dati per quella chiave.
     * @param exportOptions - regole per costruire le right del file. costruita come mappa che indica per ogni dato della listaDocumenti se è da esportare, in che colonna, e che tipo di dato è.
     * ex: [ variabile1 : [esportabile: true/false, label: 'Nome Colonna 1', index: 0, columnType: 'TEXT']
     *      , variabile2 : [esportabile: true/false, label: 'Nome Colonna 2', index: 1, columnType: 'TEXT']   ]
     * @param listaFogli - dati da esportare. strutturata come una mappa, in cui ogni entry indica il nome di una sheet, e la lista di righe contenute in quella sheet.
     * Le righe hanno la stessa struttura della listaDocumenti.
     * ex: [ Foglio_1 : [ [variabile1 : "A", variabile2 : 1], [variabile1 : "B", variabile2 : 2] ]
     *      , Foglio_2 : [ [variabile1 : "C", variabile2 : 3], [variabile1 : "D", variabile2 : 4] ] ]
     */
    @Transactional(readOnly = true)
    void downloadExcel(def exportOptions, Map<String,List> listaFogli) {
        byte[] fileExcel = createExcel(exportOptions, listaFogli)
        InputStream is = new ByteArrayInputStream(fileExcel)
        Filedownload.save(is, "application/vnd.ms-excel", "export_" + new Date().format("yyyyMMddHHmmss") + ".xls")
        is.close()
    }

    // Feature #73156 - aggiunta la possibilità di avere multiple sheet per file.
    private byte[] createExcel(def exportOptions, Map<String, List> listaFogli) {

        Workbook wb = new HSSFWorkbook()
        CreationHelper createHelper = wb.getCreationHelper()
        listaFogli.keySet().each { sheetName ->
            Sheet sheet = wb.createSheet(sheetName)
            def listaDocumenti = listaFogli[sheetName]

            Row testata = sheet.createRow((short) 0)

            Font font = wb.createFont()
            font.setBoldweight(Font.BOLDWEIGHT_BOLD)
            CellStyle style = wb.createCellStyle()
            style.setFont(font)

            Set set = exportOptions.entrySet()
            Iterator iterator = set.iterator()

            int columns = 0
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next()
                def map = me.getValue()
                if (map.esportabile) {
                    columns++
                    Cell cell = testata.createCell(map.index)
                    cell.setCellValue(map.label)
                    cell.setCellStyle(style)
                }
            }

            for (int num_row = 0; num_row < listaDocumenti.size(); num_row++) {
                def rows = listaDocumenti.get(num_row)
                Set columnSet = rows.entrySet()

                Row row = sheet.createRow((short) num_row + 1)
                for (Iterator columnIterator = columnSet.iterator(); columnIterator.hasNext();) {
                    Map.Entry column = (Map.Entry) columnIterator.next()
                    if (exportOptions[column.getKey()].esportabile == true) {
                        Cell cell = row.createCell(exportOptions[column.getKey()].index)
                        String key = column.getKey()
                        String value = column.getValue()
                        if (value != null && value.length() > 0) {
                            switch (exportOptions[key].columnType) {
                                case 'DATE':
                                    try {
                                        HSSFCellStyle cellStyle = wb.createCellStyle()
                                        cellStyle.setDataFormat(wb.createDataFormat().getFormat(exportOptions[key].formato))
                                        cell.setCellStyle(cellStyle)
                                        cell.setCellValue(new Date(column.getValue().getTime()))

                                    } catch (Exception e) {
                                        cell.setCellValue(new HSSFRichTextString(value))
                                    }
                                    break
                                case 'NUMBER':
                                    try {
                                        cell.setCellType(Cell.CELL_TYPE_NUMERIC)
                                        cell.setCellValue(column.getValue())
                                    } catch (Exception e) {
                                        cell.setCellValue(new HSSFRichTextString(value))
                                    }
                                    break
                                case 'BOOLEAN':
                                    try {
                                        //cell.setCellType(Cell.CELL_TYPE_BOOLEAN)
                                        //cell.setCellValue(column.getValue());
                                        if (column.getValue()) {
                                            cell.setCellValue(new HSSFRichTextString("Si"))
                                        } else {
                                            cell.setCellValue(new HSSFRichTextString("No"))
                                        }

                                    } catch (Exception e) {
                                        cell.setCellValue(new HSSFRichTextString(value))
                                    }
                                    break
                                case 'TEXT':
                                default:
                                    cell.setCellValue(new HSSFRichTextString(value))
                                    break
                            }
                            HSSFCellStyle cellStyle = cell.getCellStyle()
                            if (cellStyle == null) {
                                cellStyle = wb.createCellStyle()
                                cell.setCellStyle(cellStyle)
                            }
                            cellStyle.setWrapText(true)
                        }
                    }
                }
            }

            for (int i = 0; i < columns; i++) {
                sheet.autoSizeColumn(i)
            }
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        wb.write(bos)
        bos.close()
        return bos.toByteArray()
    }
}

