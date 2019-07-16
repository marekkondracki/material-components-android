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

import android.content.Context;
import android.graphics.Canvas;
import androidx.core.util.Pair;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;
import java.util.Calendar;

final class MaterialCalendarGridView extends GridView {

  private final Calendar dayCompute = Calendar.getInstance();

  public MaterialCalendarGridView(Context context) {
    this(context, null);
  }

  public MaterialCalendarGridView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MaterialCalendarGridView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    getAdapter().notifyDataSetChanged();
  }

  @Override
  public MonthAdapter getAdapter() {
    return (MonthAdapter) super.getAdapter();
  }

  @Override
  public final void setAdapter(ListAdapter adapter) {
    if (!(adapter instanceof MonthAdapter)) {
      throw new IllegalArgumentException(
          String.format(
              "%1$s must have its Adapter set to a %2$s",
              MaterialCalendarGridView.class.getCanonicalName(),
              MonthAdapter.class.getCanonicalName()));
    }
    super.setAdapter(adapter);
  }

  @Override
  protected final void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    MonthAdapter monthAdapter = getAdapter();
    DateSelector<?> dateSelector = monthAdapter.dateSelector;
    CalendarStyle calendarStyle = monthAdapter.calendarStyle;
    Long firstOfMonth = monthAdapter.getItem(monthAdapter.firstPositionInMonth());
    Long lastOfMonth = monthAdapter.getItem(monthAdapter.lastPositionInMonth());

    for (Pair<Long, Long> range : dateSelector.getSelectedRanges()) {
      if (range.first == null || range.second == null) {
        continue;
      }
      long startItem = range.first;
      long endItem = range.second;

      if (skipMonth(firstOfMonth, lastOfMonth, startItem, endItem)) {
        return;
      }

      int firstHighlightPosition;
      int rangeHighlightStart;
      if (startItem < firstOfMonth) {
        firstHighlightPosition = monthAdapter.firstPositionInMonth();
        rangeHighlightStart =
            monthAdapter.isFirstInRow(firstHighlightPosition)
                ? 0
                : getChildAt(firstHighlightPosition - 1).getRight();
      } else {
        dayCompute.setTimeInMillis(startItem);
        firstHighlightPosition = monthAdapter.dayToPosition(dayCompute.get(Calendar.DAY_OF_MONTH));
        rangeHighlightStart = horizontalMidPoint(getChildAt(firstHighlightPosition));
      }

      int lastHighlightPosition;
      int rangeHighlightEnd;
      if (endItem > lastOfMonth) {
        lastHighlightPosition = monthAdapter.lastPositionInMonth();
        rangeHighlightEnd =
            monthAdapter.isLastInRow(lastHighlightPosition)
                ? getWidth()
                : getChildAt(lastHighlightPosition + 1).getLeft();
      } else {
        dayCompute.setTimeInMillis(endItem);
        lastHighlightPosition = monthAdapter.dayToPosition(dayCompute.get(Calendar.DAY_OF_MONTH));
        rangeHighlightEnd = horizontalMidPoint(getChildAt(lastHighlightPosition));
      }

      int firstRow = (int) monthAdapter.getItemId(firstHighlightPosition);
      int lastRow = (int) monthAdapter.getItemId(lastHighlightPosition);
      for (int row = firstRow; row <= lastRow; row++) {
        int firstPositionInRow = row * getNumColumns();
        int lastPositionInRow = firstPositionInRow + getNumColumns() - 1;
        View firstView = getChildAt(firstPositionInRow);
        int top = firstView.getTop() + calendarStyle.day.getTopInset();
        int bottom = firstView.getBottom() - calendarStyle.day.getBottomInset();
        int left = firstPositionInRow > firstHighlightPosition ? 0 : rangeHighlightStart;
        int right = lastHighlightPosition > lastPositionInRow ? getWidth() : rangeHighlightEnd;
        canvas.drawRect(left, top, right, bottom, calendarStyle.rangeFill);
      }
    }
  }

  private static boolean skipMonth(
      Long firstOfMonth, Long lastOfMonth, Long startDay, Long endDay) {
    if (firstOfMonth == null || lastOfMonth == null || startDay == null || endDay == null) {
      return true;
    }
    return startDay > lastOfMonth || endDay < firstOfMonth;
  }

  private static int horizontalMidPoint(View view) {
    return view.getLeft() + view.getWidth() / 2;
  }
}
