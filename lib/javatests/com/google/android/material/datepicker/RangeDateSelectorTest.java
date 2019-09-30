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
package com.google.android.material.datepicker;

import com.google.android.material.R;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import android.content.Context;
import androidx.core.util.Pair;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView.BufferType;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.internal.ParcelableTestUtils;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Calendar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class RangeDateSelectorTest {

  private RangeDateSelector rangeDateSelector;
  private MonthAdapter adapter;
  private Context context;

  @Before
  public void setupMonthAdapters() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    context = activity.getApplicationContext();
    GridView gridView = new GridView(context);
    rangeDateSelector = new RangeDateSelector();
    adapter =
        new MonthAdapter(
            Month.create(2016, Calendar.FEBRUARY),
            rangeDateSelector,
            new CalendarConstraints.Builder().build());
    gridView.setAdapter(adapter);
  }

  @Test
  public void textInputValid() {
    View root =
        rangeDateSelector.onCreateTextInputView(
            LayoutInflater.from(context),
            null,
            null,
            new CalendarConstraints.Builder().build(),
            new OnSelectionChangedListener<Pair<Long, Long>>() {
              @Override
              public void onSelectionChanged(Pair<Long, Long> selection) {}
            });
    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);

    startTextInput.getEditText().setText("2/2/2010", BufferType.EDITABLE);
    endTextInput.getEditText().setText("2/2/2012", BufferType.EDITABLE);

    assertThat(startTextInput.getError(), nullValue());
    assertThat(endTextInput.getError(), nullValue());
  }

  @Test
  public void textInputFormatError() {
    View root =
        rangeDateSelector.onCreateTextInputView(
            LayoutInflater.from(context),
            null,
            null,
            new CalendarConstraints.Builder().build(),
            new OnSelectionChangedListener<Pair<Long, Long>>() {
              @Override
              public void onSelectionChanged(Pair<Long, Long> selection) {}
            });
    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);

    startTextInput.getEditText().setText("22/22/2010", BufferType.EDITABLE);
    endTextInput.getEditText().setText("555-555-5555", BufferType.EDITABLE);

    assertThat(startTextInput.getError(), notNullValue());
    assertThat(endTextInput.getError(), notNullValue());
  }

  @Test
  public void textInputRangeError() {
    rangeDateSelector = new RangeDateSelector();
    View root =
        rangeDateSelector.onCreateTextInputView(
            LayoutInflater.from(context),
            null,
            null,
            new CalendarConstraints.Builder().build(),
            new OnSelectionChangedListener<Pair<Long, Long>>() {
              @Override
              public void onSelectionChanged(Pair<Long, Long> selection) {}
            });
    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);

    startTextInput.getEditText().setText("2/2/2010", BufferType.EDITABLE);
    endTextInput.getEditText().setText("2/2/2008", BufferType.EDITABLE);

    assertThat(
        startTextInput.getError(),
        is((CharSequence) context.getString(R.string.mtrl_picker_invalid_range)));
    assertThat(endTextInput.getError(), notNullValue());
  }

  @Test
  public void rangeDateSelectorMaintainsStateAfterParceling() {
    int startPosition = 8;
    int endPosition = 15;
    long expectedStart = adapter.getItem(startPosition);
    long expectedEnd = adapter.getItem(endPosition);

    rangeDateSelector.select(adapter.getItem(startPosition));
    rangeDateSelector.select(adapter.getItem(endPosition));
    RangeDateSelector rangeDateSelectorFromParcel =
        ParcelableTestUtils.parcelAndCreate(rangeDateSelector, RangeDateSelector.CREATOR);

    assertThat(adapter.withinMonth(startPosition), is(true));
    assertThat(adapter.withinMonth(endPosition), is(true));
    assertThat(rangeDateSelectorFromParcel.getSelection().first, is(expectedStart));
    assertThat(rangeDateSelectorFromParcel.getSelection().second, is(expectedEnd));
  }

  @Test
  public void nullDateSelectionFromParcel() {
    RangeDateSelector rangeDateSelectorFromParcel =
        ParcelableTestUtils.parcelAndCreate(rangeDateSelector, RangeDateSelector.CREATOR);
    assertThat(rangeDateSelectorFromParcel.getSelection().first, nullValue());
    assertThat(rangeDateSelectorFromParcel.getSelection().second, nullValue());
  }

  @Test
  public void setSelectionDirectly() {
    Calendar setToStart = UtcDates.getCalendar();
    setToStart.set(2004, Calendar.MARCH, 5);
    Calendar setToEnd = UtcDates.getCalendar();
    setToEnd.set(2005, Calendar.FEBRUARY, 1);

    rangeDateSelector.setSelection(
        new Pair<>(setToStart.getTimeInMillis(), setToEnd.getTimeInMillis()));
    Calendar resultCalendarStart = UtcDates.getCalendar();
    Calendar resultCalendarEnd = UtcDates.getCalendar();
    resultCalendarStart.setTimeInMillis(rangeDateSelector.getSelection().first);
    resultCalendarEnd.setTimeInMillis(rangeDateSelector.getSelection().second);

    assertThat(resultCalendarStart.get(Calendar.DAY_OF_MONTH), is(5));
    assertThat(resultCalendarEnd.get(Calendar.DAY_OF_MONTH), is(1));
    assertThat(
        rangeDateSelector
            .getSelectedDays()
            .contains(UtcDates.canonicalYearMonthDay(setToStart.getTimeInMillis())),
        is(true));
    assertThat(
        rangeDateSelector
            .getSelectedDays()
            .contains(UtcDates.canonicalYearMonthDay(setToStart.getTimeInMillis())),
        is(true));
    assertThat(
        rangeDateSelector
            .getSelectedDays()
            .contains(
                UtcDates.canonicalYearMonthDay(UtcDates.getTodayCalendar().getTimeInMillis())),
        is(false));
  }

  @Test
  public void invalidSetThrowsException() {
    Calendar setToStart = UtcDates.getCalendar();
    setToStart.set(2005, Calendar.FEBRUARY, 1);
    Calendar setToEnd = UtcDates.getCalendar();
    ;
    setToEnd.set(2004, Calendar.MARCH, 5);

    // ThrowingRunnable used by assertThrows is not available until gradle 4.13
    try {
      rangeDateSelector.setSelection(
          new Pair<>(setToStart.getTimeInMillis(), setToEnd.getTimeInMillis()));
    } catch (IllegalArgumentException e) {
      return;
    }
    Assert.fail();
  }
}
