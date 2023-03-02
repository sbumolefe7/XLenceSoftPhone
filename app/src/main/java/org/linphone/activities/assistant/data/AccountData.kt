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
package org.linphone.activities.assistant.data

import androidx.lifecycle.MutableLiveData
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.core.Account

class AccountData(val account: Account) {
    val selectedForLinking = MutableLiveData<Boolean>()

    val alreadyLinked = MutableLiveData<Boolean>()

    init {
        selectedForLinking.value = false

        val dependentOf = coreContext.core.accountList.find {
            it.dependency == account
        }
        alreadyLinked.value = account.dependency != null || dependentOf != null
    }

    fun toggleSelection() {
        if (alreadyLinked.value == true) return
        selectedForLinking.value = selectedForLinking.value == false
    }
}
