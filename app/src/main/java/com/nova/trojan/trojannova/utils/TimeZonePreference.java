package com.nova.trojan.trojannova.utils;


import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;

import org.jsoup.helper.StringUtil;

import java.util.Calendar;
import java.util.TimeZone;


public class TimeZonePreference extends ListPreference {

    public TimeZonePreference(Context context) {
        super(context);
        this.init();
    }

    public TimeZonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    private void init() {
        this.setEntries(TimeZone.getAvailableIDs());
        this.setEntryValues(this.getEntries());
    }

    // endregion

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String value= a.getString(index);
        return StringUtil.isBlank(value) ? Calendar.getInstance().getTimeZone().getID() : value;
    }

    @Override
    public CharSequence getSummary() {
        String value= this.getValue();
        return StringUtil.isBlank(value) ?"None" : value;
    }
}