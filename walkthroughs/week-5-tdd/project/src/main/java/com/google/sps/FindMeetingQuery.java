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

package com.google.sps;

import com.google.sps.Event;
import com.google.sps.Events;
import com.google.sps.MeetingRequest;
import com.google.sps.TimeRange;
import java.util.*;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // if the request meeting is longer than the duration of one day
    // return an empty list
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }
    // if there is no event on the calendar currently
    // return whole day as possible time slot
    if (events.size() == 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    Collection<String> mandatoryAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    // if no mandatory attendees or optional attendees, return whole day
    if (mandatoryAttendees.size() == 0 && optionalAttendees.size() == 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    // get unavailable time of mandatory attendees
    List<TimeRange> unavailableTime = getUnavailableTime(events, mandatoryAttendees);
    // merge available intervals with overlaps
    List<TimeRange> mergedUnavailableTime = mergeUnavailableTime(unavailableTime);
    // get available time slot base on merged unavailable time
    List<TimeRange> res = getAvailableTime(mergedUnavailableTime, request.getDuration());
    return res;
  }

  private List<TimeRange> getUnavailableTime(Collection<Event> events, Collection<String> mandatoryAttendees) {
    List<TimeRange> res = new ArrayList<>();
    for (Event e : events) {
      Set<String> attendees = e.getAttendees();
      for (String attendee : attendees) {
        // if there is a least on mandatory attendee for the requested event need to attend this event
        // we mark the duration of this event as unavailable
        if (mandatoryAttendees.contains(attendee)) {
          res.add(e.getWhen());
          break;
        }
      }
    }
    return res;
  }

  private List<TimeRange> mergeUnavailableTime(List<TimeRange> timeRanges) {
    Collections.sort(timeRanges, TimeRange.ORDER_BY_START);
    List<TimeRange> res = new ArrayList<>();

    for (TimeRange t : timeRanges) {
      // if the result list is empty, we can add it to the result list directly
      if (res.isEmpty()) {
        res.add(t);
      } else {
        TimeRange latestTimeRange = res.get(res.size() - 1);

        // if the latest time slot that has been processed doesn't overlap with the time slot currently being processed
        if (!t.overlaps(latestTimeRange)) {
          res.add(t);
        } else {
          // since we sort the time range by start time
          // the start time of previous events should be earlier or equal to current event
          // and we keep the later end time
          int end = Math.max(t.end(), latestTimeRange.end());
          // remove the previous time range with overlap
          res.remove(res.size() - 1);
          res.add(TimeRange.fromStartEnd(latestTimeRange.start(), end, false));
        }
      }
    }
    return res;
  }

  private List<TimeRange> getAvailableTime(List<TimeRange> timeRanges, long duration) {
    List<TimeRange> res = new ArrayList<>();
    int prevEnd = TimeRange.START_OF_DAY;
    for (int i = 0; i < timeRanges.size(); i++) {
      TimeRange avaialbeTimeRange = TimeRange.fromStartEnd(prevEnd, timeRanges.get(i).start(), false);
      if (avaialbeTimeRange.duration() >= duration) {
        res.add(avaialbeTimeRange);
      }
      prevEnd = timeRanges.get(i).end();
    }
    // deal with the time after the last meeting until the end of day
    TimeRange lastTimeRange = TimeRange.fromStartEnd(prevEnd, TimeRange.END_OF_DAY, true);
    if (lastTimeRange.duration() >= duration) {
      res.add(lastTimeRange);
    }
    return res;
  }
}

