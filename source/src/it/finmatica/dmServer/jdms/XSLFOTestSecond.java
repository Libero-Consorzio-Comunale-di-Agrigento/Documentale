package it.finmatica.dmServer.jdms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xml.sax.SAXException;

import be.re.css.CSSToXSLFOException;


public class XSLFOTestSecond {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws CSSToXSLFOException 
	 * @throws MalformedURLException 
	 * @throws TransformerConfigurationException 
	 */
	public static void main(String[] args) throws TransformerConfigurationException, MalformedURLException, CSSToXSLFOException, SAXException, ParserConfigurationException, TransformerFactoryConfigurationError, IOException {
			FOEngine engine = new FOEngine();
			//create_input_stream()
			String InputFilePath = "C:"+ File.separator + "download"+ File.separator +"XSLFO"+ File.separator +"test"+ File.separator +"Input2.html";
			File InputFile = new File(InputFilePath);
			String OutputFoFilePath = "C:"+ File.separator + "download"+ File.separator +"XSLFO"+ File.separator +"test"+ File.separator +"Output2.fo";
			File OutputFoFile = new File(OutputFoFilePath);
			String OutputPdfFilePath = "C:"+ File.separator + "download"+ File.separator +"XSLFO"+ File.separator +"test"+ File.separator +"Output2.pdf";
			File OutputPdfFile = new File(OutputPdfFilePath);
			FileInputStream fis = new FileInputStream(InputFile);
			FileOutputStream fosFO = new FileOutputStream(OutputFoFile);
			ByteArrayOutputStream baosFO = new ByteArrayOutputStream(); 
			FileOutputStream fosPDF = new FileOutputStream(OutputPdfFile);
			engine.convertXHTML2FO(fis,InputFile.toURL(),baosFO);
			ByteArrayInputStream baisFO = new ByteArrayInputStream(baosFO.toByteArray());
			FileInputStream fisFO = new FileInputStream(OutputFoFile);
			engine.convertFO2PDF(baisFO,fosPDF);
	}

	
}
