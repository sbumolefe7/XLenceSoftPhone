/*
 * Copyright (c) 2010-2021 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.activities.main.conference.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import java.util.*
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.R
import org.linphone.activities.main.conference.data.ConferenceSchedulingParticipantData
import org.linphone.activities.main.conference.data.Duration
import org.linphone.activities.main.conference.data.TimeZoneData
import org.linphone.contact.ContactsSelectionViewModel
import org.linphone.core.*
import org.linphone.core.tools.Log
import org.linphone.utils.Event
import org.linphone.utils.TimestampUtils

class ConferenceSchedulingViewModel : ContactsSelectionViewModel() {
    val subject = MutableLiveData<String>()
    val description = MutableLiveData<String>()

    val scheduleForLater = MutableLiveData<Boolean>()

    val formattedDate = MutableLiveData<String>()
    val formattedTime = MutableLiveData<String>()

    val isEncrypted = MutableLiveData<Boolean>()

    val sendInviteViaChat = MutableLiveData<Boolean>()
    val sendInviteViaEmail = MutableLiveData<Boolean>()

    val participantsData = MutableLiveData<List<ConferenceSchedulingParticipantData>>()

    val address = MutableLiveData<Address>()

    val conferenceCreationInProgress = MutableLiveData<Boolean>()

    val conferenceCreationCompletedEvent: MutableLiveData<Event<Boolean>> by lazy {
        MutableLiveData<Event<Boolean>>()
    }

    val continueEnabled: MediatorLiveData<Boolean> = MediatorLiveData()

    var timeZone = MutableLiveData<TimeZoneData>()
    val timeZones: List<TimeZoneData> = computeTimeZonesList()

    var duration = MutableLiveData<Duration>()
    val durationList: List<Duration> = computeDurationList()

    private var date: Long = 0
    private var hour: Int = 0
    private var minutes: Int = 0

    private val listener = object : CoreListenerStub() {
        override fun onConferenceInfoOnSent(core: Core, conferenceInfo: ConferenceInfo) {
            Log.i("[Conference Creation] Conference information successfully sent to all participants")
            conferenceCreationInProgress.value = false
            conferenceCreationCompletedEvent.value = Event(true)
        }

        override fun onConferenceInfoOnParticipantError(
            core: Core,
            conferenceInfo: ConferenceInfo,
            participant: Address,
            error: ConferenceInfoError?
        ) {
            Log.e("[Conference Creation] Conference information wasn't sent to participant ${participant.asStringUriOnly()}")
            onErrorEvent.value = Event(R.string.conference_schedule_info_not_sent_to_participant)
            conferenceCreationInProgress.value = false
        }

        override fun onConferenceStateChanged(
            core: Core,
            conference: Conference,
            state: Conference.State?
        ) {
            Log.i("[Conference Creation] Conference state changed: $state")
            if (state == Conference.State.CreationPending) {
                Log.i("[Conference Creation] Conference address will be ${conference.conferenceAddress.asStringUriOnly()}")
                address.value = conference.conferenceAddress

                if (scheduleForLater.value == true) {
                    sendConferenceInfo()
                } else {
                    conferenceCreationInProgress.value = false
                    conferenceCreationCompletedEvent.value = Event(true)
                }
            }
        }
    }

    init {
        sipContactsSelected.value = true

        subject.value = ""
        scheduleForLater.value = false
        isEncrypted.value = false
        sendInviteViaChat.value = true
        sendInviteViaEmail.value = false

        timeZone.value = timeZones.find {
            it.id == TimeZone.getDefault().id
        }
        duration.value = durationList.find {
            it.value == 60
        }

        continueEnabled.value = false
        continueEnabled.addSource(subject) {
            continueEnabled.value = allMandatoryFieldsFilled()
        }
        continueEnabled.addSource(scheduleForLater) {
            continueEnabled.value = allMandatoryFieldsFilled()
        }
        continueEnabled.addSource(formattedDate) {
            continueEnabled.value = allMandatoryFieldsFilled()
        }
        continueEnabled.addSource(formattedTime) {
            continueEnabled.value = allMandatoryFieldsFilled()
        }

        coreContext.core.addListener(listener)
    }

    override fun onCleared() {
        coreContext.core.removeListener(listener)
        participantsData.value.orEmpty().forEach(ConferenceSchedulingParticipantData::destroy)

        super.onCleared()
    }

    fun toggleSchedule() {
        scheduleForLater.value = scheduleForLater.value == false
    }

    fun setDate(d: Long) {
        date = d
        formattedDate.value = TimestampUtils.dateToString(date)
    }

    fun setTime(h: Int, m: Int) {
        hour = h
        minutes = m
        formattedTime.value = TimestampUtils.timeToString(hour, minutes)
    }

    fun updateEncryption(enable: Boolean) {
        isEncrypted.value = enable
    }

    fun computeParticipantsData() {
        participantsData.value.orEmpty().forEach(ConferenceSchedulingParticipantData::destroy)
        val list = arrayListOf<ConferenceSchedulingParticipantData>()

        for (address in selectedAddresses.value.orEmpty()) {
            val data = ConferenceSchedulingParticipantData(address, isEncrypted.value == true)
            list.add(data)
        }

        participantsData.value = list
    }

    fun createConference() {
        val participantsCount = selectedAddresses.value.orEmpty().size
        if (participantsCount == 0) {
            Log.e("[Conference Creation] Couldn't create conference without any participant!")
            return
        }

        conferenceCreationInProgress.value = true
        val params = coreContext.core.createConferenceParams()
        params.isVideoEnabled = true // TODO: Keep this to true ?
        // TODO: params.setSubject(subject.value)
        val startTime = getConferenceStartTimestamp()
        // TODO: params.setStartTime(startTime)
        val duration = duration.value?.value ?: 0
        // TODO: if (duration != 0) params.setEndTime(startTime + duration)

        val participants = arrayOfNulls<Address>(selectedAddresses.value.orEmpty().size)
        selectedAddresses.value?.toArray(participants)

        val localAddress = coreContext.core.defaultAccount?.params?.identityAddress
        // TODO: coreContext.core.createConferenceOnServer(params, localAddress, participants)
    }

    private fun computeTimeZonesList(): List<TimeZoneData> {
        return TimeZone.getAvailableIDs().map { id -> TimeZoneData(TimeZone.getTimeZone(id)) }.toList().sorted()
    }

    private fun computeDurationList(): List<Duration> {
        return arrayListOf(Duration(30, "30min"), Duration(60, "1h"), Duration(120, "2h"))
    }

    private fun allMandatoryFieldsFilled(): Boolean {
        return !subject.value.isNullOrEmpty() &&
            (
                scheduleForLater.value == false ||
                    (
                        !formattedDate.value.isNullOrEmpty() &&
                            !formattedTime.value.isNullOrEmpty()
                        )
                )
    }

    private fun sendConferenceInfo() {
        val participants = arrayOfNulls<Address>(selectedAddresses.value.orEmpty().size)
        selectedAddresses.value?.toArray(participants)

        val conferenceInfo = Factory.instance().createConferenceInfo()
        conferenceInfo.uri = Factory.instance().createAddress("sip:video-conference-0@sip.linphone.org") // TODO: use address.value
        conferenceInfo.setParticipants(participants)
        conferenceInfo.organizer = coreContext.core.defaultAccount?.params?.identityAddress
        conferenceInfo.subject = subject.value
        conferenceInfo.description = description.value
        conferenceInfo.duration = duration.value?.value ?: 0
        val timestamp = getConferenceStartTimestamp()
        conferenceInfo.dateTime = timestamp

        Log.i("[Conference Creation] Conference date & time set to ${TimestampUtils.dateToString(timestamp)} ${TimestampUtils.timeToString(timestamp)}, duration = ${conferenceInfo.duration}")
        coreContext.core.sendConferenceInformation(conferenceInfo, "")

        conferenceCreationInProgress.value = false
        conferenceCreationCompletedEvent.value = Event(true)
    }

    private fun getConferenceStartTimestamp(): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone.value?.id ?: TimeZone.getDefault().id))
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minutes)
        return calendar.timeInMillis / 1000 // Linphone expects a time_t (so in seconds)
    }
}
