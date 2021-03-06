package com.rafaelkarlo.workmode.mainscreen.presenter;

import com.rafaelkarlo.workmode.mainscreen.service.WorkModeAudioOverrideService;
import com.rafaelkarlo.workmode.mainscreen.service.WorkModeService;
import com.rafaelkarlo.workmode.mainscreen.service.alarm.WorkModeAlarm;
import com.rafaelkarlo.workmode.mainscreen.service.audio.AudioMode;
import com.rafaelkarlo.workmode.mainscreen.service.time.WorkDay;
import com.rafaelkarlo.workmode.mainscreen.view.MainView;

import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static com.rafaelkarlo.workmode.mainscreen.service.time.WorkDay.MONDAY;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MainPresenterTest {

    @Mock
    private MainView mainView;

    @Mock
    private WorkModeService workModeService;

    @Mock
    private WorkModeAudioOverrideService workModeAudioOverrideService;

    @Mock
    private WorkModeAlarm workModeAlarm;

    private MainPresenter mainPresenter;

    @Before
    public void setupPresenter() {
        mainPresenter = new MainPresenterImpl(workModeService, workModeAlarm, workModeAudioOverrideService);
        mainPresenter.attachView(mainView);
    }

    @Test
    public void shouldUpdateStatusWhenWorkModeHasBeenActivated() {
        when(workModeService.getStartTime()).thenReturn(new LocalTime());
        when(workModeService.getEndTime()).thenReturn(new LocalTime());
        when(workModeService.getWorkDays()).thenReturn(new HashSet<>(singletonList(MONDAY)));

        mainPresenter.activateWorkMode();

        verify(workModeService).activate();
        verify(mainView).onWorkModeActivation();
        verify(mainView).displayActivationSuccessful();
    }

    @Test
    public void shouldUpdateStatusWhenWorkModeHasBeenDeactivated() {
        mainPresenter.deactivateWorkMode();

        verify(workModeService).deactivate();
        verify(mainView).onWorkModeDeactivation();
    }

    @Test
    public void shouldSetViewToActivatedStatusWhenStartingApp() {
        when(workModeService.isActivated()).thenReturn(true);

        mainPresenter.onCreate();

        verify(mainView).onWorkModeActivation();
        verify(mainView).onSetStartDate(anyString());
        verify(mainView).onSetStartDate(anyString());
        verify(mainView).onSetWorkDays(anyString());
    }

    @Test
    public void shouldSetViewToDeactivatedStatusWhenStartingApp() {
        when(workModeService.isActivated()).thenReturn(false);

        mainPresenter.onCreate();

        verify(mainView).onWorkModeDeactivation();
        verify(mainView).onSetStartDate(anyString());
        verify(mainView).onSetStartDate(anyString());
    }

    @Test
    public void shouldNotBeAllowedToEnableWhenEndTimeIsMissing() {
        when(workModeService.getEndTime()).thenReturn(null);
        LocalTime someLocalTime = new LocalTime();
        when(workModeService.getStartTime()).thenReturn(someLocalTime);

        mainPresenter.activateWorkMode();

        verify(mainView, never()).onWorkModeActivation();
        verify(mainView).displayErrorOnMissingWorkHours();
    }

    @Test
    public void shouldNotBeAllowedToEnableWhenStartTimeIsMissing() {
        when(workModeService.getStartTime()).thenReturn(null);
        LocalTime someLocalTime = new LocalTime();
        when(workModeService.getEndTime()).thenReturn(someLocalTime);

        mainPresenter.activateWorkMode();

        verify(mainView, never()).onWorkModeActivation();
        verify(mainView).displayErrorOnMissingWorkHours();
    }

    @Test
    public void shouldNotBeAllowedToEnableWhenWorkDaysSetIsEmpty() {
        LocalTime someLocalTime = new LocalTime();
        LocalTime someOtherLocalTime = new LocalTime().plusSeconds(1);
        when(workModeService.getStartTime()).thenReturn(someLocalTime);
        when(workModeService.getEndTime()).thenReturn(someOtherLocalTime);

        when(workModeService.getWorkDays()).thenReturn(Collections.<WorkDay>emptySet());

        mainPresenter.activateWorkMode();

        verify(mainView, never()).onWorkModeActivation();
        verify(mainView).displayErrorOnMissingWorkDays();
    }

    @Test
    public void shouldBeAllowedToActivateWhenStartTimeIsLaterThanEndTime() {
        LocalTime afternoonTime = new LocalTime(17, 0);
        LocalTime morningTime = new LocalTime(9, 0);
        when(workModeService.getStartTime()).thenReturn(afternoonTime);
        when(workModeService.getEndTime()).thenReturn(morningTime);
        when(workModeService.getWorkDays()).thenReturn(new HashSet<>(singletonList(MONDAY)));

        mainPresenter.activateWorkMode();

        verify(mainView).onWorkModeActivation();
        verify(mainView).displayActivationSuccessful();
    }

    @Test
    public void shouldNotBeAllowedToActivateWhenStartTimeIsEqualToEndTime() {
        LocalTime afternoonTime = new LocalTime(17, 0);
        LocalTime morningTime = new LocalTime(17, 0);
        when(workModeService.getStartTime()).thenReturn(afternoonTime);
        when(workModeService.getEndTime()).thenReturn(morningTime);

        mainPresenter.activateWorkMode();

        verify(mainView, never()).onWorkModeActivation();
        verify(mainView).displayErrorOnInvalidWorkHours();
    }

    @Test
    public void shouldDeactivateWhenNewStartTimeHasBeenSet() {
        mainPresenter.setStartDate(10, 30);

        verify(mainView).onWorkModeDeactivation();
    }

    @Test
    public void shouldDeactivateWhenNewEndTimeHasBeenSet() {
        mainPresenter.setEndDate(10, 30);

        verify(mainView).onWorkModeDeactivation();
    }

    @Test
    public void shouldDeactivateWhenNewWorkDaysHaveBeenSet() {
        Set<WorkDay> dontCareWorkDays = null;
        mainPresenter.setWorkDays(dontCareWorkDays);

        verify(mainView).onWorkModeDeactivation();
    }

    @Test
    public void shouldUpdateWorkDaysDisplayAfterSettingWorkdays() {
        HashSet<WorkDay> workDays = new HashSet<>(singletonList(MONDAY));
        mainPresenter.setWorkDays(workDays);

        verify(workModeService).setWorkDays(workDays);
        verify(mainView).onSetWorkDays(anyString());
    }

    @Test
    public void shouldBeAbleToGetSavedDays() {
        HashSet<WorkDay> savedWorkDays = new HashSet<>(singletonList(MONDAY));
        when(workModeService.getWorkDays()).thenReturn(savedWorkDays);

        assertThat(mainPresenter.getSavedDays()).isEqualTo(savedWorkDays);
    }

    @Test
    public void shouldStartAlarmWhenActivating() {
        LocalTime validStartTime = new LocalTime(9, 0);
        LocalTime validEndTime = new LocalTime(17, 0);
        when(workModeService.getWorkDays()).thenReturn(new HashSet<>(singletonList(MONDAY)));
        when(workModeService.getStartTime()).thenReturn(validStartTime);
        when(workModeService.getEndTime()).thenReturn(validEndTime);

        mainPresenter.activateWorkMode();

        verify(workModeAlarm).startAlarm(any(LocalTime.class), any(LocalTime.class));
    }

    @Test
    public void shouldCancelCurrentAlarmWhenDeactivating() {
        mainPresenter.deactivateWorkMode();

        verify(workModeAlarm).cancelAlarm();
    }

    @Test
    public void shouldReturnToPreviousRingerModeWhenDeactivating() {
        mainPresenter.deactivateWorkMode();

        verify(workModeService).setToPreviousMode();
    }

    @Test
    public void shouldOverrideAudioModeWhenSet() {
        mainPresenter.setCurrentAudioMode(AudioMode.NORMAL);

        verify(workModeAudioOverrideService).overrideCurrentAudioMode(AudioMode.NORMAL);
        verify(mainView).displayAudioOverrideSuccessMessage("Normal");
    }

}