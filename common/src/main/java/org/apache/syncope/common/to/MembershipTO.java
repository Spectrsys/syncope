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
package org.apache.syncope.common.to;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "membership")
@XmlType
public class MembershipTO extends AbstractAttributableTO {

    private static final long serialVersionUID = 5992828670273935861L;

    private long roleId;

    private String roleName;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public boolean addResource(String resource) {
        return false;
    }

    @Override
    public boolean removeResource(String resource) {
        return false;
    }

    @Override
    @XmlElementWrapper(name = "resources")
    @XmlElement(name = "resource")
    public Set<String> getResources() {
        return Collections.<String>emptySet();
    }

    @Override
    public void setResources(Set<String> resources) {
    }

    @Override
    public boolean addPropagationTO(PropagationStatusTO status) {
        return false;
    }

    @Override
    public boolean removePropagationTO(String resource) {
        return false;
    }

    @Override
    public List<PropagationStatusTO> getPropagationStatusTOs() {
        return Collections.<PropagationStatusTO>emptyList();
    }

    @Override
    public void setPropagationStatusTOs(List<PropagationStatusTO> propagationTOs) {
    }
}
