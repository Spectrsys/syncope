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
package org.apache.syncope.console.commons;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.syncope.common.search.NodeCond;
import org.apache.syncope.common.services.InvalidSearchConditionException;
import org.apache.syncope.common.to.AbstractAttributableTO;
import org.apache.syncope.console.rest.AbstractAttributableRestClient;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributableDataProvider extends SortableDataProvider<AbstractAttributableTO, String> {
    private static final Logger LOG = LoggerFactory.getLogger(AttributableDataProvider.class);

    private static final long serialVersionUID = 6267494272884913376L;

    private final SortableAttributableProviderComparator comparator;

    private NodeCond filter = null;

    private final int paginatorRows;

    private final boolean filtered ;

    private final AbstractAttributableRestClient restClient;

    public AttributableDataProvider(final AbstractAttributableRestClient restClient,
            final int paginatorRows, final boolean filtered) {

        super();

        this.restClient = restClient;
        this.filtered = filtered;
        this.paginatorRows = paginatorRows;

        // default sorting
        setSort("id", SortOrder.ASCENDING);

        this.comparator = new SortableAttributableProviderComparator(this);
    }

    public void setSearchCond(final NodeCond searchCond) {
        this.filter = searchCond;
    }

    @Override
    public Iterator<? extends AbstractAttributableTO> iterator(final long first, final long count) {
        final List<? extends AbstractAttributableTO> result;

        if (filtered) {
            try {
                result = filter == null
                        ? Collections.<AbstractAttributableTO>emptyList()
                        : restClient.search(filter, ((int) first / paginatorRows) + 1, paginatorRows);
            } catch (InvalidSearchConditionException e) {
                LOG.error(e.getMessage(), e);
                return Collections.<AbstractAttributableTO>emptyList().iterator();
            }
        } else {
            result = restClient.list(((int) first / paginatorRows) + 1, paginatorRows);
        }

        Collections.sort(result, comparator);
        return result.iterator();
    }

    @Override
    public long size() {
        if (filtered) {
            try {
                return filter == null
                        ? 0
                        : restClient.searchCount(filter);
            } catch (InvalidSearchConditionException e) {
                LOG.error(e.getMessage(), e);
                return 0;
            }
        } else {
            return restClient.count();
        }
    }

    @Override
    public IModel<AbstractAttributableTO> model(final AbstractAttributableTO object) {
        return new CompoundPropertyModel<AbstractAttributableTO>(object);
    }
}
