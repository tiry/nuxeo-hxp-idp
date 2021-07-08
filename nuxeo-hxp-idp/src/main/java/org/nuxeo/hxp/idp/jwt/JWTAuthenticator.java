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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.jwt.JWTService;
import org.nuxeo.ecm.platform.api.login.UserIdentificationInfo;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.usermapper.extension.UserMapper;
import org.nuxeo.usermapper.service.UserMapperService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.nuxeo.ecm.jwt.JWTClaims.CLAIM_SUBJECT;

/**
 * Extends {@link org.nuxeo.ecm.jwt.JWTAuthenticator} to add user mapper support
 */
public class JWTAuthenticator extends org.nuxeo.ecm.jwt.JWTAuthenticator {

    private static final Log log = LogFactory.getLog(JWTAuthenticator.class);

    protected static final String DEFAULT_USER_MAPPER = "jwt";

    @Override
    public UserIdentificationInfo handleRetrieveIdentity(HttpServletRequest request, HttpServletResponse response) {
        // XXX: reuse validation logics (audience claim validation is application specific)
        String token = retrieveToken(request);
        if (token == null) {
            log.trace("No JWT token");
            return null;
        }
        JWTService service = Framework.getService(JWTService.class);
        Map<String, Object> claims = service.verifyToken(token);
        if (claims == null) {
            log.trace("JWT token invalid");
            return null;
        }
        Object sub = claims.get(CLAIM_SUBJECT);
        if (!(sub instanceof String)) {
            log.trace("JWT token contains non-String subject claim");
            return null;
        }
        if (log.isTraceEnabled()) {
            log.trace("JWT token valid for subject: " + sub);
        }

        UserMapperService ums = Framework.getService(UserMapperService.class);
        if (ums.getAvailableMappings().contains(DEFAULT_USER_MAPPER)) {
            UserMapper userMapper = ums.getMapper(DEFAULT_USER_MAPPER);
            if (userMapper != null) {
                NuxeoPrincipal principal = userMapper.getOrCreateAndUpdateNuxeoPrincipal(token);
                return new UserIdentificationInfo(principal.getName());
            }
        }

        return null;
    }
}
