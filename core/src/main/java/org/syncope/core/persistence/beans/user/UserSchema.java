/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.syncope.core.persistence.beans.user;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import org.syncope.core.persistence.beans.AbstractAttribute;
import org.syncope.core.persistence.beans.AbstractDerivedSchema;
import org.syncope.core.persistence.beans.AbstractSchema;
import org.syncope.core.persistence.beans.SchemaMapping;

@Entity
public class UserSchema extends AbstractSchema {

    @OneToMany(mappedBy = "schema")
    private List<UserAttribute> attributes;
    @ManyToMany(mappedBy = "schemas")
    private List<UserDerivedSchema> derivedSchemas;
    /**
     * All the mappings of the attribute schema.
     */
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "userSchema")
    private List<SchemaMapping> mappings;

    public UserSchema() {
        attributes = new ArrayList<UserAttribute>();
        derivedSchemas = new ArrayList<UserDerivedSchema>();
        mappings = new ArrayList<SchemaMapping>();
    }

    @Override
    public <T extends AbstractAttribute> boolean addAttribute(T attribute) {
        return attributes.add((UserAttribute) attribute);
    }

    @Override
    public <T extends AbstractAttribute> boolean removeAttribute(T attribute) {
        return attributes.remove((UserAttribute) attribute);
    }

    @Override
    public List<? extends AbstractAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(List<? extends AbstractAttribute> attributes) {
        this.attributes = (List<UserAttribute>) attributes;
    }

    @Override
    public <T extends AbstractDerivedSchema> boolean addDerivedSchema(
            T derivedSchema) {

        return derivedSchemas.add((UserDerivedSchema) derivedSchema);
    }

    @Override
    public <T extends AbstractDerivedSchema> boolean removeDerivedSchema(
            T derivedSchema) {

        return derivedSchemas.remove((UserDerivedSchema) derivedSchema);
    }

    @Override
    public List<? extends AbstractDerivedSchema> getDerivedSchemas() {
        return derivedSchemas;
    }

    @Override
    public void setDerivedSchemas(
            List<? extends AbstractDerivedSchema> derivedSchemas) {

        this.derivedSchemas = (List<UserDerivedSchema>) derivedSchemas;
    }

    @Override
    public List<SchemaMapping> getMappings() {
        if (this.mappings == null) {
            this.mappings = new ArrayList<SchemaMapping>();
        }
        return this.mappings;
    }

    @Override
    public void setMappings(List<SchemaMapping> mappings) {
        this.mappings = mappings;
    }

    @Override
    public boolean addMapping(SchemaMapping mapping) {
        if (this.mappings == null) {
            this.mappings = new ArrayList<SchemaMapping>();
        }
        return this.mappings.add(mapping);
    }

    @Override
    public boolean removeMapping(SchemaMapping mapping) {
        if (this.mappings == null) {
            return true;
        }
        return this.mappings.remove(mapping);
    }
}
