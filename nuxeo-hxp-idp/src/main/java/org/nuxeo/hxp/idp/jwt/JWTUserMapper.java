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

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.hxp.idp.HxPUserInfo;
import org.nuxeo.hxp.idp.HxPUserManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.usermapper.extension.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User mapper relying on HxPUserManager
 */
public class JWTUserMapper implements UserMapper {

    private static final Logger log = LoggerFactory.getLogger(JWTUserMapper.class);

    protected UserManager nuxeoUserManager;
    protected HxPUserManager hxpUserManager;

    @Override
    public NuxeoPrincipal getOrCreateAndUpdateNuxeoPrincipal(Object userObject) {
        return getOrCreateAndUpdateNuxeoPrincipal(userObject, true, true, null);
    }

    @Override
    public NuxeoPrincipal getOrCreateAndUpdateNuxeoPrincipal(Object token, boolean createIfNeeded, boolean update,
                                                             Map<String, Serializable> params) {
        return  Framework.doPrivileged(() -> {

            HxPUserInfo userInfo = hxpUserManager.me((String) token);

            for (String role : userInfo.getGroups()) {
                findOrCreateGroup(role, userInfo.getUsername());
            }

            DocumentModel userDoc = findUser(userInfo);
            if (userDoc == null) {
                userDoc = createUser(userInfo);
            }

            updateUser(userDoc, userInfo);

            String userId = (String) userDoc.getPropertyValue(nuxeoUserManager.getUserIdField());
            return nuxeoUserManager.getPrincipal(userId);
        });
    }

    @Override
    public void init(Map<String, String> params) throws Exception {
        nuxeoUserManager = Framework.getService(UserManager.class);
        String url = params.get("url");
        if (url == null) {
            throw new IllegalArgumentException("Missing 'url' for HxP user directory in JWT mapper");
        }
        hxpUserManager = new HxPUserManager(url);
    }

    private DocumentModel findOrCreateGroup(String role, String userName) {
        DocumentModel groupDoc = findGroup(role);
        if (groupDoc == null) {
            groupDoc = nuxeoUserManager.getBareGroupModel();
            groupDoc.setPropertyValue(nuxeoUserManager.getGroupIdField(), role);
            groupDoc.setPropertyValue(nuxeoUserManager.getGroupLabelField(), role + " group");
            groupDoc = nuxeoUserManager.createGroup(groupDoc);
        }
        List<String> users = nuxeoUserManager.getUsersInGroupAndSubGroups(role);
        if (!users.contains(userName)) {
            users.add(userName);
            groupDoc.setProperty(nuxeoUserManager.getGroupSchemaName(), nuxeoUserManager.getGroupMembersField(), users);
            nuxeoUserManager.updateGroup(groupDoc);
        }
        return groupDoc;
    }

    private DocumentModel findGroup(String role) {
        Map<String, Serializable> query = new HashMap<>();
        query.put(nuxeoUserManager.getGroupIdField(), role);
        DocumentModelList groups = nuxeoUserManager.searchGroups(query, null);

        if (groups.isEmpty()) {
            return null;
        }
        return groups.get(0);
    }

    private DocumentModel findUser(HxPUserInfo userInfo) {
        Map<String, Serializable> query = new HashMap<>();
        query.put(nuxeoUserManager.getUserIdField(), userInfo.getUsername());
        DocumentModelList users = nuxeoUserManager.searchUsers(query, null);

        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    private DocumentModel createUser(HxPUserInfo userInfo) {
        try {
            DocumentModel userDoc = nuxeoUserManager.getBareUserModel();
            userDoc.setPropertyValue(nuxeoUserManager.getUserIdField(), userInfo.getUsername());
            userDoc.setPropertyValue(nuxeoUserManager.getUserEmailField(), userInfo.getEmail());
            return nuxeoUserManager.createUser(userDoc);
        } catch (NuxeoException e) {
            String message = "Error while creating user [" + userInfo.getUsername() + "] in UserManager";
            log.error(message, e);
            throw new RuntimeException(message);
        }
    }

    private void updateUser(DocumentModel userDoc, HxPUserInfo userInfo) {
        userDoc.setPropertyValue(nuxeoUserManager.getUserIdField(), userInfo.getUsername());
        userDoc.setPropertyValue(nuxeoUserManager.getUserEmailField(), userInfo.getEmail());
        userDoc.setProperty(nuxeoUserManager.getUserSchemaName(), "firstName", userInfo.getFirstName());
        userDoc.setProperty(nuxeoUserManager.getUserSchemaName(), "lastName", userInfo.getLastName());
        nuxeoUserManager.updateUser(userDoc);
    }

    @Override
    public Object wrapNuxeoPrincipal(NuxeoPrincipal principal, Object nativePrincipal, Map<String, Serializable> params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void release() {
    }

}
