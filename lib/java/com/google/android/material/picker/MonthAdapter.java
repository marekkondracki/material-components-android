/*
 * Copyright 2019 The Android Open Source Project
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
 */
package com.google.android.material.picker;

import com.google.android.material.R;

import android.content.Context;
import androidx.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.Calendar;

/**
 * Represents the days of a month with {@link TextView} instances for each day.
 *
 * <p>The number of rows is always equal to the maximum number of weeks that can exist across all
 * months (e.g., 6 for the GregorianCalendar).
 */
class MonthAdapter extends BaseAdapter {

  /**
   * The maximum number of weeks possible in any month. 6 for {@link java.util.GregorianCalendar}.
   */
  static final int MAXIMUM_WEEKS = Calendar.getInstance().getMaximum(Calendar.WEEK_OF_MONTH);

  private final Month month;
  /**
   * The {@link DateSelector} dictating the draw behavior of {@link #getView(int, View, ViewGroup)}.
   */
  final DateSelector<?> dateSelector;

  CalendarStyle calendarStyle;
  final CalendarConstraints calendarConstraints;

  MonthAdapter(Month month, DateSelector<?> dateSelector, CalendarConstraints calendarConstraints) {
    this.month = month;
    this.dateSelector = dateSelector;
    this.calendarConstraints = calendarConstraints;
  }

  /**
   * Returns a {@link Calendar} object for the given grid position
   *
   * @param position Index for the item. 0 matches the {@link Calendar#getFirstDayOfWeek()} for the
   *     first week of the month represented by {@link Month}.
   * @return An {@link Calendar} representing the day at the position or null if the position does
   *     not represent a valid day in the month.
   */
  @Nullable
  @Override
  public Long getItem(int position) {
    if (position < month.daysFromStartOfWeekToFirstOfMonth() || position > lastPositionInMonth()) {
      return null;
    }
    return month.getDay(positionToDay(position));
  }

  @Override
  public long getItemId(int position) {
    return position / month.daysInWeek;
  }

  /**
   * Returns the number of days in a week times the maximum number of weeks in any possible month.
   * This number is at least the number of days in the month.
   *
   * @return The maximum valid position index
   */
  @Override
  public int getCount() {
    return month.daysInWeek * MAXIMUM_WEEKS;
  }

  @Override
  public TextView getView(int position, View convertView, ViewGroup parent) {
    initializeStyles(parent.getContext());
    TextView day = (TextView) convertView;
    if (convertView == null) {
      LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
      day = (TextView) layoutInflater.inflate(R.layout.mtrl_calendar_day, parent, false);
    }
    int offsetPosition = position - firstPositionInMonth();
    if (offsetPosition < 0 || offsetPosition >= month.daysInMonth) {
      day.setVisibility(View.GONE);
    } else {
      // The tag and text uniquely identify the view within the MaterialCalendar for testing
      day.setText(String.valueOf(offsetPosition + 1));
      day.setTag(month);
      day.setVisibility(View.VISIBLE);
    }

    Long date = getItem(position);
    if (date != null) {
      if (calendarConstraints.getDateValidator().isValid(date)) {
        day.setEnabled(true);
        if (dateSelector.getSelectedDays().contains(date)) {
          calendarStyle.selectedDay.styleItem(day);
        } else if (DateUtils.isToday(date)) {
          calendarStyle.todayDay.styleItem(day);
        } else {
          calendarStyle.day.styleItem(day);
        }
      } else {
        day.setEnabled(false);
        calendarStyle.invalidDay.styleItem(day);
      }
    }
    return day;
  }

  private void initializeStyles(Context context) {
    if (calendarStyle == null) {
      calendarStyle = new CalendarStyle(context);
    }
  }

  /**
   * Returns the index of the first position which is part of the month.
   *
   * <p>For example, this returns the position index representing February 1st. Since position 0
   * represents a day which must be the first day of the week, the first position in the month may
   * be greater than 0.
   */
  int firstPositionInMonth() {
    return month.daysFromStartOfWeekToFirstOfMonth();
  }

  /**
   * Returns the index of the last position which is part of the month.
   *
   * <p>For example, this returns the position index representing November 30th. Since position 0
   * represents a day which must be the first day of the week, the last position in the month may
   * not match the number of days in the month.
   */
  int lastPositionInMonth() {
    return month.daysFromStartOfWeekToFirstOfMonth() + month.daysInMonth - 1;
  }

  /**
   * Returns the day representing the provided adapter index
   *
   * @param position The adapter index
   * @return The day corresponding to the adapter index. May be non-positive for position inputs
   *     less than {@link MonthAdapter#firstPositionInMonth()}.
   */
  int positionToDay(int position) {
    return position - month.daysFromStartOfWeekToFirstOfMonth() + 1;
  }

  /** Returns the adapter index representing the provided day. */
  int dayToPosition(int day) {
    int offsetFromFirst = day - 1;
    return firstPositionInMonth() + offsetFromFirst;
  }

  /** True when a provided adapter position is within the calendar month */
  boolean withinMonth(int position) {
    return position >= firstPositionInMonth() && position <= lastPositionInMonth();
  }

  /**
   * True when the provided adapter position is the smallest position for a value of {@link
   * MonthAdapter#getItemId(int)}.
   */
  boolean isFirstInRow(int position) {
    return position % month.daysInWeek == 0;
  }

  /**
   * True when the provided adapter position is the largest position for a value of {@link
   * MonthAdapter#getItemId(int)}.
   */
  boolean isLastInRow(int position) {
    return (position + 1) % month.daysInWeek == 0;
  }
}
