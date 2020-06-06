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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
    private static final String CONTENT_PARAMETER = "content";
    private static final String TIMESTAMP_PARAMETER = "timestamp";
    private static final String COMMENT_PARAMETER = "Comment";
    private static final int DEFAULT_MAX_COMMENTS = 10;
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Gson gson = new Gson();
        int limit = getRequestNum(request);
        List<Comment> comments = getComments(limit);

        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(comments));
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String content = request.getParameter("comment-input");
        long timestamp = System.currentTimeMillis();

        Entity comment = new Entity(COMMENT_PARAMETER);
        comment.setProperty(CONTENT_PARAMETER, content);
        comment.setProperty(TIMESTAMP_PARAMETER, timestamp);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(comment);
        response.sendRedirect("/index.html");
    }

    private int getRequestNum(HttpServletRequest request) {
        String limitString = request.getParameter("limit");
        int limit;
        try {
            limit = Integer.parseInt(limitString);
        } catch(NumberFormatException e) {
            limit = DEFAULT_MAX_COMMENTS;
        }
        return limit;
    } 

    private List<Comment> getComments(int limit) {
        Query query = new Query("Comment").addSort(TIMESTAMP_PARAMETER, SortDirection.DESCENDING);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery res = datastore.prepare(query);

        List<Comment> comments = new ArrayList<>();
        for(Entity entity : res.asIterable()) {
            String content = (String) entity.getProperty(CONTENT_PARAMETER);
            long timestamp = (long) entity.getProperty(TIMESTAMP_PARAMETER);
            Comment comment = new Comment(content, timestamp);
            comments.add(comment);
            if(comments.size() >= limit) {
                break;
            }
        }
        return comments;
    }
}
