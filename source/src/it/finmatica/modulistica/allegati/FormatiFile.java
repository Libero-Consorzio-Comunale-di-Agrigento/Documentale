package it.finmatica.modulistica.allegati;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.helpers.DefaultHandler;

public class FormatiFile {
  public static final String MIME_APPLICATION_ANDREW_INSET  = "application/andrew-inset";
  public static final String MIME_APPLICATION_JSON      = "application/json";
  public static final String MIME_APPLICATION_ZIP       = "application/zip";
  public static final String MIME_APPLICATION_X_GZIP      = "application/x-gzip";
  public static final String MIME_APPLICATION_TGZ       = "application/tgz";
  public static final String MIME_APPLICATION_MSWORD      = "application/msword";
  public static final String MIME_APPLICATION_POSTSCRIPT    = "application/postscript";
  public static final String MIME_APPLICATION_PDF       = "application/pdf";
  public static final String MIME_APPLICATION_JNLP      = "application/jnlp";
  public static final String MIME_APPLICATION_MAC_BINHEX40  = "application/mac-binhex40";
  public static final String MIME_APPLICATION_MAC_COMPACTPRO  = "application/mac-compactpro";
  public static final String MIME_APPLICATION_MATHML_XML    = "application/mathml+xml";
  public static final String MIME_APPLICATION_OCTET_STREAM  = "application/octet-stream";
  public static final String MIME_APPLICATION_ODA       = "application/oda";
  public static final String MIME_APPLICATION_RDF_XML     = "application/rdf+xml";
  public static final String MIME_APPLICATION_JAVA_ARCHIVE  = "application/java-archive";
  public static final String MIME_APPLICATION_RDF_SMIL    = "application/smil";
  public static final String MIME_APPLICATION_SRGS      = "application/srgs";
  public static final String MIME_APPLICATION_SRGS_XML    = "application/srgs+xml";
  public static final String MIME_APPLICATION_VND_MIF     = "application/vnd.mif";
  public static final String MIME_APPLICATION_VND_MSEXCEL   = "application/vnd.ms-excel";
  public static final String MIME_APPLICATION_VND_MSPOWERPOINT= "application/vnd.ms-powerpoint";
  public static final String MIME_APPLICATION_VND_RNREALMEDIA = "application/vnd.rn-realmedia";
  public static final String MIME_APPLICATION_X_BCPIO     = "application/x-bcpio";
  public static final String MIME_APPLICATION_X_CDLINK    = "application/x-cdlink";
  public static final String MIME_APPLICATION_X_CHESS_PGN   = "application/x-chess-pgn";
  public static final String MIME_APPLICATION_X_CPIO      = "application/x-cpio";
  public static final String MIME_APPLICATION_X_CSH     = "application/x-csh";
  public static final String MIME_APPLICATION_X_DIRECTOR    = "application/x-director";
  public static final String MIME_APPLICATION_X_DVI     = "application/x-dvi";
  public static final String MIME_APPLICATION_X_FUTURESPLASH  = "application/x-futuresplash";
  public static final String MIME_APPLICATION_X_GTAR      = "application/x-gtar";
  public static final String MIME_APPLICATION_X_HDF     = "application/x-hdf";
  public static final String MIME_APPLICATION_X_JAVASCRIPT  = "application/x-javascript";
  public static final String MIME_APPLICATION_X_KOAN      = "application/x-koan";
  public static final String MIME_APPLICATION_X_LATEX     = "application/x-latex";
  public static final String MIME_APPLICATION_X_NETCDF    = "application/x-netcdf";
  public static final String MIME_APPLICATION_X_OGG     = "application/x-ogg";
  public static final String MIME_APPLICATION_X_SH      = "application/x-sh";
  public static final String MIME_APPLICATION_X_SHAR      = "application/x-shar";
  public static final String MIME_APPLICATION_X_SHOCKWAVE_FLASH = "application/x-shockwave-flash";
  public static final String MIME_APPLICATION_X_STUFFIT     = "application/x-stuffit";
  public static final String MIME_APPLICATION_X_SV4CPIO     = "application/x-sv4cpio";
  public static final String MIME_APPLICATION_X_SV4CRC    = "application/x-sv4crc";
  public static final String MIME_APPLICATION_X_TAR       = "application/x-tar";
  public static final String MIME_APPLICATION_X_RAR_COMPRESSED= "application/x-rar-compressed";
  public static final String MIME_APPLICATION_X_TCL       = "application/x-tcl";
  public static final String MIME_APPLICATION_X_TEX       = "application/x-tex";
  public static final String MIME_APPLICATION_X_TEXINFO   = "application/x-texinfo";
  public static final String MIME_APPLICATION_X_TROFF     = "application/x-troff";
  public static final String MIME_APPLICATION_X_TROFF_MAN   = "application/x-troff-man";
  public static final String MIME_APPLICATION_X_TROFF_ME    = "application/x-troff-me";
  public static final String MIME_APPLICATION_X_TROFF_MS    = "application/x-troff-ms";
  public static final String MIME_APPLICATION_X_USTAR     = "application/x-ustar";
  public static final String MIME_APPLICATION_X_WAIS_SOURCE = "application/x-wais-source";
  public static final String MIME_APPLICATION_VND_MOZZILLA_XUL_XML = "application/vnd.mozilla.xul+xml";
  public static final String MIME_APPLICATION_XHTML_XML     = "application/xhtml+xml";
  public static final String MIME_APPLICATION_XSLT_XML    = "application/xslt+xml";
  public static final String MIME_APPLICATION_XML       = "application/xml";
  public static final String MIME_APPLICATION_XML_DTD     = "application/xml-dtd";
  public static final String MIME_IMAGE_BMP         = "image/bmp";
  public static final String MIME_IMAGE_CGM         = "image/cgm";
  public static final String MIME_IMAGE_GIF         = "image/gif";
  public static final String MIME_IMAGE_IEF         = "image/ief";
  public static final String MIME_IMAGE_JPEG          = "image/jpeg";
  public static final String MIME_IMAGE_TIFF          = "image/tiff";
  public static final String MIME_IMAGE_PNG         = "image/png";
  public static final String MIME_IMAGE_SVG_XML       = "image/svg+xml";
  public static final String MIME_IMAGE_VND_DJVU        = "image/vnd.djvu";
  public static final String MIME_IMAGE_WAP_WBMP        = "image/vnd.wap.wbmp";
  public static final String MIME_IMAGE_X_CMU_RASTER      = "image/x-cmu-raster";
  public static final String MIME_IMAGE_X_ICON        = "image/x-icon";
  public static final String MIME_IMAGE_X_PORTABLE_ANYMAP   = "image/x-portable-anymap";
  public static final String MIME_IMAGE_X_PORTABLE_BITMAP   = "image/x-portable-bitmap";
  public static final String MIME_IMAGE_X_PORTABLE_GRAYMAP  = "image/x-portable-graymap";
  public static final String MIME_IMAGE_X_PORTABLE_PIXMAP   = "image/x-portable-pixmap";
  public static final String MIME_IMAGE_X_RGB         = "image/x-rgb";
  public static final String MIME_AUDIO_BASIC         = "audio/basic";
  public static final String MIME_AUDIO_MIDI          = "audio/midi";
  public static final String MIME_AUDIO_MPEG          = "audio/mpeg";
  public static final String MIME_AUDIO_X_AIFF        = "audio/x-aiff";
  public static final String MIME_AUDIO_X_MPEGURL       = "audio/x-mpegurl";
  public static final String MIME_AUDIO_X_PN_REALAUDIO    = "audio/x-pn-realaudio";
  public static final String MIME_AUDIO_X_WAV         = "audio/x-wav";
  public static final String MIME_CHEMICAL_X_PDB        = "chemical/x-pdb";
  public static final String MIME_CHEMICAL_X_XYZ        = "chemical/x-xyz";
  public static final String MIME_MODEL_IGES          = "model/iges";
  public static final String MIME_MODEL_MESH          = "model/mesh";
  public static final String MIME_MODEL_VRLM          = "model/vrml";
  public static final String MIME_TEXT_PLAIN          = "text/plain";
  public static final String MIME_TEXT_RICHTEXT       = "text/richtext";
  public static final String MIME_TEXT_RTF          = "text/rtf";
  public static final String MIME_TEXT_HTML         = "text/html";
  public static final String MIME_TEXT_CALENDAR       = "text/calendar";
  public static final String MIME_TEXT_CSS          = "text/css";
  public static final String MIME_TEXT_SGML         = "text/sgml";
  public static final String MIME_TEXT_TAB_SEPARATED_VALUES = "text/tab-separated-values";
  public static final String MIME_TEXT_VND_WAP_XML      = "text/vnd.wap.wml";
  public static final String MIME_TEXT_VND_WAP_WMLSCRIPT    = "text/vnd.wap.wmlscript";
  public static final String MIME_TEXT_X_SETEXT       = "text/x-setext";
  public static final String MIME_TEXT_X_COMPONENT      = "text/x-component";
  public static final String MIME_VIDEO_QUICKTIME       = "video/quicktime";
  public static final String MIME_VIDEO_MPEG          = "video/mpeg";
  public static final String MIME_VIDEO_VND_MPEGURL     = "video/vnd.mpegurl";
  public static final String MIME_VIDEO_X_MSVIDEO       = "video/x-msvideo";
  public static final String MIME_VIDEO_X_MS_WMV        = "video/x-ms-wmv";
  public static final String MIME_VIDEO_X_SGI_MOVIE     = "video/x-sgi-movie";
  public static final String MIME_X_CONFERENCE_X_COOLTALK   = "x-conference/x-cooltalk";
  public static final String MIME_HTML_XML   				= "application/xhtml+xml";
  public static final String MIME_MAIL_EML   				= "message/rfc822";
  
	private LinkedList<String> formato 					= null;
	private LinkedList<String> fileMime					= null;

  /**
   * Costruttore
   * 
  */
	public FormatiFile() {
		formato 				= new LinkedList<String>();
		fileMime 				= new LinkedList<String>();
		init();
	}

	public void addSegnalazione(String p_formato, 
			String p_fileMime) {
		formato.add(p_formato);
		fileMime.add(p_fileMime);
	}
	
	private void init() {
		addSegnalazione("PDF.P7M", "application/x-pkcs7-mime");
		addSegnalazione("DOC.P7M", "application/x-pkcs7-mime");
		addSegnalazione("DWG", "application/dwg");
		addSegnalazione("mp4", "video/mp4");
		addSegnalazione("js", "application/javascript");
		addSegnalazione("wsdl", "application/wsdl+xml");
		addSegnalazione("ODT", "application/vnd.oasis.opendocument.text");
		addSegnalazione("3gp", "video/3gpp");
		addSegnalazione("log", "text/x-log");
		addSegnalazione("ods", "application/vnd.oasis.opendocument.spreadsheet");
		addSegnalazione("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		addSegnalazione("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		addSegnalazione("java", "text/x-java-source");
		addSegnalazione("xsd", "application/xml");
		addSegnalazione("apk", "application/vnd.android.package-archive");
		addSegnalazione("csv", "text/csv");

		
		addSegnalazione("xul", MIME_APPLICATION_VND_MOZZILLA_XUL_XML);
		addSegnalazione("json", MIME_APPLICATION_JSON);
		addSegnalazione("ice", MIME_X_CONFERENCE_X_COOLTALK);
		addSegnalazione("movie", MIME_VIDEO_X_SGI_MOVIE);
		addSegnalazione("avi", MIME_VIDEO_X_MSVIDEO);
		addSegnalazione("wmv", MIME_VIDEO_X_MS_WMV);
		addSegnalazione("m4u", MIME_VIDEO_VND_MPEGURL);
		addSegnalazione("mxu", MIME_VIDEO_VND_MPEGURL);
		addSegnalazione("htc", MIME_TEXT_X_COMPONENT);
		addSegnalazione("etx", MIME_TEXT_X_SETEXT);
		addSegnalazione("wmls", MIME_TEXT_VND_WAP_WMLSCRIPT);
		addSegnalazione("wml", MIME_TEXT_VND_WAP_XML);
		addSegnalazione("tsv", MIME_TEXT_TAB_SEPARATED_VALUES);
		addSegnalazione("sgm", MIME_TEXT_SGML);
		addSegnalazione("sgml", MIME_TEXT_SGML);
		addSegnalazione("css", MIME_TEXT_CSS);
		addSegnalazione("ifb", MIME_TEXT_CALENDAR);
		addSegnalazione("ics", MIME_TEXT_CALENDAR);
		addSegnalazione("wrl", MIME_MODEL_VRLM);
		addSegnalazione("vrlm", MIME_MODEL_VRLM);
		addSegnalazione("silo", MIME_MODEL_MESH);
    addSegnalazione("mesh", MIME_MODEL_MESH);
    addSegnalazione("msh", MIME_MODEL_MESH);
    addSegnalazione("iges", MIME_MODEL_IGES);
    addSegnalazione("igs", MIME_MODEL_IGES);
    addSegnalazione("rgb", MIME_IMAGE_X_RGB);
    addSegnalazione("ppm", MIME_IMAGE_X_PORTABLE_PIXMAP);
    addSegnalazione("pgm", MIME_IMAGE_X_PORTABLE_GRAYMAP);
    addSegnalazione("pbm", MIME_IMAGE_X_PORTABLE_BITMAP);
    addSegnalazione("pnm", MIME_IMAGE_X_PORTABLE_ANYMAP);
    addSegnalazione("ico", MIME_IMAGE_X_ICON);
    addSegnalazione("ras", MIME_IMAGE_X_CMU_RASTER);
    addSegnalazione("wbmp", MIME_IMAGE_WAP_WBMP);
    addSegnalazione("djv", MIME_IMAGE_VND_DJVU);
    addSegnalazione("djvu", MIME_IMAGE_VND_DJVU);
    addSegnalazione("svg", MIME_IMAGE_SVG_XML);
    addSegnalazione("ief", MIME_IMAGE_IEF);
    addSegnalazione("cgm", MIME_IMAGE_CGM);
    addSegnalazione("bmp", MIME_IMAGE_BMP);
    addSegnalazione("xyz", MIME_CHEMICAL_X_XYZ);
    addSegnalazione("pdb", MIME_CHEMICAL_X_PDB);
    addSegnalazione("ra", MIME_AUDIO_X_PN_REALAUDIO);
    addSegnalazione("ram", MIME_AUDIO_X_PN_REALAUDIO);
    addSegnalazione("m3u", MIME_AUDIO_X_MPEGURL);
    addSegnalazione("aifc", MIME_AUDIO_X_AIFF);
    addSegnalazione("aif", MIME_AUDIO_X_AIFF);
    addSegnalazione("aiff", MIME_AUDIO_X_AIFF);
    addSegnalazione("mp3", MIME_AUDIO_MPEG);
    addSegnalazione("mp2", MIME_AUDIO_MPEG);
    addSegnalazione("mp1", MIME_AUDIO_MPEG);
    addSegnalazione("mpga", MIME_AUDIO_MPEG);
    addSegnalazione("kar", MIME_AUDIO_MIDI);
    addSegnalazione("mid", MIME_AUDIO_MIDI);
    addSegnalazione("midi", MIME_AUDIO_MIDI);
    addSegnalazione("dtd", MIME_APPLICATION_XML_DTD);
    addSegnalazione("xsl", MIME_APPLICATION_XML);
    addSegnalazione("xml", MIME_APPLICATION_XML);
    addSegnalazione("xslt", MIME_APPLICATION_XSLT_XML);
    addSegnalazione("xht", MIME_APPLICATION_XHTML_XML);
    addSegnalazione("xhtml", MIME_APPLICATION_XHTML_XML);
    addSegnalazione("src", MIME_APPLICATION_X_WAIS_SOURCE);
    addSegnalazione("ustar", MIME_APPLICATION_X_USTAR);
    addSegnalazione("ms", MIME_APPLICATION_X_TROFF_MS);
    addSegnalazione("me", MIME_APPLICATION_X_TROFF_ME);
    addSegnalazione("man", MIME_APPLICATION_X_TROFF_MAN);
    addSegnalazione("roff", MIME_APPLICATION_X_TROFF);
    addSegnalazione("tr", MIME_APPLICATION_X_TROFF);
    addSegnalazione("t", MIME_APPLICATION_X_TROFF);
    addSegnalazione("texi", MIME_APPLICATION_X_TEXINFO);
    addSegnalazione("texinfo", MIME_APPLICATION_X_TEXINFO);
    addSegnalazione("tex", MIME_APPLICATION_X_TEX);
    addSegnalazione("tcl", MIME_APPLICATION_X_TCL);
    addSegnalazione("sv4crc", MIME_APPLICATION_X_SV4CRC);
    addSegnalazione("sv4cpio", MIME_APPLICATION_X_SV4CPIO);
    addSegnalazione("sit", MIME_APPLICATION_X_STUFFIT);
    addSegnalazione("swf", MIME_APPLICATION_X_SHOCKWAVE_FLASH);
    addSegnalazione("shar", MIME_APPLICATION_X_SHAR);
    addSegnalazione("sh", MIME_APPLICATION_X_SH);
    addSegnalazione("cdf", MIME_APPLICATION_X_NETCDF);
    addSegnalazione("nc", MIME_APPLICATION_X_NETCDF);
    addSegnalazione("latex", MIME_APPLICATION_X_LATEX);
    addSegnalazione("skm", MIME_APPLICATION_X_KOAN);
    addSegnalazione("skt", MIME_APPLICATION_X_KOAN);
    addSegnalazione("skd", MIME_APPLICATION_X_KOAN);
    addSegnalazione("skp", MIME_APPLICATION_X_KOAN);
    addSegnalazione("js", MIME_APPLICATION_X_JAVASCRIPT);
    addSegnalazione("hdf", MIME_APPLICATION_X_HDF);
    addSegnalazione("gtar", MIME_APPLICATION_X_GTAR);
    addSegnalazione("spl", MIME_APPLICATION_X_FUTURESPLASH);
    addSegnalazione("dvi", MIME_APPLICATION_X_DVI);
    addSegnalazione("dxr", MIME_APPLICATION_X_DIRECTOR);
    addSegnalazione("dir", MIME_APPLICATION_X_DIRECTOR);
    addSegnalazione("dcr", MIME_APPLICATION_X_DIRECTOR);
    addSegnalazione("csh", MIME_APPLICATION_X_CSH);
    addSegnalazione("cpio", MIME_APPLICATION_X_CPIO);
    addSegnalazione("pgn", MIME_APPLICATION_X_CHESS_PGN);
    addSegnalazione("vcd", MIME_APPLICATION_X_CDLINK);
    addSegnalazione("bcpio", MIME_APPLICATION_X_BCPIO);
    addSegnalazione("rm", MIME_APPLICATION_VND_RNREALMEDIA);
    addSegnalazione("ppt", MIME_APPLICATION_VND_MSPOWERPOINT);
    addSegnalazione("mif", MIME_APPLICATION_VND_MIF);
    addSegnalazione("grxml", MIME_APPLICATION_SRGS_XML);
    addSegnalazione("gram", MIME_APPLICATION_SRGS);
    addSegnalazione("smil", MIME_APPLICATION_RDF_SMIL);
    addSegnalazione("smi", MIME_APPLICATION_RDF_SMIL);
    addSegnalazione("rdf", MIME_APPLICATION_RDF_XML);
    addSegnalazione("ogg", MIME_APPLICATION_X_OGG);
    addSegnalazione("oda", MIME_APPLICATION_ODA);
    addSegnalazione("dmg", MIME_APPLICATION_OCTET_STREAM);
    addSegnalazione("lzh", MIME_APPLICATION_OCTET_STREAM);
    addSegnalazione("so", MIME_APPLICATION_OCTET_STREAM);
    addSegnalazione("lha", MIME_APPLICATION_OCTET_STREAM);
    addSegnalazione("dms", MIME_APPLICATION_OCTET_STREAM);
    addSegnalazione("bin", MIME_APPLICATION_OCTET_STREAM);
    addSegnalazione("mathml", MIME_APPLICATION_MATHML_XML);
    addSegnalazione("cpt", MIME_APPLICATION_MAC_COMPACTPRO);
    addSegnalazione("hqx", MIME_APPLICATION_MAC_BINHEX40);
    addSegnalazione("jnlp", MIME_APPLICATION_JNLP);
    addSegnalazione("ez", MIME_APPLICATION_ANDREW_INSET);
    addSegnalazione("txt", MIME_TEXT_PLAIN);
		addSegnalazione("text", MIME_TEXT_PLAIN);
    addSegnalazione("ini", MIME_TEXT_PLAIN);
    addSegnalazione("c", MIME_TEXT_PLAIN);
    addSegnalazione("h", MIME_TEXT_PLAIN);
    addSegnalazione("cpp", MIME_TEXT_PLAIN);
    addSegnalazione("cxx", MIME_TEXT_PLAIN);
    addSegnalazione("cc", MIME_TEXT_PLAIN);
    addSegnalazione("chh", MIME_TEXT_PLAIN);
    addSegnalazione("java", MIME_TEXT_PLAIN);
    addSegnalazione("csv", MIME_TEXT_PLAIN);
    addSegnalazione("bat", MIME_TEXT_PLAIN);
    addSegnalazione("cmd", MIME_TEXT_PLAIN);
    addSegnalazione("asc", MIME_TEXT_PLAIN);
    addSegnalazione("rtf", MIME_TEXT_RTF);
    addSegnalazione("rtx", MIME_TEXT_RICHTEXT);
    addSegnalazione("html", MIME_TEXT_HTML);
    addSegnalazione("htm", MIME_TEXT_HTML);
    addSegnalazione("zip", MIME_APPLICATION_ZIP);
    addSegnalazione("rar", MIME_APPLICATION_X_RAR_COMPRESSED);
    addSegnalazione("gzip", MIME_APPLICATION_X_GZIP);
    addSegnalazione("gz", MIME_APPLICATION_X_GZIP);
    addSegnalazione("tgz", MIME_APPLICATION_TGZ);
    addSegnalazione("tar", MIME_APPLICATION_X_TAR);
    addSegnalazione("gif", MIME_IMAGE_GIF);
    addSegnalazione("jpeg", MIME_IMAGE_JPEG);
    addSegnalazione("jpg", MIME_IMAGE_JPEG);
    addSegnalazione("jpe", MIME_IMAGE_JPEG);
    addSegnalazione("tiff", MIME_IMAGE_TIFF);
    addSegnalazione("tif", MIME_IMAGE_TIFF);
    addSegnalazione("png", MIME_IMAGE_PNG);
    addSegnalazione("au", MIME_AUDIO_BASIC);
    addSegnalazione("snd", MIME_AUDIO_BASIC);
    addSegnalazione("wav", MIME_AUDIO_X_WAV);
    addSegnalazione("mov", MIME_VIDEO_QUICKTIME);
    addSegnalazione("qt", MIME_VIDEO_QUICKTIME);
    addSegnalazione("mpeg", MIME_VIDEO_MPEG);
    addSegnalazione("mpg", MIME_VIDEO_MPEG);
    addSegnalazione("mpe", MIME_VIDEO_MPEG);
    addSegnalazione("abs", MIME_VIDEO_MPEG);
    addSegnalazione("doc", MIME_APPLICATION_MSWORD);
    addSegnalazione("xls", MIME_APPLICATION_VND_MSEXCEL);
    addSegnalazione("eps", MIME_APPLICATION_POSTSCRIPT);
    addSegnalazione("ai", MIME_APPLICATION_POSTSCRIPT);
    addSegnalazione("ps", MIME_APPLICATION_POSTSCRIPT);
    addSegnalazione("pdf", MIME_APPLICATION_PDF);
    addSegnalazione("exe", MIME_APPLICATION_OCTET_STREAM);
    addSegnalazione("dll", MIME_APPLICATION_OCTET_STREAM);
    addSegnalazione("class", MIME_APPLICATION_OCTET_STREAM);
    addSegnalazione("jar", MIME_APPLICATION_JAVA_ARCHIVE);
		addSegnalazione("html", MIME_HTML_XML);
		addSegnalazione("eml", MIME_MAIL_EML);
		
	}
	
	public String getFileMime(String p_formato) {
		int posizione = 0;
		boolean trovato = false;

		while (posizione < formato.size() && !trovato) {
			if (formato.get(posizione).equalsIgnoreCase(p_formato)) {
				trovato = true;
			} else {
				posizione++;
			}
		}
		if (!trovato) {
			posizione = -1;
		}
		if (posizione < fileMime.size() && posizione> -1) {
			return fileMime.get(posizione);
		} else {
			return "";
		}
	}
	
	public boolean verificaFile(File fUp) throws Exception {
		String nomefile = fUp.getName();
    AutoDetectParser parser = new AutoDetectParser();
    parser.setParsers(new HashMap<MediaType, Parser>());

    Metadata metadata = new Metadata();
    metadata.add(TikaMetadataKeys.RESOURCE_NAME_KEY, nomefile);

    InputStream stream = new FileInputStream(fUp);
    parser.parse(stream, new DefaultHandler(), metadata, new ParseContext());
    stream.close();

    String mimeType = metadata.get(HttpHeaders.CONTENT_TYPE);
    
    return verificaFormato(mimeType, formatoFileDaNome(nomefile));
	}
	
	public boolean verificaFormato(String p_mymetype, String p_formato_default) {
		int posizione = 0;
		boolean trovato = false;
		
		while (posizione < fileMime.size() && !trovato) {
			if (fileMime.get(posizione).equalsIgnoreCase(p_mymetype) && formato.get(posizione).equalsIgnoreCase(p_formato_default)) {
				trovato = true;
			} else {
				posizione++;
			}
		}
		
		return trovato;
	}

	public String formatoFileDaNome(String nomeFile) {
		int pos = 0;
		int k = 0;
		if (nomeFile.length() > 0 && nomeFile.indexOf(".") > -1) {
  		int j = nomeFile.toLowerCase().indexOf(".p7m");
  		if (j > -1) {
  			while (k < j) {
  				pos = k;
  				k = nomeFile.toLowerCase().indexOf(".",k+1);
  			}
  			return nomeFile.substring(pos+1,j)+".p7m";
  		} else {
  			while (k > -1) {
  				pos = k;
  				k = nomeFile.toLowerCase().indexOf(".",k+1);
  			}
  			return nomeFile.substring(pos+1);
  		}
		} else {
			return "";
		}
	}
}
