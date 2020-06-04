// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import com.google.sps.data.Comment;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.sps.data.Comment;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
    private static final String content = "content";
    private static final String timestamp = "timestamp";
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    int limit = getRequestNum(request);
    ArrayList<Comment> comments = getComments(limit);

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      String text = request.getParameter("comment-input");
      long time = System.currentTimeMillis();

      Entity comment = new Entity("Comment");
      comment.setProperty(content, text);
      comment.setProperty(timestamp,time);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(comment);
      int max = getRequestNum(request);  
      response.sendRedirect("/index.html?limit=" + max);
  }

  private int getRequestNum(HttpServletRequest request) {
      String limitString = request.getParameter("limit");
      int limit;
      try {
          limit = Integer.parseInt(limitString);
      } catch(NumberFormatException e) {
          return 10;
      }
      return limit;
  } 

  private ArrayList<Comment> getComments(int limit) {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery res = datastore.prepare(query);

    ArrayList<Comment> comments = new ArrayList<Comment>();
    for(Entity entity : res.asIterable()) {
        String content = (String) entity.getProperty("content");
        long timestamp = (long) entity.getProperty("timestamp");
        Comment comment = new Comment(content, timestamp);
        comments.add(comment);
        if(comments.size() > limit) {
            break;
        }
    }
    return comments;
  }
}
