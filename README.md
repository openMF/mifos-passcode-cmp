# Mifos-Passcode-Cmp
Library for passcode implementation along with an optional additional feature to ask for a passcode when your app resumes from the background. (Works with minSDK >= 21)

# Project Structure :

- **`androidApp` Module**:
  - Contains the Android-specific application code.
  - Depends on the `shared` module to utilize common code across platforms.

- **`iosApp` Module**:
  - Holds the iOS-specific application code.
  - Integrates the `shared` module, typically as a framework, to access shared logic.

- **`shared` Module**:
  - The core module contains platform-agnostic code, including business logic and Compose Multiplatform UI components.
  - Referenced by both `androidApp` and `iosApp` modules to promote code reuse.

- **`cmp-mifos-passcode` Module**:
  - A specialized module designed to package and publish the `shared` module as a Compose Multiplatform (CMP) library.
  - Facilitates the distribution and reuse of the shared codebase across different projects or teams.


Usage
-----

In order to use the library

**1. Gradle dependency**

  -  Add the following to your project level `build.gradle`:

```gradle
allprojects {
	repositories {
		jcenter()
	}
}
```
  -  Add this to your app `build.gradle`:

```gradle
dependencies {
	implementation 'com.github.openMF.mifos-passcode:compose:1.0.3'
}
```

## Example

## Android Implementation:

https://github.com/user-attachments/assets/b4d2719e-a198-44fa-9f8f-dc184d80193c



For a basic implementation of the PassCode Screen
- Inject the `PasscodeRepository` in your activity, which is essentially abstracting the operations related to saving, retrieving, and validating the passcode
- Import `PasscodeScreen` to your project, which has 4 parameters mentioned below:
  - `onForgotButton`: This will allow us to handle the case when the user isn't able to log into the app. In our project, we are redirecting the user to the login page
  - `onSkipButton`: This offers users the flexibility to bypass the passcode setup process, granting them immediate access to the desired screen
  - `onPasscodeConfirm`: This allows you to pass a function that accepts a string parameter
  - `onPasscodeRejected`: This can be used to handle the event when the user has entered the wrong passcode

- This is how a typical implementation would look.

```kotlin
    @Inject
    lateinit var passcodeRepository: PasscodeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MifosPasscodeTheme {
                PasscodeScreen(
                    onForgotButton = {},
                    onSkipButton = {},
                    onPasscodeConfirm = {},
                    onPasscodeRejected = {}
                )
            }
        }
    }
```
- You can now define functions of your own and pass them to their respective fields. You can find the entire implementation in the `PasscodeActivity` of `:app` module

## Screenshots
- Here are some screenshots of the app
<table>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/e63c1b1f-6969-48f8-9752-d7e81b18c533" width=250 height=510></td>
    <td><img src="https://github.com/user-attachments/assets/bee50537-44ae-4992-aefa-46032c84a426" width=250 height=510></td>
    <td><img src="https://github.com/user-attachments/assets/4c1b5c80-cd39-4550-b2e8-ebed0d73fa01" width=250 height=510></td>
    <td><img src="https://github.com/user-attachments/assets/39896810-a870-4dbb-9613-b1ec929cc0a8" width=250 height=510></td>
    <td><img src="https://github.com/user-attachments/assets/3435cb86-94bf-4fc9-8d91-c02ac019dd5c" width=250 height=510></td>
  </tr>
</table>
