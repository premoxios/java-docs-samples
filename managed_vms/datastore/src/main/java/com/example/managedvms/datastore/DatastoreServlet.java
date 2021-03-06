/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.managedvms.datastore;

import com.google.gcloud.datastore.Datastore;
import com.google.gcloud.datastore.DatastoreOptions;
import com.google.gcloud.datastore.DateTime;
import com.google.gcloud.datastore.Entity;
import com.google.gcloud.datastore.FullEntity;
import com.google.gcloud.datastore.IncompleteKey;
import com.google.gcloud.datastore.KeyFactory;
import com.google.gcloud.datastore.Query;
import com.google.gcloud.datastore.QueryResults;
import com.google.gcloud.datastore.StructuredQuery;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
@WebServlet(name = "datastore", value = "")
public class DatastoreServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    // store only the first two octets of a users ip address
    String userIp = req.getRemoteAddr();
    InetAddress address = InetAddress.getByName(userIp);
    if (address instanceof Inet6Address) {
      // nest indexOf calls to find the second occurrence of a character in a string
      // an alternative is to use Apache Commons Lang: StringUtils.ordinalIndexOf()
      userIp = userIp.substring(0, userIp.indexOf(":", userIp.indexOf(":") + 1)) + ":*:*:*:*:*:*";
    } else if (address instanceof Inet4Address) {
      userIp = userIp.substring(0, userIp.indexOf(".", userIp.indexOf(".") + 1)) + ".*.*";
    }

    Datastore datastore = DatastoreOptions.defaultInstance().service();
    KeyFactory keyFactory = datastore.newKeyFactory().kind("visit");
    IncompleteKey key = keyFactory.kind("visit").newKey();

    // Record a visit to the datastore, storing the IP and timestamp.
    FullEntity<IncompleteKey> curVisit = FullEntity.builder(key)
        .set("user_ip", userIp).set("timestamp", DateTime.now()).build();
    datastore.add(curVisit);

    // Retrieve the last 10 visits from the datastore, ordered by timestamp.
    Query<Entity> query = Query.entityQueryBuilder().kind("visit")
        .orderBy(StructuredQuery.OrderBy.desc("timestamp")).limit(10).build();
    QueryResults<Entity> results = datastore.run(query);

    resp.setContentType("text/plain");
    PrintWriter out = resp.getWriter();
    out.print("Last 10 visits:\n");
    while (results.hasNext()) {
      Entity entity = results.next();
      out.format("Time: %s Addr: %s\n", entity.getDateTime("timestamp"),
          entity.getString("user_ip"));
    }
  }
}
// [END example]
