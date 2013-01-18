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
package org.apache.syncope.console.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.syncope.client.to.EntitlementTO;
import org.apache.syncope.client.util.CollectionWrapper;
import org.apache.syncope.services.EntitlementService;
import org.springframework.stereotype.Component;

/**
 * Console client for invoking Rest Resources services.
 */
@Component
public class AuthRestClient extends BaseRestClient {

    private static final long serialVersionUID = 2999780105004742914L;

    /**
     * Get all Entitlements.
     *
     * @return List<String>
     */
    public List<String> getAllEntitlements() {
        List<EntitlementTO> entitlemens = new ArrayList<EntitlementTO>(getService(EntitlementService.class)
                .getAllEntitlements());
        return CollectionWrapper.unwrap(entitlemens);
    }

    /**
     * Get owned Entitlements.
     *
     * @return List<String>
     */
    public List<String> getOwnedEntitlements() {
        List<EntitlementTO> entitlemens = new ArrayList<EntitlementTO>(getService(EntitlementService.class)
                .getMyEntitlements());
        return CollectionWrapper.unwrap(entitlemens);
    }
}