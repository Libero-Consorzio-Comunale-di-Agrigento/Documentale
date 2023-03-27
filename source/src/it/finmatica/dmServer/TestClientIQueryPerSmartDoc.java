package it.finmatica.dmServer;

import it.finmatica.dmServer.management.IQuery;
import java.sql.Connection;
import java.sql.DriverManager;

public class TestClientIQueryPerSmartDoc {
    public static void main(String[] args) throws Exception
    {
        Connection conn= null;
        Class.forName("oracle.jdbc.driver.OracleDriver");

        //as02
        conn= DriverManager.getConnection("jdbc:oracle:thin:@test-agspr-db02.finmatica.local:1521:ORCL","GDM","GDM");

        IQuery Iq = new IQuery();
        Iq.initVarEnv(/*vu*/"GDM","", conn);

        Iq.escludiControlloCompetenze(true);

        Iq.addCampoCategoria("DATA_RICERCA", "" + "02/01/2023", "02/02/2023", "CLASSIFICABILE");
        //Iq.addCampoCategoria("CLASS_COD", "" + "01-01", "=", "CLASSIFICABILE");
        //Iq.addCampoCategoria("FASCIOLO_ANNO", "" + "2022", "=", "CLASSIFICABILE");
        //Iq.addCampoCategoria("FASCIOLO_NUMERO", "" + "2", "=", "CLASSIFICABILE");

        Iq.addCampoOrdinamentoAsc("ANNO", "CLASSIFICABILE");
        //Bug #62600 Errore, se inserisco questo si rompe
        //Iq.addCampoOrdinamentoDescConFormato("DATA", "CLASSIFICABILE", "dd/mm/yyyy HH:mi:ss");
        //Iq.addCampoOrdinamentoAscConFormato("DATA", "CLASSIFICABILE", "dd/mm/yyyy HH:mi:ss");


        Iq.addCampoOrdinamentoAscConFormato("DATA", "CLASSIFICABILE", "dd/mm/yyyy HH:mi:ss");

        Iq.addCampoReturn("DESCRIZIONE_TIPO_REGISTRO", "CLASSIFICABILE");
        Iq.addCampoReturn("ANNO", "CLASSIFICABILE");
        Iq.addCampoReturn("NUMERO", "CLASSIFICABILE");

        //2. PROVA ANCHE QUESTO QUESTO
        Iq.addCampoReturnConFormato("DATA", "CLASSIFICABILE","dd/mm/yyyy HH:mi:ss");

        Iq.addCampoReturn("OGGETTO", "CLASSIFICABILE");
        Iq.addCampoReturn("FASCICOLO_NUMERO", "CLASSIFICABILE");
        Iq.addCampoReturn("FASCICOLO_ANNO", "CLASSIFICABILE");
        Iq.addCampoReturn("CLASS_COD", "CLASSIFICABILE");
        Iq.addCampoReturn("NUMERO_CONTRATTO", "CLASSIFICABILE");
        Iq.addCampoReturn("ANNO_CONTRATTO", "CLASSIFICABILE");
        Iq.addCampoReturn("DATA_STIPULA", "CLASSIFICABILE");
        Iq.addCampoReturn("CIG", "CLASSIFICABILE");

        //Iq.setTipoRicercaDefault("H");
        System.out.println(Iq.ricerca());



        System.out.println(Iq.getSqlQuery());
        System.out.println(Iq.getError());
    }
}
