package it.finmatica.dmServer.jdms;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public final class XSSEncodeFilter implements Filter {
	

	public void doFilter(ServletRequest request, ServletResponse response,FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		
		String user = httpRequest.getRemoteUser();
		String parametro="";
		
		if(httpRequest.getSession().getAttribute("CODIFICA_XSS")==null){
			try {
				XSS_Encoder xss = new XSS_Encoder(new CCS_Common("jdbc/gdm",user));
				parametro = xss.getCodificaXSS();
				
				httpRequest.getSession().setAttribute("CODIFICA_XSS", parametro);
			}
			catch (Exception exp) {			
				 exp.printStackTrace();
			}
		}
		else
			parametro = httpRequest.getSession().getAttribute("CODIFICA_XSS").toString();
		
		if(parametro.equals("S")){
			HttpServletRequest wrappedRequest = new XSSRequestWrapper((HttpServletRequest) request);
			chain.doFilter(wrappedRequest, response);
		}
		else
			chain.doFilter(request, response);
	}

	public void init(FilterConfig f) throws ServletException {		
		
	}
	
	public void destroy() {
		
	}
	
}