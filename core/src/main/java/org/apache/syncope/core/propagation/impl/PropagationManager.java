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
package org.apache.syncope.core.propagation.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.syncope.common.mod.AttributeMod;
import org.apache.syncope.common.to.AttributeTO;
import org.apache.syncope.common.types.AttributableType;
import org.apache.syncope.common.types.ResourceOperation;
import org.apache.syncope.core.connid.ConnObjectUtil;
import org.apache.syncope.core.persistence.beans.AbstractAttributable;
import org.apache.syncope.core.persistence.beans.AbstractMappingItem;
import org.apache.syncope.core.persistence.beans.ExternalResource;
import org.apache.syncope.core.persistence.beans.PropagationTask;
import org.apache.syncope.core.persistence.beans.role.SyncopeRole;
import org.apache.syncope.core.persistence.beans.user.SyncopeUser;
import org.apache.syncope.core.persistence.dao.ResourceDAO;
import org.apache.syncope.core.propagation.PropagationByResource;
import org.apache.syncope.core.rest.controller.UnauthorizedRoleException;
import org.apache.syncope.core.rest.data.AbstractAttributableDataBinder;
import org.apache.syncope.core.rest.data.RoleDataBinder;
import org.apache.syncope.core.rest.data.UserDataBinder;
import org.apache.syncope.core.util.AttributableUtil;
import org.apache.syncope.core.util.JexlUtil;
import org.apache.syncope.core.util.MappingUtil;
import org.apache.syncope.core.util.NotFoundException;
import org.apache.syncope.core.workflow.WorkflowResult;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manage the data propagation to external resources.
 */
@Transactional(rollbackFor = {Throwable.class})
public class PropagationManager {

    /**
     * Logger.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(PropagationManager.class);

    /**
     * User DataBinder.
     */
    @Autowired
    private UserDataBinder userDataBinder;

    /**
     * User DataBinder.
     */
    @Autowired
    private RoleDataBinder roleDataBinder;

    /**
     * Resource DAO.
     */
    @Autowired
    private ResourceDAO resourceDAO;

    /**
     * ConnObjectUtil.
     */
    @Autowired
    private ConnObjectUtil connObjectUtil;

    /**
     * JEXL engine for evaluating connector's account link.
     */
    @Autowired
    private JexlUtil jexlUtil;

    /**
     * Create the user on every associated resource.
     *
     * @param wfResult user to be propagated (and info associated), as per result from workflow
     * @param password to be set
     * @param vAttrs virtual attributes to be set
     * @return list of propagation tasks
     * @throws NotFoundException if user is not found
     * @throws UnauthorizedRoleException if caller doesn't own enough entitlements to administer the given user
     */
    public List<PropagationTask> getUserCreateTaskIds(final WorkflowResult<Map.Entry<Long, Boolean>> wfResult,
            final String password, final List<AttributeTO> vAttrs)
            throws NotFoundException, UnauthorizedRoleException {

        return getUserCreateTaskIds(wfResult, password, vAttrs, null);
    }

    /**
     * Create the user on every associated resource.
     *
     * @param wfResult user to be propagated (and info associated), as per result from workflow
     * @param password to be set
     * @param vAttrs virtual attributes to be set
     * @param syncResourceNames external resources performing sync, hence not to be considered for propagation
     * @return list of propagation tasks
     * @throws NotFoundException if user is not found
     * @throws UnauthorizedRoleException if caller doesn't own enough entitlements to administer the given user
     */
    public List<PropagationTask> getUserCreateTaskIds(final WorkflowResult<Map.Entry<Long, Boolean>> wfResult,
            final String password, final List<AttributeTO> vAttrs, final Set<String> syncResourceNames)
            throws NotFoundException, UnauthorizedRoleException {

        SyncopeUser user = userDataBinder.getUserFromId(wfResult.getResult().getKey());
        if (vAttrs != null && !vAttrs.isEmpty()) {
            userDataBinder.fillVirtual(user, vAttrs, AttributableUtil.getInstance(AttributableType.USER));
        }
        return getCreateTaskIds(user, password, vAttrs,
                wfResult.getResult().getValue(), wfResult.getPropByRes(), syncResourceNames);
    }

    /**
     * Create the role on every associated resource.
     *
     * @param wfResult user to be propagated (and info associated), as per result from workflow
     * @param vAttrs virtual attributes to be set
     * @return list of propagation tasks
     * @throws NotFoundException if role is not found
     * @throws UnauthorizedRoleException if caller doesn't own enough entitlements to administer the given role
     */
    public List<PropagationTask> getRoleCreateTaskIds(final WorkflowResult<Long> wfResult,
            final List<AttributeTO> vAttrs)
            throws NotFoundException, UnauthorizedRoleException {

        return getRoleCreateTaskIds(wfResult, vAttrs, null);
    }

    /**
     * Create the role on every associated resource.
     *
     * @param wfResult role to be propagated (and info associated), as per result from workflow
     * @param vAttrs virtual attributes to be set
     * @param syncResourceNames external resources performing sync, hence not to be considered for propagation
     * @return list of propagation tasks
     * @throws NotFoundException if role is not found
     * @throws UnauthorizedRoleException if caller doesn't own enough entitlements to administer the given role
     */
    public List<PropagationTask> getRoleCreateTaskIds(final WorkflowResult<Long> wfResult,
            final List<AttributeTO> vAttrs, final Set<String> syncResourceNames)
            throws NotFoundException, UnauthorizedRoleException {

        SyncopeRole role = roleDataBinder.getRoleFromId(wfResult.getResult());
        if (vAttrs != null && !vAttrs.isEmpty()) {
            roleDataBinder.fillVirtual(role, vAttrs, AttributableUtil.getInstance(AttributableType.ROLE));
        }
        return getCreateTaskIds(role, null, vAttrs, null, wfResult.getPropByRes(), syncResourceNames);
    }

    protected List<PropagationTask> getCreateTaskIds(final AbstractAttributable attributable,
            final String password, final List<AttributeTO> vAttrs, final Boolean enable,
            final PropagationByResource propByRes, final Set<String> syncResourceNames) {

        if (propByRes == null || propByRes.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        if (syncResourceNames != null) {
            propByRes.get(ResourceOperation.CREATE).removeAll(syncResourceNames);
        }

        return createTasks(attributable, password, null, null, enable, false, propByRes);
    }

    /**
     * Performs update on each resource associated to the user excluding the specified into 'resourceNames' parameter.
     *
     * @param user to be propagated
     * @param enable whether user must be enabled or not
     * @param syncResourceNames external resource names not to be considered for propagation. Use this during sync and
     * disable/enable actions limited to the external resources only
     * @return list of propagation tasks
     * @throws NotFoundException if user is not found
     */
    public List<PropagationTask> getUserUpdateTaskIds(final SyncopeUser user, final Boolean enable,
            final Set<String> syncResourceNames)
            throws NotFoundException {

        return getUpdateTaskIds(
                user, // SyncopeUser to be updated on external resources
                null, // no password
                enable, // status to be propagated
                Collections.<String>emptySet(), // no virtual attributes to be managed
                Collections.<AttributeMod>emptySet(), // no virtual attributes to be managed
                null, // no propagation by resources
                syncResourceNames);
    }

    /**
     * Performs update on each resource associated to the user.
     *
     * @param wfResult user to be propagated (and info associated), as per result from workflow.
     * @return list of propagation tasks
     * @throws NotFoundException if user is not found
     * @throws UnauthorizedRoleException if caller doesn't own enough entitlements to administer the given user
     */
    public List<PropagationTask> getUserUpdateTaskIds(final WorkflowResult<Map.Entry<Long, Boolean>> wfResult)
            throws NotFoundException, UnauthorizedRoleException {

        return getUserUpdateTaskIds(
                wfResult, null, Collections.<String>emptySet(), Collections.<AttributeMod>emptySet(), null);
    }

    /**
     * Performs update on each resource associated to the user.
     *
     * @param wfResult user to be propagated (and info associated), as per result from workflow
     * @param password to be updated
     * @param vAttrsToBeRemoved virtual attributes to be removed
     * @param vAttrsToBeUpdated virtual attributes to be added
     * @return list of propagation tasks
     * @throws NotFoundException if user is not found
     * @throws UnauthorizedRoleException if caller doesn't own enough entitlements to administer the given user
     */
    public List<PropagationTask> getUserUpdateTaskIds(final WorkflowResult<Map.Entry<Long, Boolean>> wfResult,
            final String password, final Set<String> vAttrsToBeRemoved, final Set<AttributeMod> vAttrsToBeUpdated)
            throws NotFoundException, UnauthorizedRoleException {

        return getUserUpdateTaskIds(wfResult, password, vAttrsToBeRemoved, vAttrsToBeUpdated, null);
    }

    /**
     * Performs update on each resource associated to the user.
     *
     * @param wfResult user to be propagated (and info associated), as per result from workflow
     * @param password to be updated
     * @param vAttrsToBeRemoved virtual attributes to be removed
     * @param vAttrsToBeUpdated virtual attributes to be added
     * @param syncResourceNames external resource names not to be considered for propagation. Use this during sync and
     * disable/enable actions limited to the external resources only
     * @return list of propagation tasks
     * @throws NotFoundException if user is not found
     * @throws UnauthorizedRoleException if caller doesn't own enough entitlements to administer the given user
     */
    public List<PropagationTask> getUserUpdateTaskIds(final WorkflowResult<Map.Entry<Long, Boolean>> wfResult,
            final String password, final Set<String> vAttrsToBeRemoved, final Set<AttributeMod> vAttrsToBeUpdated,
            final Set<String> syncResourceNames)
            throws NotFoundException, UnauthorizedRoleException {

        SyncopeUser user = userDataBinder.getUserFromId(wfResult.getResult().getKey());
        return getUpdateTaskIds(user, password, wfResult.getResult().getValue(),
                vAttrsToBeRemoved, vAttrsToBeUpdated, wfResult.getPropByRes(), syncResourceNames);
    }

    /**
     * Performs update on each resource associated to the role.
     *
     * @param wfResult role to be propagated (and info associated), as per result from workflow
     * @param vAttrsToBeRemoved virtual attributes to be removed
     * @param vAttrsToBeUpdated virtual attributes to be added
     * @return list of propagation tasks
     * @throws NotFoundException if role is not found
     * @throws UnauthorizedRoleException if caller doesn't own enough entitlements to administer the given role
     */
    public List<PropagationTask> getRoleUpdateTaskIds(final WorkflowResult<Long> wfResult,
            final Set<String> vAttrsToBeRemoved, final Set<AttributeMod> vAttrsToBeUpdated)
            throws NotFoundException, UnauthorizedRoleException {

        return getRoleUpdateTaskIds(wfResult, vAttrsToBeRemoved, vAttrsToBeUpdated, null);
    }

    /**
     * Performs update on each resource associated to the role.
     *
     * @param wfResult role to be propagated (and info associated), as per result from workflow
     * @param vAttrsToBeRemoved virtual attributes to be removed
     * @param vAttrsToBeUpdated virtual attributes to be added
     * @param syncResourceNames external resource names not to be considered for propagation. Use this during sync and
     * disable/enable actions limited to the external resources only
     * @return list of propagation tasks
     * @throws NotFoundException if role is not found
     * @throws UnauthorizedRoleException if caller doesn't own enough entitlements to administer the given role
     */
    public List<PropagationTask> getRoleUpdateTaskIds(final WorkflowResult<Long> wfResult,
            final Set<String> vAttrsToBeRemoved, final Set<AttributeMod> vAttrsToBeUpdated,
            final Set<String> syncResourceNames)
            throws NotFoundException, UnauthorizedRoleException {

        SyncopeRole role = roleDataBinder.getRoleFromId(wfResult.getResult());
        return getUpdateTaskIds(role, null, null,
                vAttrsToBeRemoved, vAttrsToBeUpdated, wfResult.getPropByRes(), syncResourceNames);
    }

    protected List<PropagationTask> getUpdateTaskIds(final AbstractAttributable attributable,
            final String password, final Boolean enable,
            final Set<String> vAttrsToBeRemoved, final Set<AttributeMod> vAttrsToBeUpdated,
            final PropagationByResource propByRes, final Set<String> syncResourceNames)
            throws NotFoundException {

        AbstractAttributableDataBinder binder = attributable instanceof SyncopeUser
                ? userDataBinder : roleDataBinder;

        PropagationByResource localPropByRes = binder.fillVirtual(attributable, vAttrsToBeRemoved == null
                ? Collections.EMPTY_SET
                : vAttrsToBeRemoved, vAttrsToBeUpdated == null
                ? Collections.EMPTY_SET
                : vAttrsToBeUpdated, AttributableUtil.getInstance(attributable));

        if (propByRes == null || propByRes.isEmpty()) {
            localPropByRes.addAll(ResourceOperation.UPDATE, attributable.getResourceNames());
        } else {
            localPropByRes.merge(propByRes);
        }

        if (syncResourceNames != null) {
            localPropByRes.get(ResourceOperation.CREATE).removeAll(syncResourceNames);
            localPropByRes.get(ResourceOperation.UPDATE).removeAll(syncResourceNames);
            localPropByRes.get(ResourceOperation.DELETE).removeAll(syncResourceNames);
        }

        Map<String, AttributeMod> vAttrsToBeUpdatedMap = null;
        if (vAttrsToBeUpdated != null) {
            vAttrsToBeUpdatedMap = new HashMap<String, AttributeMod>();
            for (AttributeMod attrMod : vAttrsToBeUpdated) {
                vAttrsToBeUpdatedMap.put(attrMod.getSchema(), attrMod);
            }
        }

        return createTasks(attributable, password,
                vAttrsToBeRemoved, vAttrsToBeUpdatedMap, enable, false, localPropByRes);
    }

    /**
     * Perform delete on each resource associated to the user. It is possible to ask for a mandatory provisioning for
     * some resources specifying a set of resource names. Exceptions won't be ignored and the process will be stopped if
     * the creation fails onto a mandatory resource.
     *
     * @param userId to be deleted
     * @return list of propagation tasks
     * @throws NotFoundException if user is not found
     * @throws UnauthorizedRoleException if caller doesn't own enough entitlements to administer the given user
     */
    public List<PropagationTask> getUserDeleteTaskIds(final Long userId)
            throws NotFoundException, UnauthorizedRoleException {

        return getUserDeleteTaskIds(userId, null);
    }

    /**
     * Perform delete on each resource associated to the user. It is possible to ask for a mandatory provisioning for
     * some resources specifying a set of resource names. Exceptions won't be ignored and the process will be stopped if
     * the creation fails onto a mandatory resource.
     *
     * @param userId to be deleted
     * @param syncResourceName name of external resource performing sync, hence not to be considered for propagation
     * @return list of propagation tasks
     * @throws NotFoundException if user is not found
     * @throws UnauthorizedRoleException if caller doesn't own enough entitlements to administer the given user
     */
    public List<PropagationTask> getUserDeleteTaskIds(final Long userId, final String syncResourceName)
            throws NotFoundException, UnauthorizedRoleException {

        SyncopeUser user = userDataBinder.getUserFromId(userId);
        return getDeleteTaskIds(user, syncResourceName);
    }

    /**
     * Perform delete on each resource associated to the role. It is possible to ask for a mandatory provisioning for
     * some resources specifying a set of resource names. Exceptions won't be ignored and the process will be stopped if
     * the creation fails onto a mandatory resource.
     *
     * @param roleId to be deleted
     * @return list of propagation tasks
     * @throws NotFoundException if role is not found
     * @throws UnauthorizedRoleException if caller doesn't own enough entitlements to administer the given role
     */
    public List<PropagationTask> getRoleDeleteTaskIds(final Long roleId)
            throws NotFoundException, UnauthorizedRoleException {

        return getRoleDeleteTaskIds(roleId, null);
    }

    /**
     * Perform delete on each resource associated to the user. It is possible to ask for a mandatory provisioning for
     * some resources specifying a set of resource names. Exceptions won't be ignored and the process will be stopped if
     * the creation fails onto a mandatory resource.
     *
     * @param roleId to be deleted
     * @param syncResourceName name of external resource performing sync, hence not to be considered for propagation
     * @return list of propagation tasks
     * @throws NotFoundException if role is not found
     * @throws UnauthorizedRoleException if caller doesn't own enough entitlements to administer the given role
     */
    public List<PropagationTask> getRoleDeleteTaskIds(final Long roleId, final String syncResourceName)
            throws NotFoundException, UnauthorizedRoleException {

        SyncopeRole role = roleDataBinder.getRoleFromId(roleId);
        return getDeleteTaskIds(role, syncResourceName);
    }

    protected List<PropagationTask> getDeleteTaskIds(final AbstractAttributable attributable,
            final String syncResourceName) {

        final PropagationByResource propByRes = new PropagationByResource();
        propByRes.set(ResourceOperation.DELETE, attributable.getResourceNames());
        if (syncResourceName != null) {
            propByRes.get(ResourceOperation.DELETE).remove(syncResourceName);
        }
        return createTasks(attributable, null, null, null, false, true, propByRes);
    }

    /**
     * Prepare attributes for sending to a connector instance.
     *
     * @param <T> user / role
     * @param subject given user / role
     * @param password clear-text password
     * @param vAttrsToBeRemoved virtual attributes to be removed
     * @param vAttrsToBeUpdated virtual attributes to be added
     * @param enable whether user must be enabled or not
     * @param resource target resource
     * @return account link + prepared attributes
     */
    protected <T extends AbstractAttributable> Map.Entry<String, Set<Attribute>> prepareAttributes(final T subject,
            final String password, final Set<String> vAttrsToBeRemoved,
            final Map<String, AttributeMod> vAttrsToBeUpdated, final Boolean enable, final ExternalResource resource) {

        LOG.debug("Preparing resource attributes for {} on resource {} with attributes {}",
                subject, resource, subject.getAttributes());

        Set<Attribute> attributes = new HashSet<Attribute>();
        String accountId = null;

        final AttributableUtil attrUtil = AttributableUtil.getInstance(subject);
        for (AbstractMappingItem mapping : attrUtil.getMappingItems(resource)) {
            LOG.debug("Processing schema {}", mapping.getIntAttrName());

            try {
                Map.Entry<String, Attribute> preparedAttribute = MappingUtil.prepareAttribute(
                        resource, mapping, subject, password, vAttrsToBeRemoved, vAttrsToBeUpdated);

                if (preparedAttribute.getKey() != null) {
                    accountId = preparedAttribute.getKey();
                }

                if (preparedAttribute.getValue() != null) {
                    final Attribute alreadyAdded = AttributeUtil.find(preparedAttribute.getValue().getName(),
                            attributes);

                    if (alreadyAdded == null) {
                        attributes.add(preparedAttribute.getValue());
                    } else {
                        attributes.remove(alreadyAdded);

                        Set<Object> values = new HashSet<Object>(alreadyAdded.getValue());
                        values.addAll(preparedAttribute.getValue().getValue());

                        attributes.add(AttributeBuilder.build(preparedAttribute.getValue().getName(), values));
                    }
                }
            } catch (Exception e) {
                LOG.debug("Attribute '{}' processing failed", mapping.getIntAttrName(), e);
            }
        }

        attributes.add(MappingUtil.evaluateNAME(subject, resource, accountId));

        if (enable != null) {
            attributes.add(AttributeBuilder.buildEnabled(enable));
        }

        return new SimpleEntry<String, Set<Attribute>>(accountId, attributes);
    }

    /**
     * Create propagation tasks.
     *
     * @param <T> user / role
     * @param subject user / role to be provisioned
     * @param password cleartext password to be provisioned
     * @param vAttrsToBeRemoved virtual attributes to be removed
     * @param vAttrsToBeUpdated virtual attributes to be added
     * @param enable whether user must be enabled or not
     * @param deleteOnResource whether user / role must be deleted anyway from external resource or not
     * @param propByRes operation to be performed per resource
     * @return list of propagation tasks created
     */
    protected <T extends AbstractAttributable> List<PropagationTask> createTasks(final T subject, final String password,
            final Set<String> vAttrsToBeRemoved, final Map<String, AttributeMod> vAttrsToBeUpdated,
            final Boolean enable, final boolean deleteOnResource,
            final PropagationByResource propByRes) {

        LOG.debug("Provisioning subject {}:\n{}", subject, propByRes);

        // Avoid duplicates - see javadoc
        propByRes.purge();
        LOG.debug("After purge: {}", propByRes);

        final List<PropagationTask> tasks = new ArrayList<PropagationTask>();

        for (ResourceOperation operation : ResourceOperation.values()) {
            for (String resourceName : propByRes.get(operation)) {
                final ExternalResource resource = resourceDAO.find(resourceName);
                if (resource == null) {
                    LOG.error("Invalid resource name specified: {}, ignoring...", resourceName);
                } else {
                    PropagationTask task = new PropagationTask();
                    task.setResource(resource);
                    task.setObjectClassName(connObjectUtil.fromAttributable(subject).getObjectClassValue());
                    task.setSubjectType(AttributableUtil.getInstance(subject).getType());
                    if (!deleteOnResource) {
                        task.setSubjectId(subject.getId());
                    }
                    task.setPropagationOperation(operation);
                    task.setPropagationMode(resource.getPropagationMode());
                    task.setOldAccountId(propByRes.getOldAccountId(resource.getName()));

                    Map.Entry<String, Set<Attribute>> preparedAttrs = prepareAttributes(subject, password,
                            vAttrsToBeRemoved, vAttrsToBeUpdated, enable, resource);
                    task.setAccountId(preparedAttrs.getKey());
                    task.setAttributes(preparedAttrs.getValue());

                    tasks.add(task);

                    LOG.debug("PropagationTask created: {}", task);
                }
            }
        }

        return tasks;
    }
}
