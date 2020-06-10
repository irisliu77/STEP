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

let map;

/* Editable marker that displays when a user clicks in the map. */
let editMarker;

const initMap = function() {
    const unc = {lat: 35.905112, lng: -79.046892}
    map = new google.maps.Map(document.getElementById("map"), {
    center: unc,
    zoom: 14
  });

    map.addListener('click', (event) => {
        createMarkerForEdit(event.latLng.lat(), event.latLng.lng());
    });

    fetchMarkers();
}

/** Fetches markers from the backend and adds them to the map. */
const fetchMarkers = function() {
    fetch('/markers').then(response => response.json()).then((markers) => {
        markers.forEach((marker) => {
            createMarkerForDisplay(marker.lat, marker.lng, marker.title, marker.description);
            $('#place-list').append('<li>' + marker.title + '</li>');
        });
    });
}

/** Creates a marker that shows a read-only info window when clicked. */
const createMarkerForDisplay = function(lat, lng, title, description) {
    const marker = new google.maps.Marker({position: {lat: lat, lng: lng}, map: map, title: title});
    const content = '<div><h1>'+ title +'</h1></div>' + 
                    '<div><p>' + description + '</p></div>';
    const infoWindow = new google.maps.InfoWindow({content: content});
    marker.addListener('click', () => {
        infoWindow.open(map, marker);
    });
}

/** Sends a marker to the backend for saving. */
const postMarker = function(lat, lng, title, description) {
    const params = new URLSearchParams();
    params.append('lat', lat);
    params.append('lng', lng);
    params.append('title', title);
    params.append('description', description);

    fetch('/markers', {method: 'POST', body: params});
}

/** Creates a marker that shows a textbox the user can edit. */
createMarkerForEdit = function(lat, lng) {
    // If we're already showing an editable marker, then remove it.
    if (editMarker) {
        editMarker.setMap(null);
    }

    editMarker = new google.maps.Marker({position: {lat: lat, lng: lng}, map: map});
    const infoWindow = new google.maps.InfoWindow({content: buildInfoWindowInput(lat, lng)});

    // When the user closes the editable info window, remove the marker.
    google.maps.event.addListener(infoWindow, 'closeclick', () => {
        editMarker.setMap(null);
    });
    infoWindow.open(map, editMarker);
}

/**
 * Builds and returns HTML elements that show an editable textbox and a submit
 * button.
 */
const buildInfoWindowInput = function(lat, lng) {
    const title = document.createElement('input');
    const description = document.createElement('textarea');
    const button = document.createElement('button');
    button.appendChild(document.createTextNode('Submit'));

    button.onclick = () => {
        postMarker(lat, lng, title.value, description.value);
        createMarkerForDisplay(lat, lng, title.value, description.value);
        editMarker.setMap(null);
    };

    const containerDiv = document.createElement('div');
    containerDiv.appendChild(title);
    containerDiv.appendChild(description);
    containerDiv.appendChild(document.createElement('br'));
    containerDiv.appendChild(button);
    
    return containerDiv;
}

