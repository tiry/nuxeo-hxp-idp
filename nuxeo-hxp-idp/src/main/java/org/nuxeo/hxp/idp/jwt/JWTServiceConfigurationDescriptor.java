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

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.common.xmap.registry.XRegistry;

@XObject(value = "configuration")
@XRegistry
public class JWTServiceConfigurationDescriptor extends org.nuxeo.ecm.jwt.JWTServiceConfigurationDescriptor {

    @XNode("algorithm")
    public String algorithm = "SHA512";

    @XNode("jwksUrl")
    public String jwksUrl;

    public String getAlgorithm() {
        return algorithm;
    }

    public String getJwksUrl() {
        return jwksUrl;
    }
}
