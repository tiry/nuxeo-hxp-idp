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
package org.nuxeo.hxp.idp;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Client for HxP's user directory
 */
public class HxPUserManager {

    private static final Logger log = LoggerFactory.getLogger(HxPUserManager.class);

    protected final URI uri;

    protected HttpClient client = HttpClient.newHttpClient();

    public HxPUserManager(String url) {
        uri = URI.create(url);
    }

    protected JSONObject query(String gql, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString("{\"query\": \"" + gql + "\"}"))
                .build();
        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            JSONTokener tokener = new JSONTokener(response.body());
            JSONObject json = new JSONObject(tokener);
            // XXX: error handling
            return json.getJSONObject("data");
        } catch (IOException | InterruptedException e) {
            log.error("graphql query failed", e);
            return null;
        }
    }

    public HxPUserInfo me(String token) {
        JSONObject json = query("{ me { userName name { familyName givenName } emails { value } groups { id displayName } } }", token);
        json = json.getJSONObject("me");
        // XXX: use proper object mapper
        HxPUserInfo info = new HxPUserInfo();
        info.setUsername(json.getString("userName"));
        JSONObject name = json.getJSONObject("name");
        info.setFirstName(name.getString("givenName"));
        info.setLastName(name.getString("familyName"));
        JSONArray emails = json.getJSONArray("emails");
        if (!emails.isEmpty()) {
            // XXX: just using first email for now
            info.setEmail(emails.getJSONObject(0).getString("value"));
        }
        Set<String> groups = json.getJSONArray("groups").toList().stream()
                .map(group -> (String) ((Map) group).get("displayName")).collect(Collectors.toSet());
        info.setGroups(groups);
        return info;
    }
}
