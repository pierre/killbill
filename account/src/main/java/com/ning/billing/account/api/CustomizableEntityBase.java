/*
 * Copyright 2010-2011 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.account.api;

import java.util.UUID;

public abstract class CustomizableEntityBase extends EntityBase implements ICustomizableEntity {
    protected final FieldStore fields;

    public CustomizableEntityBase(UUID id) {
        super(id);
        fields = FieldStore.create(getId(), getObjectName());
    }

    @Override
    public String getFieldValue(String fieldName) {
        return fields.getValue(fieldName);
    }

    @Override
    public void setFieldValue(String fieldName, String fieldValue) {
        fields.setValue(fieldName, fieldValue);
    }

    @Override
    public void save() {
        super.save();
        fields.save();
    }

    @Override
    public void load() {
        loadObject();
        loadCustomFields();
    }

    protected void loadCustomFields() {
        fields.load();
    }

    public abstract String getObjectName();

    protected abstract void saveObject();

    protected abstract void loadObject();
}