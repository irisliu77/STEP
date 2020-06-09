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

$(document).ready(function() {
    // Enable comment function in main page
    handleQuantityButton();
    handleDeleteButton();
    getComments();
    // Enable maps
    initMap();
});

const getComments = function(max) {
    fetch('/data?limit=' + max).then(response => response.json()).then((comments) => {
        const commentsContainer = document.getElementById('comments-container');
        commentsContainer.innerHTML = ''; 
        for (let comment of comments) {
            commentsContainer.appendChild(createCommentElement(comment));
        }
    });
};

/** Creates an <li> element containing text. */
const createCommentElement = function(comment) {
    const commentElement = document.createElement('li');
    commentElement.className = 'comment';
    commentElement.innerText = comment.content;
    return commentElement;
};

const handleQuantityButton = function() {
    $('#quantity-button').click(function() {
        let max = $('#quantity').val();
        getComments(max);
    });
};

const deleteAllComments = function() {
    const request = new Request('/delete-data', {method: 'POST'});
    fetch(request).then(response => {
        getComments();
    });
};

const handleDeleteButton = function() {
    $('#delete-button').click(function() {
        deleteAllComments();
    });
};

const initMap = function() {
    const unc = {lat: 35.905112, lng: -79.046892}
    const map = new google.maps.Map(document.getElementById("map"), {
    center: unc,
    zoom: 14
  });
    addLandmark(map, 35.912533, -79.058543, 
                'Lime & Basil', 'Lime & Basil provides delicious Vietnamese Pho.');     
}

/** Adds a marker that shows an info window when clicked. */
const addLandmark = function(map, lat, lng, title, description) {
    const marker = new google.maps.Marker(
                    {position: {lat: lat, lng: lng}, map: map, title: title});

    const infoWindow = new google.maps.InfoWindow({content: description});
    marker.addListener('click', () => {
        infoWindow.open(map, marker);
    });
    $('#place-list').append('<li>' + title + '</li>');
}
