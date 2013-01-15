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

import javax.servlet.ServletContext;

import org.apache.syncope.core.persistence.dao.impl.ContentLoader;
import org.apache.syncope.core.workflow.ActivitiDetector;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

/**
 * Take care of all initializations needed by Syncope to run up and safe.
 */
@Component
public class TestDbInitializer implements ServletContextAware, InitializingBean {
    @Autowired
    private ContentLoader contentLoader;

    @Override
    public void setServletContext(final ServletContext servletContext) {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        contentLoader.load(ActivitiDetector.isActivitiEnabledForUsers());
    }
}