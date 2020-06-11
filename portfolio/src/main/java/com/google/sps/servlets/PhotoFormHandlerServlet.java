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

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.gson.Gson;
import com.google.sps.data.Post;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/photo-form-handler")
public class PhotoFormHandlerServlet extends HttpServlet {
    private static final String MESSAGE_PARAMETER = "message";
    private static final String IMAGE_PARAMETER = "image";
    private static final String POST_PARAMETER = "Post";
    private static final String URL_PARAMETER = "url";
    private static final String DISPLAY_PARAMETER = "display";

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = request.getParameter(MESSAGE_PARAMETER);
        String imageUrl = getUploadedFileUrl(request, IMAGE_PARAMETER);
        String display = request.getParameter(DISPLAY_PARAMETER);

        Entity post = new Entity(POST_PARAMETER);
        post.setProperty(MESSAGE_PARAMETER, message);
        post.setProperty(URL_PARAMETER, imageUrl);
        post.setProperty(DISPLAY_PARAMETER, display);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(post);
        response.sendRedirect("/photo.html");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Gson gson = new Gson();
        
        List<Post> posts = getPosts();

        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(posts));
    }

    private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
        List<BlobKey> blobKeys = blobs.get(IMAGE_PARAMETER);

        // User submitted form without selecting a file, so we can't get a URL. (devserver)
        if (blobKeys == null || blobKeys.isEmpty()) {
            return null;
        }

        BlobKey blobKey = blobKeys.get(0);
        // User submitted form without selecting a file, so we can't get a URL. (live server)
        BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
        if (blobInfo.getSize() == 0) {
            blobstoreService.delete(blobKey);
            return null;
        }

        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
        try {
            URL url = new URL(imagesService.getServingUrl(options));
            return url.getPath();
        } catch (MalformedURLException e) {
            return imagesService.getServingUrl(options);
        }
    }

    private List<Post> getPosts() {
        Query query = new Query(POST_PARAMETER);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery res = datastore.prepare(query);

        List<Post> posts = new ArrayList<>();
        for(Entity entity : res.asIterable()) {
            String message = (String) entity.getProperty(MESSAGE_PARAMETER);
            String url = (String) entity.getProperty(URL_PARAMETER);
            String display = (String) entity.getProperty(DISPLAY_PARAMETER);
            Post post = new Post(message, url, display);
            posts.add(post);
        }
        return posts;
    }
}
