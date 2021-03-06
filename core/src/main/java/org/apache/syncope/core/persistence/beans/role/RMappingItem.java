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
package org.apache.syncope.core.persistence.beans.role;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import org.apache.syncope.core.persistence.beans.AbstractMapping;
import org.apache.syncope.core.persistence.beans.AbstractMappingItem;

@Entity
public class RMappingItem extends AbstractMappingItem {

    private static final long serialVersionUID = 2936446317887310833L;

    @Id
    private Long id;

    @ManyToOne
    private RMapping mapping;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbstractMapping> T getMapping() {
        return (T) mapping;
    }

    @Override
    public <T extends AbstractMapping> void setMapping(final T mapping) {
        if (mapping != null && !(mapping instanceof RMapping)) {
            throw new ClassCastException("accountIdItem is expected to be typed RMapping: "
                    + mapping.getClass().getName());
        }
        this.mapping = (RMapping) mapping;
    }
}
