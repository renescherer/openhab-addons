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

/**
 * The {@link MyEnergiApiException} is thrown during API interactions.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class MyEnergiApiException extends Exception {

    private static final long serialVersionUID = -7851429813904230535L;

    public MyEnergiApiException() {
        super();
    }

    public MyEnergiApiException(String message) {
        super(message);
    }

    public MyEnergiApiException(Throwable cause) {
        super(cause);
    }

    public MyEnergiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
