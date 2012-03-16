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
package org.syncope.core.rest.controller;

import java.util.ArrayList;
import java.util.List;
import javassist.NotFoundException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.syncope.client.to.DerivedSchemaTO;
import org.syncope.client.validation.SyncopeClientCompositeErrorException;
import org.syncope.core.audit.AuditManager;
import org.syncope.core.persistence.beans.AbstractDerSchema;
import org.syncope.core.persistence.dao.DerSchemaDAO;
import org.syncope.core.rest.data.DerivedSchemaDataBinder;
import org.syncope.types.AuditElements.Category;
import org.syncope.types.AuditElements.Result;
import org.syncope.types.AuditElements.SchemaSubCategory;

@Controller
@RequestMapping("/derivedSchema")
public class DerivedSchemaController extends AbstractController {

    @Autowired
    private AuditManager auditManager;

    @Autowired
    private DerSchemaDAO derivedSchemaDAO;

    @Autowired
    private DerivedSchemaDataBinder derivedSchemaDataBinder;

    @PreAuthorize("hasRole('SCHEMA_CREATE')")
    @RequestMapping(method = RequestMethod.POST, value = "/{kind}/create")
    public DerivedSchemaTO create(final HttpServletResponse response,
            @RequestBody final DerivedSchemaTO derivedSchemaTO, @PathVariable("kind") final String kind)
            throws SyncopeClientCompositeErrorException {

        AbstractDerSchema derivedSchema = derivedSchemaDAO.save(
                derivedSchemaDataBinder.create(derivedSchemaTO, getAttributableUtil(kind).newDerivedSchema()));

        auditManager.audit(Category.schema, SchemaSubCategory.createDerived, Result.success,
                "Successfully created derived schema: " + kind + "/" + derivedSchema.getName());

        response.setStatus(HttpServletResponse.SC_CREATED);
        return derivedSchemaDataBinder.getDerivedSchemaTO(derivedSchema);
    }

    @PreAuthorize("hasRole('SCHEMA_DELETE')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{kind}/delete/{schema}")
    public void delete(@PathVariable("kind") final String kind,
            @PathVariable("schema") final String derivedSchemaName) throws NotFoundException {

        Class reference = getAttributableUtil(kind).derivedSchemaClass();
        AbstractDerSchema derivedSchema = derivedSchemaDAO.find(derivedSchemaName, reference);
        if (derivedSchema == null) {
            throw new NotFoundException("Derived schema '" + derivedSchemaName + "'");
        }

        derivedSchemaDAO.delete(derivedSchemaName, getAttributableUtil(kind));

        auditManager.audit(Category.schema, SchemaSubCategory.deleteDerived, Result.success,
                "Successfully deleted derived schema: " + kind + "/" + derivedSchema.getName());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{kind}/list")
    public List<DerivedSchemaTO> list(@PathVariable("kind") final String kind) {
        Class reference = getAttributableUtil(kind).derivedSchemaClass();
        List<AbstractDerSchema> derivedAttributeSchemas = derivedSchemaDAO.findAll(reference);

        List<DerivedSchemaTO> derivedSchemaTOs = new ArrayList<DerivedSchemaTO>(derivedAttributeSchemas.size());
        for (AbstractDerSchema derivedSchema : derivedAttributeSchemas) {
            derivedSchemaTOs.add(derivedSchemaDataBinder.getDerivedSchemaTO(derivedSchema));
        }

        auditManager.audit(Category.schema, SchemaSubCategory.listDerived, Result.success,
                "Successfully listed all derived schemas: " + kind + "/" + derivedSchemaTOs.size());

        return derivedSchemaTOs;
    }

    @PreAuthorize("hasRole('SCHEMA_READ')")
    @RequestMapping(method = RequestMethod.GET, value = "/{kind}/read/{derivedSchema}")
    public DerivedSchemaTO read(@PathVariable("kind") final String kind,
            @PathVariable("derivedSchema") final String derivedSchemaName) throws NotFoundException {

        Class reference = getAttributableUtil(kind).derivedSchemaClass();
        AbstractDerSchema derivedSchema = derivedSchemaDAO.find(derivedSchemaName, reference);
        if (derivedSchema == null) {
            throw new NotFoundException("Derived schema '" + derivedSchemaName + "'");
        }

        auditManager.audit(Category.schema, SchemaSubCategory.readDerived, Result.success,
                "Successfully read derived schema: " + kind + "/" + derivedSchema.getName());

        return derivedSchemaDataBinder.getDerivedSchemaTO(derivedSchema);
    }

    @PreAuthorize("hasRole('SCHEMA_UPDATE')")
    @RequestMapping(method = RequestMethod.POST, value = "/{kind}/update")
    public DerivedSchemaTO update(@RequestBody final DerivedSchemaTO derivedSchemaTO,
            @PathVariable("kind") final String kind) throws NotFoundException {

        Class reference = getAttributableUtil(kind).derivedSchemaClass();
        AbstractDerSchema derivedSchema = derivedSchemaDAO.find(derivedSchemaTO.getName(), reference);
        if (derivedSchema == null) {
            throw new NotFoundException("Derived schema '" + derivedSchemaTO.getName() + "'");
        }

        derivedSchema = derivedSchemaDataBinder.update(derivedSchemaTO, derivedSchema);
        derivedSchema = derivedSchemaDAO.save(derivedSchema);

        auditManager.audit(Category.schema, SchemaSubCategory.updateDerived, Result.success,
                "Successfully updated derived schema: " + kind + "/" + derivedSchema.getName());

        return derivedSchemaDataBinder.getDerivedSchemaTO(derivedSchema);
    }
}
