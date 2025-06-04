//
// Created by thekalpeshpawar on 31-05-2025.
//


#include "userVerification.h"
#include "registration.h"
#include "WindowsHelloAuthenticator.h"
#include "mock_server.h"
#include "utils.h"
#include <stdio.h>
#include <webauthn.h>


EXPORT bool checkIfAuthenticatorIsAvailable() {
    BOOL isAvailable = FALSE;
    HRESULT hrCheck = WebAuthNIsUserVerifyingPlatformAuthenticatorAvailable(&isAvailable);
    if (!isAvailable && SUCCEEDED(hrCheck)) {
        printf("Windows Hello is not available on this system or not configured.\n");
        return false;
    }
    return true;
}

EXPORT VerificationDataPOST verifyUser(
    const VerificationDataGET *verificationData
) {
    printf("Windows Hello Authenticator verify user\n");
    printf("Data received by C program: ");
    printf(verificationData->challenge);printf("\n");
    printf(verificationData->origin);printf("\n");
    printf(verificationData->rpId);printf("\n");
    printf("Receiver credential Id: \n");
    for (DWORD i = 0; i< verificationData->userIDLength; i++ ) {
        printf("%02X" , verificationData->userID[i]);
    }
    printf("\n");
    return initializeUserVerification(
        charToWchar(verificationData->origin),
        charToWchar(verificationData->challenge),
        verificationData->userID,
        verificationData->userIDLength,
        charToWchar(verificationData->rpId),
        verificationData->timeout
    );
}


EXPORT RegistrationDataPOST registerUser(
    const RegistrationDataGET *registrationData
) {

    return initiateUserRegistration(
        charToWchar(registrationData->origin),
        charToWchar(registrationData->challenge),
        registrationData->timeout,
        charToWchar(registrationData->rpId),
        charToWchar(registrationData->rpName),
        registrationData->userID,
        charToWchar(registrationData->accountName),
        charToWchar(registrationData->displayName)
    );

}

EXPORT void FreeRegistrationDataPOSTContents(const RegistrationDataPOST *data) {
    if (data) {
        if (data->attestationObjectBytes) free(data->attestationObjectBytes);
        if (data->origin) free(data->origin);
        if (data->challenge) free(data->challenge);
    }
}

EXPORT void FreeVerificationDataPOSTContents(const VerificationDataPOST *data) {
    if (data) {
        if (data->authenticatorDataBytes) free(data->authenticatorDataBytes);
        if (data->signatureBytes) free(data->signatureBytes);
        if (data->userHandle) free(data->userHandle);
        if (data->challenge) free(data->challenge);
        if (data->origin) free(data->origin);
    }
}