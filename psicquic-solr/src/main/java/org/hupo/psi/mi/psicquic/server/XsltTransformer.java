package org.hupo.psi.mi.psq.server;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # XsltTransformer: transform the incoming xml file into solr documents
 #
 #=========================================================================== */

import java.util.*;
import java.net.*;
import java.io.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.bind.util.JAXBResult;

public class XsltTransformer implements PsqTransformer{
    
    Transformer psqtr = null;
    
    public XsltTransformer( String xslt ){
		    
	try {
	    DocumentBuilderFactory
		dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware( true );
	    
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    
	    File xslFile = new File( xslt );
	    InputStream xslIStr = null;
	    
	    if( !xslFile.canRead() ){
		xslIStr = this.getClass().getClassLoader()
		    .getResourceAsStream( conf );
	    } else {
		xslIStr = new FileInputStream( xslt );
	    }
	    
	    Document xslDoc = db.parse( xslIStr );
	    
	    DOMSource xslDomSource = new DOMSource( xslDoc );
	    TransformerFactory
		tFactory = TransformerFactory.newInstance();
	    
	    psqtr = tFactory.newTransformer( xslDomSource );
            
	} catch( Exception ex ) {
	    ex.printStackTrace();
	}
    }

    public void start( String fileName ){};

    boolean hasNext(){
	if( domNode == null) return false;
	
	return false;
    }

    SolrInputDocument next(){
	return null;
    }

    Node domNode = null;
    
    public void start( String fileName, InputStream is ){
	
	try {

            StreamSource ssNative = new StreamSource( is );
            DOMResult domResult = new DOMResult();
	    
            //transform into dom
            //------------------
            
            psqtr.clearParameters();
            psqtr.setParameter( "file", fileName );
            psqtr.transform( ssNative, domResult );
            
	    domNode=domResult.getNode();
	    
	} catch( Exception ex ) {
	    ex.printStackTrace();
	}
    }
        return null;
    }
}