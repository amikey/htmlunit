/*
 *  Copyright (C) 2002 Gargoyle Software Inc. All rights reserved.
 *
 *  This file is part of HtmlUnit. For details on use and redistribution
 *  please refer to the license.html file included with these sources.
 */
package com.gargoylesoftware.htmlunit;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 *  An object that handles the actual communication portion of page
 *  retrieval/submission <p />
 *
 *  THIS CLASS IS FOR INTERNAL USE ONLY
 *
 * @version  $Revision$
 * @author  <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 */
public abstract class WebConnection {
    private final String proxyHost_;
    private final int proxyPort_;
    private final WebClient webClient_;


    /**
     *  Create an instance that will not use a proxy server
     *
     * @param  webClient The WebClient that is using this connection
     */
    public WebConnection( final WebClient webClient ) {
        webClient_ = webClient;
        proxyHost_ = null;
        proxyPort_ = 0;
    }


    /**
     *  Create an instance that will use the specified proxy server
     *
     * @param  proxyHost The server that will act as proxy
     * @param  proxyPort The port to use on the proxy server
     * @param  webClient The web client that is using this connection
     */
    public WebConnection( final WebClient webClient, final String proxyHost, final int proxyPort ) {
        webClient_ = webClient;
        proxyHost_ = proxyHost;
        proxyPort_ = proxyPort;
    }


    /**
     *  Submit a request and retrieve a response
     *
     * @param  parameters Any parameters
     * @param  url The url of the server
     * @param  submitMethod The submit method. Ie SubmitMethod.GET
     * @param  requestHeaders Any headers that need to be put into the request.
     * @return  See above
     * @exception  IOException If an IO error occurs
     */
    public abstract WebResponse getResponse(
            final URL url,
            final SubmitMethod submitMethod,
            final List parameters,
            final Map requestHeaders )
        throws
            IOException;


    /**
     * Return the web client
     * @return The web client.
     */
    public final WebClient getWebClient() {
        return webClient_;
    }


    /**
     * Return the proxy host
     * @return The proxy host.
     */
    public final String getProxyHost() {
        return proxyHost_;
    }


    /**
     * Return the proxy port.
     * @return The proxy port.
     */
    public final int getProxyPort() {
        return proxyPort_;
    }
}

