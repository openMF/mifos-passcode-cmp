//
// Created by TheKalpeshPawar on 31-05-2025.
//

#ifndef WINDOWSHELLOAUTHENTICATOR_H
#define WINDOWSHELLOAUTHENTICATOR_H
#define EXPORT __declspec(dllexport)

#include <stdbool.h>

#include "mock_server.h"
#include "WindowsHelloAuthenticator.h"

EXPORT bool checkIfAuthenticatorIsAvailable();

EXPORT VerificationDataPOST verifyUser(
    const VerificationDataGET *verificationData
);

EXPORT RegistrationDataPOST registerUser(
    const RegistrationDataGET *registrationData
);

EXPORT void FreeRegistrationDataPOSTContents(const RegistrationDataPOST *data);

EXPORT void FreeVerificationDataPOSTContents(const VerificationDataPOST *data);



#endif //WINDOWSHELLOAUTHENTICATOR_H
