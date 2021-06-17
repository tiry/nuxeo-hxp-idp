/*
 * (C) Copyright 2021 Nuxeo SA (http://nuxeo.com/) and others.
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
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 */

package org.nuxeo.hxp.idp;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.oauth2.openid.OpenIDConnectProvider;
import org.nuxeo.ecm.platform.oauth2.openid.auth.OpenIDUserInfo;
import org.nuxeo.ecm.platform.oauth2.openid.auth.UserResolver;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.usermapper.extension.UserMapper;
import org.nuxeo.usermapper.service.UserMapperService;

/**
 * Dedicated UserResolver class for 2 reasons:
 * 
 *  - default implementation is broken if nuxeo.oauth.auth.create.user is not set to true
 *  - this allows to add java logic if needed later
 * 
 */
public class HxPUserResolver extends UserResolver {

    protected final String mapperName = "hxp_mapper";

    public HxPUserResolver(OpenIDConnectProvider provider) {
        super(provider);
    }

    @Override
    public String findOrCreateNuxeoUser(OpenIDUserInfo userInfo) {
        UserMapper mapper = Framework.getService(UserMapperService.class).getMapper(mapperName);
        NuxeoPrincipal principal = Framework.doPrivileged(() -> mapper.getOrCreateAndUpdateNuxeoPrincipal(userInfo));
        if (principal != null) {
            return principal.getName();
        } else {
            return null;
        }
    }

    @Override
    protected String findNuxeoUser(OpenIDUserInfo userInfo) {
    	 UserMapper mapper = Framework.getService(UserMapperService.class).getMapper(mapperName);
         NuxeoPrincipal principal = Framework.doPrivileged(() -> mapper.getOrCreateAndUpdateNuxeoPrincipal(userInfo, false, false, null));
         if (principal != null) {
             return principal.getName();
         } else {
             return null;
         }
    }

    @Override
    protected DocumentModel updateUserInfo(DocumentModel user, OpenIDUserInfo userInfo) {
        throw new UnsupportedOperationException();
    }

}
