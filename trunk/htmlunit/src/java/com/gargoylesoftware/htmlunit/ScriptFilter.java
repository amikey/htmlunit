/*
 *  Copyright (C) 2002, 2003 Gargoyle Software Inc. All rights reserved.
 *
 *  This file is part of HtmlUnit. For details on use and redistribution
 *  please refer to the license.html file included with these sources.
 */
package com.gargoylesoftware.htmlunit;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.StringReader;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.filters.DefaultFilter;

/**
 * A filter that will execute javascript and pass the result of any document.write calls back into
 * the input stream.
 *
 * @version  $Revision$
 * @author  <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 */
public final class ScriptFilter extends DefaultFilter {

    private final HTMLConfiguration configuration_;
    private final HtmlPage htmlPage_;

    private StringBuffer scriptBuffer_;
    private StringBuffer newContentBuffer_;
    private String systemId_;
    private int scriptCount_;
    private String scriptType_;

    /**
     *  Create an instance
     *
     * @param  config
     */
    public ScriptFilter( final HTMLConfiguration config, final HtmlPage htmlPage ) {
        assertNotNull("config", config);
        assertNotNull("htmlPage", htmlPage);
        configuration_ = config;
        htmlPage_ = htmlPage;
        htmlPage_.setScriptFilter(this);
    }


    /**
     *  Start document.
     */
    public void startDocument(
            final XMLLocator locator,
            final String encoding,
            final Augmentations augmentations )
        throws
            XNIException {

        scriptBuffer_ = null;
        systemId_ = locator != null ? locator.getLiteralSystemId() : null;
        scriptCount_ = 0;
        super.startDocument( locator, encoding, augmentations );
    }


    /**
     *  Start element.
     */
    public void startElement(
            final QName element,
            final XMLAttributes attrs,
            final Augmentations augmentations )
        throws
            XNIException {

        if( element.rawname.equalsIgnoreCase( "script" ) ) {
            scriptType_ = attrs.getValue( "type" );
            if( scriptType_ == null ) {
                scriptType_ = "";
            }
            if( scriptType_ == null || scriptType_.length() == 0 || scriptType_.equals("text/javascript") ) {
                scriptBuffer_ = new StringBuffer();
            }
        }

        super.startElement( element, attrs, augmentations );
    }


    /**
     *  Empty element.
     */
    public void emptyElement(
            final QName element,
            final XMLAttributes attrs,
            final Augmentations augmentations )
        throws
            XNIException {

        super.emptyElement( element, attrs, augmentations );
    }


    /**
     *  Characters.
     */
    public void characters(
            final XMLString text,
            final Augmentations augmentations )
        throws
            XNIException {

        if( scriptBuffer_ == null ) {
            super.characters( text, augmentations );
        }
        else {
            super.characters( text, augmentations );
            scriptBuffer_.append( text.ch, text.offset, text.length );
        }
    }


    /**
     *  End element.
     */
    public void endElement(
            final QName element,
            final Augmentations augmentations )
        throws
            XNIException {

        super.endElement( element, augmentations );
        if( scriptBuffer_ != null ) {
            if( element.rawname.equalsIgnoreCase( "script" ) == false ) {
                throw new IllegalStateException("Other elements were contained within the script tag");
            }

            try {
                final String script = scriptBuffer_.toString();
//                System.out.println();
//                System.out.println("=================== SCRIPT START ======================");
//                System.out.println(script);
//                System.out.println("=================== SCRIPT END   ======================");

                final String result = executeScript(script);
                if( result.length() != 0 ) {
                    final XMLInputSource xmlInputSource = new XMLInputSource(
                        null, systemId_, null, new StringReader( scriptBuffer_.toString() ), "UTF-8" );
                    configuration_.pushInputSource( xmlInputSource );
                }
            }
            finally {
                scriptBuffer_ = null;
            }
        }
//        else {
//            super.endElement( element, augmentations );
//        }
    }


    private void assertNotNull( final String description, final Object object ) {
        if( object == null ) {
            throw new NullPointerException(description);
        }
    }


    private synchronized String executeScript( final String script ) {
        newContentBuffer_ = null;
        htmlPage_.executeJavaScriptIfPossible(script, "Embedded script", false);
        if( newContentBuffer_ == null ) {
            return "";
        }
        else {
            final String result = newContentBuffer_.toString();
            newContentBuffer_ = null;
            return result;
        }
    }


    public synchronized void write( final String content ) {
        if( newContentBuffer_ == null ) {
            newContentBuffer_ = new StringBuffer();
        }
        newContentBuffer_.append(content);
    }
}

