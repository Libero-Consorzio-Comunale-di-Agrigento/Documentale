package it.finmatica.dmServer.jdms;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.owasp.encoder.Encode;

public class XSSRequestWrapper extends HttpServletRequestWrapper {
	
	   private Map<String, String[]> allParameters = null;
	   private Map<String, String[]> modifiableParameters = null;

	    public XSSRequestWrapper(final HttpServletRequest servletRequest) {
	    	   super(servletRequest);
	    }

	    @Override
	    public String getParameter(final String name)
	    {
	        String[] strings = getParameterMap().get(name);
	        if (strings != null)
	        {
	            return strings[0];
	        }
	        return super.getParameter(name);
	    }
	    
		@Override
	    public String getHeader(final String name) {
	        String[] strings = getParameterMap().get(name);
	        if (strings != null)
	        {
	            return strings[0];
	        }	        
			return super.getHeader(name);
	    }

	    @Override
	    public Enumeration<String> getParameterNames()
	    {
	        return Collections.enumeration(getParameterMap().keySet());
	    }

	    @Override
	    public String[] getParameterValues(final String name)
	    {
	        return getParameterMap().get(name);
	    }
	    
	    @Override
	    public Map<String, String[]> getParameterMap()
	    {
	        if (allParameters == null)
	        {
	            allParameters = new TreeMap<String, String[]>();
	            modifiableParameters = new TreeMap<String, String[]>(); 
	            allParameters.putAll(super.getParameterMap());
	            
	            Iterator<String> it  = allParameters.keySet().iterator();
	            	
	            while(it.hasNext()) {
            	   String key=it.next();
            	   if (key != null) {
        				String value = ((String[]) allParameters.get(key))[0];
        				String valueEncoded = encodeXSS(value);	  
        				modifiableParameters.put(key, new String[]{valueEncoded});
        			}	
	            } 
	            		            
	        }

	        //Return an unmodifiable collection because we need to uphold the interface contract.
	        return Collections.unmodifiableMap(modifiableParameters);
	    }
		    
	    private String encodeXSS(String value) {
	    	if (value != null) {
	        	value = Encode.forHtmlAttribute(value);
	        }
	        return value;
	    }
}
