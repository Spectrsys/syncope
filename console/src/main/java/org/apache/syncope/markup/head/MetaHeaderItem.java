/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.syncope.markup.head;

import java.io.Serializable;
import java.util.Arrays;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.Response;

public class MetaHeaderItem extends HeaderItem implements Serializable {

    private static final long serialVersionUID = 7578609827530302053L;

    private final String key;

    private final String value;

    public MetaHeaderItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Iterable<?> getRenderTokens() {
        return Arrays.asList("meta-" + key + "-" + value);
    }

    @Override
    public void render(final Response response) {
        response.write("<meta http-equiv=\"" + key + "\" content=\"" + value + "\"/>");
        response.write("\n");
    }
}
