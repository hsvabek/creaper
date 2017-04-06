/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.extras.creaper.commands.elytron.realm;

import java.security.Principal;
import java.util.Map;

import org.wildfly.extension.elytron.Configurable;
import org.wildfly.extras.creaper.commands.elytron.CustomImplInvocationRuntimeException;
import org.wildfly.security.auth.SupportLevel;
import org.wildfly.security.auth.server.RealmIdentity;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityRealm;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.evidence.Evidence;

public class AddCustomRealmImpl implements SecurityRealm, Configurable {

    @Override
    public RealmIdentity getRealmIdentity(Principal principal) throws RealmUnavailableException {
        throw new CustomImplInvocationRuntimeException(
            String.format("Custom implementation of [%s] was invoked", this.getClass().getSimpleName()));
    }

    @Override
    public RealmIdentity getRealmIdentity(Evidence evidence) throws RealmUnavailableException {
        throw new CustomImplInvocationRuntimeException(
            String.format("Custom implementation of [%s] was invoked", this.getClass().getSimpleName()));
    }

    @Override
    public SupportLevel getCredentialAcquireSupport(final Class<? extends Credential> credentialType,
        final String algorithmName) throws RealmUnavailableException {
        return SupportLevel.SUPPORTED;
    }

    @Override
    public SupportLevel getEvidenceVerifySupport(final Class<? extends Evidence> evidenceType, final String algorithmName)
        throws RealmUnavailableException {
        return SupportLevel.SUPPORTED;
    }

    @Override
    public void initialize(Map<String, String> configuration) {
        if (configuration.containsKey("throwException")) {
            throw new IllegalStateException("Only test purpose. This exception was thrown on demand.");
        }
    }
}
