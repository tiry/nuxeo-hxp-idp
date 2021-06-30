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

import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.api.login.UserIdentificationInfo;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.usermapper.extension.UserMapper;
import org.nuxeo.usermapper.service.UserMapperService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Extends {@link org.nuxeo.ecm.jwt.JWTAuthenticator} to add user mapper support
 */
public class JWTAuthenticator extends org.nuxeo.ecm.jwt.JWTAuthenticator {

    protected static final String DEFAULT_USER_MAPPER = "jwt";

    @Override
    public UserIdentificationInfo handleRetrieveIdentity(HttpServletRequest request, HttpServletResponse response) {
        // reuse validation logics
        UserIdentificationInfo ident = super.handleRetrieveIdentity(request, response);
        if (ident == null) {
            return null;
        }

        UserMapperService ums = Framework.getService(UserMapperService.class);
        if (ums.getAvailableMappings().contains(DEFAULT_USER_MAPPER)) {
            UserMapper userMapper = ums.getMapper(DEFAULT_USER_MAPPER);
            if (userMapper != null) {
                String token = retrieveToken(request);
                NuxeoPrincipal principal = userMapper.getOrCreateAndUpdateNuxeoPrincipal(token);
                ident = new UserIdentificationInfo(principal.getName());
            }
        }

        return ident;
    }
}
