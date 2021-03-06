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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.syncope.common.AbstractBaseBean;
import org.apache.syncope.common.types.SyncopeLoggerLevel;

@XmlRootElement(name = "logger")
@XmlType
public class LoggerTO extends AbstractBaseBean {

    private static final long serialVersionUID = -7794833835668648505L;

    private String name;

    private SyncopeLoggerLevel level;

    public SyncopeLoggerLevel getLevel() {
        return level;
    }

    public void setLevel(final SyncopeLoggerLevel level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
