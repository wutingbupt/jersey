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
package org.glassfish.jersey.server;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.glassfish.jersey.server.model.ModelValidationException;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test basic application behavior.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ApplicationHandlerTest {

    ApplicationHandler application;

    private ApplicationHandler createApplication(Class<?>... classes) {
        final ResourceConfig resourceConfig = new ResourceConfig(classes);

        return new ApplicationHandler(resourceConfig);
    }

    @Path("/")
    public static class Resource {

        @GET
        public String doGetFoo(@Context HttpHeaders headers) {

            return Integer.toString(headers.getLength());
        }
    }

    @Path("merged")
    public static class MergedA {

        public static final String RESPONSE = "Got in A";

        @GET
        public String doGet() {
            return RESPONSE;
        }
    }

    @Path("merged")
    public static class MergedA1 {

        public static final String RESPONSE = "Got in A";

        @GET
        public String doGet() {
            return RESPONSE;
        }
    }

    @Path("merged")
    public static class MergedB {

        public static final String RESPONSE = "Posted in B";

        @POST
        public String doPost() {

            return RESPONSE;
        }
    }

    @Test
    public void testReturnBadRequestOnIllHeaderValue() throws Exception {
        ApplicationHandler app = createApplication(Resource.class);

        assertEquals(400,
                app.apply(RequestContextBuilder.from("/", "GET").header(HttpHeaders.CONTENT_LENGTH, "text").build())
                        .get().getStatus());
    }

    @Test
    public void testMergedResources() throws Exception {
        ApplicationHandler app = createApplication(MergedA.class, MergedB.class);

        ContainerResponse response;

        response = app.apply(RequestContextBuilder.from("/merged", "GET").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals(MergedA.RESPONSE, response.getEntity());

        response = app.apply(RequestContextBuilder.from("/merged", "POST").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals(MergedB.RESPONSE, response.getEntity());
    }

    /**
     * This test ensures that resource validation kicks in AFTER resources are merged.
     */
    @Test
    public void testMergedResourcesValidationFailure() throws Exception {
        try {
            createApplication(MergedA.class, MergedA1.class);
        } catch (ModelValidationException ex) {
            // success
            return;
        }

        fail("Model validation exception was expected but not thrown.");
    }
}
