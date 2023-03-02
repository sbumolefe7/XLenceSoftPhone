/*
 * Copyright (c) 2010-2023 Belledonne Communications SARL.
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
package org.linphone.activities.assistant.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.LinphoneApplication.Companion.corePreferences
import org.linphone.activities.assistant.data.AccountData
import org.linphone.core.tools.Log

class AccountLinkingViewModel : ViewModel() {
    val linphoneAccounts = MutableLiveData<List<AccountData>>()

    val thirdPartyAccounts = MutableLiveData<List<AccountData>>()

    init {
        val linphoneAccountsList = arrayListOf<AccountData>()
        val thirdPartyAccountsList = arrayListOf<AccountData>()

        for (account in coreContext.core.accountList) {
            if (account.dependency == account) {
                Log.w("[Account Linking] Account was dependent on itself, fixed that")
                account.dependency = null
            }

            if (account.params.domain == corePreferences.defaultDomain) {
                linphoneAccountsList.add(AccountData(account))
            } else {
                thirdPartyAccountsList.add(AccountData(account))
            }
        }

        linphoneAccounts.value = linphoneAccountsList
        thirdPartyAccounts.value = thirdPartyAccountsList
    }

    fun linkAccounts(): Boolean {
        val linphoneAccountToLink = linphoneAccounts.value.orEmpty().find {
            it.selectedForLinking.value == true
        }
        Log.i("[Account Linking] Linphone account to link is ${linphoneAccountToLink?.account?.params?.identity}")

        val thirdPartyAccountToLink = thirdPartyAccounts.value.orEmpty().find {
            it.selectedForLinking.value == true
        }
        Log.i("[Account Linking] Third party account to link is ${thirdPartyAccountToLink?.account?.params?.identity}")

        if (linphoneAccountToLink != null && thirdPartyAccountToLink != null) {
            thirdPartyAccountToLink.account.dependency = linphoneAccountToLink.account
            Log.i("[Account Linking] Done")
            return true
        }

        Log.w("[Account Linking] Can't link, missing selected account")
        return false
    }
}
