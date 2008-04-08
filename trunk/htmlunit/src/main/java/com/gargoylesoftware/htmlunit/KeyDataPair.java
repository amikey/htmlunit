/*
 * Copyright (c) 2002-2008 Gargoyle Software Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment:
 *
 *       "This product includes software developed by Gargoyle Software Inc.
 *        (http://www.GargoyleSoftware.com/)."
 *
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 4. The name "Gargoyle Software" must not be used to endorse or promote
 *    products derived from this software without prior written permission.
 *    For written permission, please contact info@GargoyleSoftware.com.
 * 5. Products derived from this software may not be called "HtmlUnit", nor may
 *    "HtmlUnit" appear in their name, without prior written permission of
 *    Gargoyle Software Inc.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GARGOYLE
 * SOFTWARE INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gargoylesoftware.htmlunit;

import java.io.File;

import org.apache.commons.httpclient.NameValuePair;

/**
 * A holder for a key/value pair that represents a file to upload.
 *
 * @version $Revision$
 * @author Brad Clarke
 * @author David D. Kilzer
 * @author Mike Bowler
 */
public class KeyDataPair extends NameValuePair {

    private static final long serialVersionUID = -1129314696176851675L;
    
    private final File fileObject_;
    private final String contentType_;
    private final String charset_;

    /**
     * Create an instance.
     *
     * @param key the key
     * @param file the file
     * @param contentType the content type
     * @param charset the charset encoding
     */
    public KeyDataPair(final String key, final File file, final String contentType,
            final String charset) {

        super(key, file.getName());

        if (file.exists()) {
            fileObject_ = file;
        }
        else {
            fileObject_ = null;
        }
        
        contentType_ = contentType;
        charset_ = charset;
    }

    /**
     * @return the {@link File} object if the file exists, else <tt>null</tt>
     */
    public File getFile() {
        return fileObject_;
    }

    /**
     * Gets the charset encoding for this file upload.
     * @return the charset
     */
    public String getCharset() {
        return charset_;
    }

    /**
     * Gets the content type for this file upload.
     * @return the content type
     */
    public String getContentType() {
        return contentType_;
    }
}
