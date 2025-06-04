//
// Created by thekalpeshpawar on 31-05-2025.
//

#ifndef USERVERIFICATION_H
#define USERVERIFICATION_H
#define EXPORT __declspec(dllexport)


#include "mock_server.h"

EXPORT VerificationDataPOST initializeUserVerification(
    wchar_t *origin,
    wchar_t *challenge,
    byte *userID,
    long userIDLength,
    const wchar_t *rpId,
    const int timeout
);


#endif //USERVERIFICATION_H
