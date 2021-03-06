/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.server.internal.inject;

import javax.ws.rs.ext.ParamConverter;

import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

/**
 * Abstract base class for implementing multivalued parameter value extractor
 * logic supplied using {@link ParamConverter parameter converters}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
abstract class AbstractParamValueExtractor<T> {

    private final ParamConverter<T> paramConverter;
    private final String parameterName;
    private final String defaultValueString;
    private final Value<T> convertedDefaultValue;

    /**
     * Constructor that initializes common string reader-based parameter extractor
     * data.
     * <p />
     * As part of the initialization, the default value validation is performed
     * based on the presence and value of the {@link ParamConverter.Lazy}
     * annotation on the supplied string value reader class.
     *
     * @param converter parameter converter.
     * @param parameterName name of the parameter.
     * @param defaultValueString default parameter value string.
     */
    protected AbstractParamValueExtractor(ParamConverter<T> converter, String parameterName, final String defaultValueString) {
        this.paramConverter = converter;
        this.parameterName = parameterName;
        this.defaultValueString = defaultValueString;


        if (defaultValueString != null) {
            this.convertedDefaultValue = Values.lazy(new Value<T>() {
                @Override
                public T get() {
                    return paramConverter.fromString(defaultValueString);
                }
            });

            if (!converter.getClass().isAnnotationPresent(ParamConverter.Lazy.class)) {
                // ignore return value - executed just for validation reasons
                convertedDefaultValue.get();
            }
        } else {
            convertedDefaultValue = null;
        }
    }

    public String getName() {
        return parameterName;
    }

    public String getDefaultValueString() {
        return defaultValueString;
    }

    protected final T fromString(String value) {
        T result = paramConverter.fromString(value);
        if (result == null) {
            return defaultValue();
        }
        return result;
    }

    protected final boolean isDefaultValueRegistered() {
        return defaultValueString != null;
    }

    protected final T defaultValue() {
        if (!isDefaultValueRegistered()) {
            return null;
        }

        return convertedDefaultValue.get();
    }
}
