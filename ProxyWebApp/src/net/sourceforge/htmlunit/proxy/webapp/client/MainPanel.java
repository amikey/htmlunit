/*
 * Copyright (c) 2010 HtmlUnit team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.htmlunit.proxy.webapp.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * The main panel.
 *
 * @author Ahmed Ashour
 * @version $Revision$
 */
public class MainPanel extends Composite {

    interface Binder extends UiBinder<Widget, MainPanel> { }
    private static final Binder binder_ = GWT.create(Binder.class);

    @UiField Button clearButton_;
    @UiField TextArea logTextArea_;

    /**
     * Constructor.
     */
    public MainPanel() {
        initWidget(binder_.createAndBindUi(this));
        clearButton_.addClickHandler(new ClickHandler() {

            public void onClick(final ClickEvent event) {
                logTextArea_.setText("");
            }
        });
    }
}
