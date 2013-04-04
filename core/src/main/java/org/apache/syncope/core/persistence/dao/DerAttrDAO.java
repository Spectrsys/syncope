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
package org.apache.syncope.core.persistence.dao;

import java.util.List;

import org.apache.syncope.core.persistence.beans.AbstractDerAttr;
import org.apache.syncope.core.persistence.validation.entity.InvalidEntityException;

public interface DerAttrDAO extends DAO {

    <T extends AbstractDerAttr> T find(Long id, Class<T> reference);

    <T extends AbstractDerAttr> List<T> findAll(Class<T> reference);

    <T extends AbstractDerAttr> T save(T derivedAttribute) throws InvalidEntityException;

    <T extends AbstractDerAttr> void delete(Long id, Class<T> reference);

    <T extends AbstractDerAttr> void delete(T derivedAttribute);
}
