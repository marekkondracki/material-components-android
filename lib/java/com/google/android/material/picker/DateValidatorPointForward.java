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

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.material.picker.CalendarConstraints.DateValidator;
import java.util.Arrays;
import java.util.Calendar;

/**
 * A {@link CalendarConstraints.DateValidator} that only allows dates from a given point onward to
 * be clicked.
 */
public class DateValidatorPointForward implements DateValidator {

  private final long point;

  public DateValidatorPointForward() {
    point = Calendar.getInstance().getTimeInMillis();
  }

  public DateValidatorPointForward(long point) {
    this.point = point;
  }

  public static final Parcelable.Creator<DateValidatorPointForward> CREATOR =
      new Parcelable.Creator<DateValidatorPointForward>() {
        @Override
        public DateValidatorPointForward createFromParcel(Parcel source) {
          return new DateValidatorPointForward(source.readLong());
        }

        @Override
        public DateValidatorPointForward[] newArray(int size) {
          return new DateValidatorPointForward[size];
        }
      };

  @Override
  public boolean isValid(long date) {
    return date >= point;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(point);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DateValidatorPointForward)) {
      return false;
    }
    DateValidatorPointForward that = (DateValidatorPointForward) o;
    return point == that.point;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {point};
    return Arrays.hashCode(hashedFields);
  }
}
