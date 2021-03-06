/*
 * Copyright 2010-2012 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.jaxrs.json;

import java.util.List;

import javax.annotation.Nullable;

import org.joda.time.LocalDate;

import com.ning.billing.analytics.api.TimeSeriesData;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

public class TimeSeriesDataJson {

    private final List<String> dates;
    private final List<Double> values;

    @JsonCreator
    public TimeSeriesDataJson(@JsonProperty("dates") final List<String> dates,
                              @JsonProperty("values") final List<Double> values) {
        this.dates = dates;
        this.values = values;
    }

    public TimeSeriesDataJson(final TimeSeriesData data) {
        this(ImmutableList.<String>copyOf(Collections2.transform(data.getDates(), new Function<LocalDate, String>() {
            @Override
            public String apply(@Nullable final LocalDate input) {
                if (input == null) {
                    return null;
                } else {
                    return input.toString();
                }
            }
        })), data.getValues());
    }

    public List<String> getDates() {
        return dates;
    }

    public List<Double> getValues() {
        return values;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TimeSeriesDataJson");
        sb.append("{dates=").append(dates);
        sb.append(", values=").append(values);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TimeSeriesDataJson that = (TimeSeriesDataJson) o;

        if (dates != null ? !dates.equals(that.dates) : that.dates != null) {
            return false;
        }
        if (values != null ? !values.equals(that.values) : that.values != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = dates != null ? dates.hashCode() : 0;
        result = 31 * result + (values != null ? values.hashCode() : 0);
        return result;
    }
}
