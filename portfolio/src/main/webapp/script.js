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
    $('#unfilter-button').css("display","none");
    handleFilterButton();
    handleUnfilterButton();
});

const handleFilterButton = function() {
    $('#filter-button').on('click',function() {
        $('.photo').css("display","none");
        $('#filter-button').css("display", "none");
        $('#unfilter-button').css("display", "inline");
        const input = $('#photo-input').val();
        $('.' + input).css("display","inline");
    });   
}

const handleUnfilterButton = function() {
   $('#unfilter-button').on('click', function() {
        $('.photo').css("display", "inline");
        $('#filter-button').css("display", "inline");
        $('#unfilter-button').css("display", "none");
        $('#photo-input').val('');
   })
}
