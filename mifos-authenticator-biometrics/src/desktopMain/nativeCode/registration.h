//
// Created by thekalpeshpawar on 31-05-2025.
//

#ifndef REGISTRATION_H
#define REGISTRATION_H
#define EXPORT __declspec(dllexport)

#include "mock_server.h"


EXPORT RegistrationDataPOST initiateUserRegistration(
    wchar_t *origin,
    wchar_t *challenge,
    const int timeout,
    const wchar_t *rpId,
    const wchar_t *rpName,
    char *userID,
    const wchar_t *account_name,
    const wchar_t *display_name
);

#endif //REGISTRATION_H
