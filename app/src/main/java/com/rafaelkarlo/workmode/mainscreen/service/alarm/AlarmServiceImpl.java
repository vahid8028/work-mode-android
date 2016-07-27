package com.rafaelkarlo.workmode.mainscreen.service.alarm;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.joda.time.LocalDateTime;

import java.util.Calendar;

import static android.app.AlarmManager.INTERVAL_DAY;
import static android.app.AlarmManager.RTC_WAKEUP;
import static com.rafaelkarlo.workmode.mainscreen.service.alarm.WorkModeAlarmUtils.createIntentWithIdentifierAndTime;
import static com.rafaelkarlo.workmode.mainscreen.service.alarm.WorkModeAlarmUtils.createPendingIntentWithIntent;

public class AlarmServiceImpl implements AlarmService {

    private final Context context;
    private final AlarmManager alarmManager;

    public AlarmServiceImpl(Context context, AlarmManager alarmManager) {
        this.context = context;
        this.alarmManager = alarmManager;
    }

    @Override
    public void setRepeatingAlarmForDateTimeWithIdentifier(LocalDateTime triggerDateTime, String identifier) {
        setDailyAlarmForAnAction(triggerDateTime, identifier);
    }

    @Override
    public void cancelAlarmWithIdentifier(String identifier) {
        alarmManager.cancel(createPendingIntentWithIntent(context, createIntentWithIdentifierAndTime(context, identifier, 0)));
    }

    private void setDailyAlarmForAnAction(LocalDateTime triggerDateTime, String identifier) {
        long timeInMillis = getTimeInMillisFromLocalTime(triggerDateTime);
        Intent workStartIntent = createIntentWithIdentifierAndTime(context, identifier, timeInMillis);

        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= Build.VERSION_CODES.KITKAT) {
            setAlarmForApi19AndAbove(timeInMillis, workStartIntent);
        } else {
            setAlarmForApiBelow19(timeInMillis, workStartIntent);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setAlarmForApi19AndAbove(long triggerTimeInMillis, Intent workStartIntent) {
        alarmManager.setExact(
                RTC_WAKEUP,
                triggerTimeInMillis,
                createPendingIntentWithIntent(context, workStartIntent)
        );
    }

    private void setAlarmForApiBelow19(long triggerTimeInMillis, Intent workStartIntent) {
        alarmManager.setRepeating(
                RTC_WAKEUP,
                triggerTimeInMillis,
                INTERVAL_DAY,
                createPendingIntentWithIntent(context, workStartIntent)
        );
    }

    private long getTimeInMillisFromLocalTime(LocalDateTime triggerTime) {
        Calendar calendarSilentInstance = Calendar.getInstance();
        calendarSilentInstance.setTime(triggerTime.toDate());
        return calendarSilentInstance.getTimeInMillis();
    }
}