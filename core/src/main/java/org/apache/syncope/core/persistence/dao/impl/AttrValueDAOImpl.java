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
package org.apache.syncope.core.persistence.dao.impl;

import java.util.List;
import javax.persistence.TypedQuery;
import org.apache.syncope.core.persistence.beans.AbstractAttrValue;
import org.apache.syncope.core.persistence.dao.AttrValueDAO;
import org.springframework.stereotype.Repository;

@Repository
public class AttrValueDAOImpl extends AbstractDAOImpl implements AttrValueDAO {

    @Override
    public <T extends AbstractAttrValue> T find(final Long id, final Class<T> reference) {
        return entityManager.find(reference, id);
    }

    @Override
    public <T extends AbstractAttrValue> List<T> findAll(final Class<T> reference) {
        TypedQuery<T> query = entityManager.createQuery("SELECT e FROM " + reference.getSimpleName() + " e", reference);
        return query.getResultList();
    }

    @Override
    public <T extends AbstractAttrValue> T save(final T attributeValue) {
        return entityManager.merge(attributeValue);
    }

    @Override
    public <T extends AbstractAttrValue> void delete(final Long id, final Class<T> reference) {
        T attributeValue = find(id, reference);
        if (attributeValue == null) {
            return;
        }

        delete(attributeValue);
    }

    @Override
    public <T extends AbstractAttrValue> void delete(final T attributeValue) {
        if (attributeValue.getAttribute() != null) {
            attributeValue.getAttribute().removeValue(attributeValue);
        }

        entityManager.remove(attributeValue);
    }
}
