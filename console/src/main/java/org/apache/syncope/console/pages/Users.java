/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.console.pages;

import org.apache.syncope.common.search.NodeCond;
import org.apache.syncope.common.to.UserTO;
import org.apache.syncope.console.commons.Constants;
import org.apache.syncope.console.pages.panels.AbstractSearchResultPanel;
import org.apache.syncope.console.pages.panels.AbstractSearchResultPanel.EventDataWrapper;
import org.apache.syncope.console.pages.panels.UserSearchPanel;
import org.apache.syncope.console.pages.panels.UserSearchResultPanel;
import org.apache.syncope.console.rest.UserRestClient;
import org.apache.syncope.console.wicket.ajax.markup.html.ClearIndicatingAjaxButton;
import org.apache.syncope.console.wicket.ajax.markup.html.ClearIndicatingAjaxLink;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class Users extends BasePage {

    private static final long serialVersionUID = 134681165644474568L;

    private final static int EDIT_MODAL_WIN_HEIGHT = 550;

    private final static int EDIT_MODAL_WIN_WIDTH = 800;

    @SpringBean
    private UserRestClient restClient;

    public Users(final PageParameters parameters) {
        super(parameters);

        // Modal window for editing user attributes
        final ModalWindow editModalWin = new ModalWindow("editModal");
        editModalWin.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        editModalWin.setInitialHeight(EDIT_MODAL_WIN_HEIGHT);
        editModalWin.setInitialWidth(EDIT_MODAL_WIN_WIDTH);
        editModalWin.setCookieName("edit-modal");
        add(editModalWin);

        final AbstractSearchResultPanel searchResult =
                new UserSearchResultPanel("searchResult", true, null, getPageReference(), restClient);
        add(searchResult);

        final AbstractSearchResultPanel listResult =
                new UserSearchResultPanel("listResult", false, null, getPageReference(), restClient);
        add(listResult);


        // create new user
        final AjaxLink<Void> createLink = new ClearIndicatingAjaxLink<Void>("createLink", getPageReference()) {

            private static final long serialVersionUID = -7978723352517770644L;

            @Override
            protected void onClickInternal(final AjaxRequestTarget target) {
                editModalWin.setPageCreator(new ModalWindow.PageCreator() {

                    private static final long serialVersionUID = -7834632442532690940L;

                    @Override
                    public Page createPage() {
                        return new EditUserModalPage(Users.this.getPageReference(), editModalWin, new UserTO());
                    }
                });

                editModalWin.show(target);
            }
        };
        MetaDataRoleAuthorizationStrategy.authorize(
                createLink, ENABLE, xmlRolesReader.getAllAllowedRoles("Users", "create"));
        add(createLink);

        setWindowClosedReloadCallback(editModalWin);

        final Form searchForm = new Form("searchForm");
        add(searchForm);

        final UserSearchPanel searchPanel = new UserSearchPanel.Builder("searchPanel").build();
        searchForm.add(searchPanel);

        final ClearIndicatingAjaxButton searchButton =
                new ClearIndicatingAjaxButton("search", new ResourceModel("search"), getPageReference()) {

            private static final long serialVersionUID = -958724007591692537L;

            @Override
            protected void onSubmitInternal(final AjaxRequestTarget target, final Form<?> form) {
                final NodeCond searchCond = searchPanel.buildSearchCond();
                LOG.debug("Node condition " + searchCond);

                doSearch(target, searchCond, searchResult);

                Session.get().getFeedbackMessages().clear();
                target.add(searchPanel.getSearchFeedback());
            }

            @Override
            protected void onError(final AjaxRequestTarget target, final Form<?> form) {

                target.add(searchPanel.getSearchFeedback());
            }
        };

        searchForm.add(searchButton);
        searchForm.setDefaultButton(searchButton);
    }

    private void doSearch(final AjaxRequestTarget target, final NodeCond searchCond,
            final AbstractSearchResultPanel resultsetPanel) {

        if (searchCond == null || !searchCond.isValid()) {
            error(getString(Constants.SEARCH_ERROR));
            return;
        }

        resultsetPanel.search(searchCond, target);
    }

    private void setWindowClosedReloadCallback(final ModalWindow window) {
        window.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {

            private static final long serialVersionUID = 8804221891699487139L;

            @Override
            public void onClose(final AjaxRequestTarget target) {
                final EventDataWrapper data = new EventDataWrapper();
                data.setTarget(target);
                data.setCreate(true);

                send(getPage(), Broadcast.BREADTH, data);

                if (isModalResult()) {
                    // reset modal result
                    setModalResult(false);
                    // set operation succeeded
                    getSession().info(getString(Constants.OPERATION_SUCCEEDED));
                    // refresh feedback panel
                    target.add(feedbackPanel);
                }
            }
        });
    }
}
