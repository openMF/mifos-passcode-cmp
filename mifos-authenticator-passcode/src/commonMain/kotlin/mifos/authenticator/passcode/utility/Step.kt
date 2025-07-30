package mifos.authenticator.passcode.utility

enum class Step(var index: Int) {
    Create(0),
    Confirm(1),
    Enter(3)
}
