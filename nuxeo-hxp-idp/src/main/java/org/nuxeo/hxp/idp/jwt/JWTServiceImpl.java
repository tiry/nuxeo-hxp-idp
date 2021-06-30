/*
 * (C) Copyright 2021 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.nuxeo.hxp.idp.jwt;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Extends {@link org.nuxeo.ecm.jwt.JWTServiceImpl} to add RS256 support.
 */
public class JWTServiceImpl extends org.nuxeo.ecm.jwt.JWTServiceImpl {

    private static final Log log = LogFactory.getLog(JWTServiceImpl.class);

    protected JwkProvider jwkProvider;

    protected JwkProvider getJwkProvider() {
        if (jwkProvider == null) {
            try {
                URL url = new URL(((JWTServiceConfigurationDescriptor) configuration).getJwksUrl());
                jwkProvider = new JwkProviderBuilder(url)
                        .cached(10, 24, TimeUnit.HOURS)
                        .rateLimited(10, 1, TimeUnit.MINUTES)
                        .build();
            } catch (MalformedURLException e) {
                log.error("wrong jwks url", e);
            }
        }
        return jwkProvider;
    }

    protected RSAKeyProvider getRSAKeyProvider() throws JwkException {
        return new RSAKeyProvider() {
            public RSAPublicKey getPublicKeyById(String keyId) {
                try {
                    return (RSAPublicKey) getJwkProvider().get(keyId).getPublicKey();
                } catch (JwkException e) {
                    log.error("failure retrieving public key", e);
                    return null;
                }
            }

            public RSAPrivateKey getPrivateKey() {
                return null;
            }

            public String getPrivateKeyId() {
                return null;
            }
        };
    }

    protected JWTServiceConfigurationDescriptor getConfiguration() {
        if (configuration == null) {
            configuration = this.<JWTServiceConfigurationDescriptor>getRegistryContribution(XP_CONFIGURATION)
                    .orElse(new JWTServiceConfigurationDescriptor());
        }
        return (JWTServiceConfigurationDescriptor) configuration;
    }

    @Override
    protected Algorithm getAlgorithm() {
        String secret = getConfiguration().getSecret();
        if (isBlank(secret)) {
            return null;
        }
        switch (getConfiguration().getAlgorithm()) {
            case "SHA512":
                return Algorithm.HMAC512(secret);
            case "RS256":
                try {
                    return Algorithm.RSA256(getRSAKeyProvider());
                } catch (JwkException e) {
                    log.error("JWK error", e);
                    return null;
                }
            default:
                log.error("unsupported algorithm");
                return null;
        }
    }

}
