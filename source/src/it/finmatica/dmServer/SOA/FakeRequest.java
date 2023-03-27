package it.finmatica.dmServer.SOA;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Classe di servizio che consente di simulare una REQUEST HTTP, per lo meno con
 * un sottoinsieme di metodi e informazioni che potrebbero essere utilizzate dal
 * nostro sistema.
 * 
 * @author agentilini
 * 
 */
public class FakeRequest implements HttpServletRequest {

	String fakeContextPath, fakeServletPath, fakeRemoteUser, fakeRemoteAddr,
			fakeRemoteUrl, fakeServerName, fakeServerPort, fakeServerProtocol;

	protected Hashtable<String, String> parametri;
	protected Hashtable<String, Object> attributi;

	public FakeRequest() {
		super();
		parametri = new Hashtable<String, String>();
		attributi = new Hashtable<String, Object>();
	}

	/*
	 * METODI EREDITATI E RIDEFINITI NELL'OGETO FAKE
	 */
	public String getContextPath() {
		return fakeContextPath;
	}

	public String getRemoteUser() {
		return fakeRemoteUser;
	}

	public String getServletPath() {
		return fakeServletPath;
	}

	public Object getAttribute(String nomeAttr) {
		return attributi.get(nomeAttr);
	}

	public Enumeration getAttributeNames() {
		return attributi.keys();
	}

	public Map getParameterMap() {
		return parametri;
	}

	public Enumeration getParameterNames() {
		return parametri.keys();
	}

	public String[] getParameterValues(String nomePar) {
		String[] retVal;

		retVal = new String[1];
		retVal[0] = parametri.get(nomePar);
		return retVal;
	}

	public String getParameter(String nomePar) {
		return parametri.get(nomePar);
	}

	public String getRemoteHost() {
		return fakeRemoteAddr;
	}

	public String getRemoteAddr() {
		return fakeRemoteAddr;
	}

	public String getFakeContextPath() {

		return fakeContextPath;
	}

	public void setFakeContextPath(String fakeContextPath) {
		this.fakeContextPath = fakeContextPath;
	}

	public String getFakeServletPath() {
		return fakeServletPath;
	}

	public void setFakeServletPath(String fakeServletPath) {
		this.fakeServletPath = fakeServletPath;
	}

	public String getFakeRemoteUser() {
		return fakeRemoteUser;
	}

	public void setFakeRemoteUser(String fakeRemoteUser) {
		this.fakeRemoteUser = fakeRemoteUser;
	}

	public void removeAttribute(String key) {
		attributi.remove(key);
	}

	public void setAttribute(String key, Object value) {
		attributi.put(key, value);
	}
	
	public void setParameter(String key, Object value) {
		parametri.put(key, ""+value);
	}


	public String getServerName() {
		return fakeServerName;
	}

	public int getServerPort() {
		return Integer.parseInt(fakeServerPort);
	}

	public String getServerProtocol() {
		return fakeServerProtocol;
	}

	public void setFakeRemoteAddr(String fakeRemoteAddr) {
		this.fakeRemoteAddr = fakeRemoteAddr;
	}

	public void setFakeRemoteUrl(String fakeRemoteUrl) {
		this.fakeRemoteUrl = fakeRemoteUrl;
	}

	public StringBuffer getRequestURL() {
		return new StringBuffer(fakeRemoteUrl);
	}

	public void setFakeServerName(String fakeServerName) {
		this.fakeServerName = fakeServerName;
	}

	public void setFakeServerPort(String fakeServerPort) {
		this.fakeServerPort = fakeServerPort;
	}

	public void setFakeServerProtocol(String fakeServerProtocl) {
		this.fakeServerProtocol = fakeServerProtocl;
	}

	public void addParameter(String key, String val) {
		parametri.put(key, val);
	}

	/*
	 * METODI EREDITATI MA NON ANCORA REIMPLEMENTATI
	 */
	public String getAuthType() {
		return null;
	}

	public Cookie[] getCookies() {
		return null;
	}

	public long getDateHeader(String arg0) {
		return 0;
	}

	public String getHeader(String arg0) {
		return null;
	}

	public Enumeration getHeaderNames() {
		return null;
	}

	public Enumeration getHeaders(String arg0) {
		return null;
	}

	public int getIntHeader(String arg0) {
		return 0;
	}

	public String getMethod() {
		return null;
	}

	public String getPathInfo() {
		return null;
	}

	public String getPathTranslated() {
		return null;
	}

	public String getQueryString() {
		return null;
	}

	public String getRequestURI() {
		return null;
	}

	public String getRequestedSessionId() {
		return null;
	}

	public HttpSession getSession() {
		return null;
	}

	public HttpSession getSession(boolean arg0) {
		return null;
	}

	public Principal getUserPrincipal() {
		return null;
	}

	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	public boolean isRequestedSessionIdValid() {
		return false;
	}

	public boolean isUserInRole(String arg0) {
		return false;
	}

	public String getCharacterEncoding() {
		return null;
	}

	public int getContentLength() {
		return 0;
	}

	public String getContentType() {
		return null;
	}

	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	public String getLocalAddr() {
		return null;
	}

	public String getLocalName() {
		return null;
	}

	public int getLocalPort() {
		return 0;
	}

	public Locale getLocale() {
		return null;
	}

	public Enumeration getLocales() {
		return null;
	}

	public String getProtocol() {
		return null;
	}

	public BufferedReader getReader() throws IOException {
		return null;
	}

	public String getRealPath(String arg0) {
		return null;
	}

	public int getRemotePort() {
		return 0;
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	public String getScheme() {
		return null;
	}

	public boolean isSecure() {
		return false;
	}

	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {

	}

}
