package it.finmatica.dmServer.testColleghi;

import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import java.sql.Connection;

//import it.finmatica.privati.struttura.Motivo;

public class TestIQueryCollection {

    public static void main(String[] args) throws Exception {
        Connection conn = null;

        // Class.forName("oracle.jdbc.driver.OracleDriver");

        //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.30.20:1521:orcl","GDM","GDM");

        //CONNESSIONE SICILIA
        //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.5:1521:si3","GDM","GDM");

        //JVMSEGR
        //conn=DriverManager.getConnection("jdbc:oracle:thin:@10.97.11.19:1521:PRMOD","GDM","GDM");

        //DBSEGR

        //   conn=DriverManager.getConnection("jdbc:oracle:thin:@10.98.0.11:1521:ORCL","GDM","GDM");

        // System.out.println(conn.getMetaData().getURL());

        SessioneDb.getInstance().addAlias(Global.ALIAS_ORACLE,
            Global.DRIVER_ORACLE);
        IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL("oracle.",
            "jdbc:oracle:thin:@svi-ora04:1521:JSUITE",
            "GDM",
            "GDM");
              
             
            /*  ResultSetIQuery rst= Motivo.trovaMotiviXTipoDocumento("CYB01",dbOp,"ROMAGNOL");
              
              if (rst.next()) {
            	  rst.get("CODICE_MOTIVO","ROMEO_GESTIONI","MOTIVO");
            	  rst.get("DESCR_MOTIVO","ROMEO_GESTIONI","MOTIVO");
            	  if (!rst.next()) {
            		  System.out.println("AAA");
            	  }
              }
              System.out.println("BB");*/

        //System.out.println(conn.getMetaData().);
             
              /*OracleConnection oc = (OracleConnection)conn;
             
             System.out.println(oc.getPhysicalConnection().getProperties());

              
              Properties info = new Properties();
              Driver driver = DriverManager.getDriver("jdbc:oracle:thin:@10.98.0.11:1521:ORCL");
              System.out.println("driver=" + driver);
              DriverPropertyInfo[] attributes = driver.getPropertyInfo("jdbc:oracle:thin:@10.98.0.11:1521:ORCL", info);
              System.out.println("attributes=" + attributes);*/
        // zero length means a connection attempt can be made
           /*   System.out.println("Resolving properties for: " + driver.getClass().getName());

              for (int i = 0; i < attributes.length; i++) {
                // get the property metadata
                String name = attributes[i].name;
                String[] choices = attributes[i].choices;
                boolean required = attributes[i].required;
                String description = attributes[i].description;
                // printout property metadata
                System.out.println(name + " (Required: " + required + ")");
                if (choices == null) {
                  System.out.println(" No choices.");
                } else {
                  System.out.print(" Choices are: ");
                  for (int j = 0; j < choices.length; j++) {
                    System.out.print(" " + choices[j]);
                  }
                }
                System.out.println(" Description: " + description);
              }*/

        //System.out.println(conn.getMetaData().);
        //  System.out.println(conn.getMetaData().getConnection().get);
             
              /*IQuery Iq = new IQuery();  
              Iq.initVarEnv("GDM","GDM", conn);
              Iq.setAccessProfile(false);
              Iq.setInstanceProfile(false);
              Iq.escludiControlloCompetenze(true);*/
        //Iq.settaArea("SEGRETERIA.PROTOCOLLO");
        // Iq.addCampo("IDRIF","7825","SEGRETERIA","M_SOGGETTO");
        // Iq.settaIdDocumentoRicerca("19");
        // Iq.addCampoReturn("COGNOME","SEGRETERIA","M_SOGGETTO");
        //  Iq.addCampoReturn("NOME","SEGRETERIA","M_SOGGETTO");
                            
            /*  IQuery Iq2 = new IQuery();  
              Iq2.initVarEnv("GDM","GDM", conn);
              Iq2.setAccessProfile(false);
              Iq2.setInstanceProfile(false);
              Iq2.escludiControlloCompetenze(true);*/
        //Iq2.settaArea("SEGRETERIA.PROTOCOLLO");
        //  Iq2.addCampo("IDRIF","8048","SEGRETERIA","M_SOGGETTO");
        // Iq.settaIdDocumentoRicerca("19");
            /*    Iq2.addCampoReturn("COGNOME","SEGRETERIA","M_SOGGETTO");
              Iq2.addCampoReturn("NOME","SEGRETERIA","M_SOGGETTO");

              IQuery iqRicerca;
              IQueryCollection Iqc = new IQueryCollection();  
              Iqc.initVarEnv("GDM","GDM", conn);
              Iqc.addIQuery(Iq2); 
              Iqc.addIQuery(Iq);
              iqRicerca = Iqc.getIQueryRicerca();
              
              if (iqRicerca.ricerca().booleanValue()) { 
            	  System.out.println("N. Totale->"+iqRicerca.getProfileNumber());
                               
                  ResultSetIQuery  rst = iqRicerca.getResultSet();
                  
                  while (rst.next()) {
                        System.out.println("id--->"+rst.getId());
                        System.out.println("cr--->"+rst.getCr());
                        System.out.println("cognome--->"+rst.get("COGNOME","SEGRETERIA","M_SOGGETTO"));
                        System.out.println("nome--->"+rst.get("NOME","SEGRETERIA","M_SOGGETTO"));
                  }
             }
              else
            	   System.out.println("Errore->"+iqRicerca.getError());           */
    }
}
