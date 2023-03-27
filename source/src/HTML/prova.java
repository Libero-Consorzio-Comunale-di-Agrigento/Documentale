package HTML;

 import java.util.*;

/*import java.io.PrintWriter;
import java.io.IOException;
import java.net.*;
import java.sql.*;
//import admindblib.*;
import it.finmatica.jfc.dbUtil.*;*/

public class prova{
//private final static String CONTENT_TYPE = "text/html; charset=windows-1252";
  //private final static String INI_FILE_PATH = "C:\\temp\\sportello.ini"; // Inifile di default (se non specificato diversamente tramite parametro in web.xml)
  //private final static String LOG_FILE_PATH = "C:\\temp\\sportello.log"; // Logfile di default (se non specificato diversamente tramite parametro in web.xml)

  //private UserDAC     udac;
  //private String      inifile = INI_FILE_PATH;
  //private String      logfile = LOG_FILE_PATH;
  //private boolean     isVerbose;

//  private String      completeContextURL = null;
//  private String      urlToImg = null;

  public static void main(String[] args)
	
	{
		String [] paths = {
		"t"	, "test", ".", "S:\\SI4\\Sportello\\HTMLTemplate"};
    
		Hashtable tmpl_args = new Hashtable();
		tmpl_args.put("filename","ServletPratiche.tmpl");
		tmpl_args.put("path", paths);/*
    tmpl_args.put("utente","NONE");
    tmpl_args.put("area","AD4");
    tmpl_args.put("tipoPratica","Tipo pratica 1");
    tmpl_args.put("protocollo","ProtocolloMarika");

    Hashtable tmplNo_args = new Hashtable();
    tmpl_args.put("filename","ServletPraticheNegato.tmpl");
		tmpl_args.put("path", paths);
try{


    doGet(tmpl_args,tmplNo_args, request, response);
}catch (Exception ex) {
       
      }
   
		if(args.length==0)    
			tmpl_args.put("filename", "ServletPratiche.tmpl");
		else
			tmpl_args.put("filename", args[0]);

		for(int i=1; i<args.length; i++)
			if(args[i].equals("debug"))
				tmpl_args.put("debug", Boolean.TRUE);
			else if(args[i].equals("case"))
				tmpl_args.put("case_sensitive", Boolean.TRUE);
			else if(args[i].equals("nostrict"))
				tmpl_args.put("strict", Boolean.FALSE);
*/

		try {
			HTML.Template tmpl = new HTML.Template(tmpl_args);
			tmpl.setParam("Full_name", "<Tellis>");
			tmpl.setParam("full_name", "<Philip>");
			//tmpl.setParam("Lists", getLists());
			//tmpl.setParam("Myloop", getLoop());

//tmpl.setParam("titolo", "ServletPratiche");
tmpl.setParam("utente", "NONE");
tmpl.setParam("area", "AD4");
tmpl.setParam("tipoPratica", "Tipo1");

//tmpl.setParam("home", System.getProperty("user.home"));
//tmpl.setParam("cwd", System.getProperty("user.dir"));
      
			System.out.print(tmpl.output());
		} catch(Exception e) {
			System.err.println("Exception: " + e);
		}
	}
}