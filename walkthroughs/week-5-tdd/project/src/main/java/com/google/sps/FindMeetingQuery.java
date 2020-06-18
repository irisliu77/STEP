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
    // If the request meeting is longer than the duration of one day, return an empty list.
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Collections.emptyList();
    }
    // If there is no event on the calendar currently, return whole day as possible time slot.
    if (events.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    Collection<String> mandatoryAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    // If no mandatory attendees or optional attendees, return whole day.
    if (mandatoryAttendees.isEmpty() && optionalAttendees.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    // Get unavailable time of mandatory attendees.
    List<TimeRange> unavailableTimes = getUnavailableTimes(events, mandatoryAttendees);
    // Merge unavailable intervals with overlaps.
    List<TimeRange> mergedUnavailableTimes = mergeUnavailableTimes(unavailableTimes);
    // Get available time slot base on merged unavailable time.
    List<TimeRange> availableTimes = getAvailableTimes(mergedUnavailableTimes, request.getDuration());
    return availableTimes;
  }

  private List<TimeRange> getUnavailableTimes(Collection<Event> events, Collection<String> mandatoryAttendees) {
    List<TimeRange> unavailableTimes = new ArrayList<>();
    for (Event e : events) {
      Set<String> attendees = e.getAttendees();
      Set<String> intersection = new HashSet<String>(attendees);
      intersection.retainAll(mandatoryAttendees);
      if(!intersection.isEmpty()) {
        unavailableTimes.add(e.getWhen());
      }
    }
    return unavailableTimes;
  }

  /**
  * @param  timeRanges  a list of timeRanges that has overlaps and is not sorted.
  * @return a list of sorted, non-overlaping timeRange based on timeRanges
  */
  private List<TimeRange> mergeUnavailableTimes(List<TimeRange> timeRanges) {
    Collections.sort(timeRanges, TimeRange.ORDER_BY_START);
    List<TimeRange> mergedUnavailableTimes = new ArrayList<>();

    for (TimeRange t : timeRanges) {
      // If the merged list is empty, we can add it to the merged list directly.
      if (mergedUnavailableTimes.isEmpty()) {
        mergedUnavailableTimes.add(t);
      } else {
        TimeRange latestTimeRange = mergedUnavailableTimes.get(mergedUnavailableTimes.size() - 1);
        if (!t.overlaps(latestTimeRange)) {
          // If the previous time slot doesn't overlap with the current time slot, 
          // we can add it to the merged list directly.
          mergedUnavailableTimes.add(t);
        } else {
          // Since we sort the time range by start time,
          // the start time of previous events should be earlier or equal to current event,
          // and we keep the later end time.
          int end = Math.max(t.end(), latestTimeRange.end());
          // Set the previous time range to be the merged new time range.
          mergedUnavailableTimes.set(mergedUnavailableTimes.size() - 1, TimeRange.fromStartEnd(latestTimeRange.start(), end, false));
        }
      }
    }
    return mergedUnavailableTimes;
  }

  private List<TimeRange> getAvailableTimes(List<TimeRange> timeRanges, long duration) {
    List<TimeRange> availableTimes = new ArrayList<>();
    int prevEnd = TimeRange.START_OF_DAY;
    for (TimeRange t : timeRanges) {
      TimeRange availableTimeRange = TimeRange.fromStartEnd(prevEnd, t.start(), false);
      if (availableTimeRange.duration() >= duration) {
        availableTimes.add(availableTimeRange);
      }
      prevEnd = t.end();
    }
    // Deal with the time after the last meeting until the end of day.
    TimeRange lastTimeRange = TimeRange.fromStartEnd(prevEnd, TimeRange.END_OF_DAY, true);
    if (lastTimeRange.duration() >= duration) {
      availableTimes.add(lastTimeRange);
    }
    return availableTimes;
  }
}
