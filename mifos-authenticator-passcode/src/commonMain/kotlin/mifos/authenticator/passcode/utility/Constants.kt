package mifos.authenticator.passcode.utility

object Constants {
    const val STEPS_COUNT = 2
    const val VIBRATE_FEEDBACK_DURATION = 300L
}

enum class PasscodeLength(val length: Int){
    FOUR_DIGIT(4),
    SIX_DIGIT(6)
}