/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.myenergi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openhab.core.io.net.http.HttpClientFactory;

/**
 * The {@link MyEnergiGetHostFromDirectorTest} is a test class for {@link MyEnergiGetHostFromDirector}.
 *
 * @author Volkmar Nissen - Initial contribution
 */
@NonNullByDefault
class MyEnergiGetHostFromDirectorTest {

    class HttpClientFactoryForTest implements HttpClientFactory {

        @Override
        public HttpClient createHttpClient(String consumerName) {
            SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
            sslContextFactory.setTrustAll(true); // you might want to think about this first
            return new HttpClient(sslContextFactory);
        }

        @Override
        public HttpClient getCommonHttpClient() {
            // TODO Auto-generated method stub
            return new HttpClient();
        }
    }

    /**
     * There is no password check at director.myenergi.net. So, the password can be left empty.
     * Only the hub serial number must be an existing one. May be this changes in the future.
     * This test uses Internet URL to director.myenergi.net. The access is not mocked
     *
     * @throws Exception
     */
    @Test
    void testGetHostName() throws Exception {
        HttpClient client = new HttpClientFactoryForTest()
                .createHttpClient(MyEnergiGetHostFromDirectorTest.class.getSimpleName());
        try {

            client.start();
            String hostName = new MyEnergiGetHostFromDirector().getHostName(client, "12215753");
            Assertions.assertTrue(hostName.contains("myenergi"));
        } catch (Exception e) {
            Assertions.fail("Exception caught" + e.getMessage());
        } finally {
            client.stop();
        }
    }
}
