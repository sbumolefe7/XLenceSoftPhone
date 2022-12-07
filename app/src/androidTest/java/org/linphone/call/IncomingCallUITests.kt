package org.linphone.call

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.linphone.methods.*
import org.linphone.methods.UITestsScreenshots.takeScreenshot

@RunWith(AndroidJUnit4::class)
class IncomingCallUITests {

    lateinit var methods: CallViewUITestsMethods

    @get:Rule
    val screenshotsRule = ScreenshotsRule(true)

    @get:Rule
    var mGrantPermissionRule = GrantPermissionRule.grant(*LinphonePermissions.CALL)

    @Before
    fun setUp() {
        UITestsUtils.testAppSetup()
        methods = CallViewUITestsMethods()
        takeScreenshot("dialer_view")
        methods.startIncomingCall()
        methods.openIncomingCallViewFromPush()
        takeScreenshot("incoming_call_view")
    }

    @After
    fun tearDown() {
        methods.endCall()
    }

    @Test
    fun testOpenIncomingCallView() {
        methods.endCall()
        takeScreenshot("dialer_view")
    }

    @Test
    fun testNoAnswerIncomingCallView() {
        methods.noAnswerCallFromIncomingCall()
        takeScreenshot("dialer_view")
    }

    @Test
    fun testDeclineIncomingCallView() {
        methods.declineCallFromIncomingCallView()
        takeScreenshot("dialer_view")
    }

    @Test
    fun testAcceptIncomingCallView() {
        methods.answerCallFromIncomingCallView()
        takeScreenshot("single_call_view")
        methods.endCall()
        takeScreenshot("dialer_view")
    }
}