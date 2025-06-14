
#ifndef MOCK_SERVER_H
#define MOCK_SERVER_H
#define EXPORT __declspec(dllexport)

#include "utils.h"

EXPORT typedef struct {
    byte *attestationObjectBytes;
    int attestationObjectLength;
    byte *credentialIdBytes;
    int credentialIdLength;
    char *origin;
    char *type;
    char *challenge;
    long authenticationResult;
} RegistrationDataPOST;


EXPORT typedef struct {
    char *origin;
    char *challenge;
    int timeout;
    char *rpId;
    char *rpName;
    char *userID;
    char *accountName;
    char *displayName;
} RegistrationDataGET;

EXPORT typedef struct {
    byte *authenticatorDataBytes;
    int authenticatorDataLength;
    byte *signatureBytes;
    int signatureBytesLength;
    byte *userHandle;
    int userHandleLength;
    char *origin;
    char *challenge;
    char *type;
    long authenticationResult;
} VerificationDataPOST;

EXPORT typedef struct {
    char *origin;
    byte *userID;
    long userIDLength;
    char *challenge;
    const char *rpId;
    const int timeout;
} VerificationDataGET;

EXPORT wchar_t* generateBase64UrlEncodedUID();

EXPORT wchar_t* generateBase64UrlEncodedChallenge();

EXPORT char* generateClientDataJson(
    wchar_t *rp_id,
    wchar_t *base64UrlEncodedChallenge,
    const char *type
);

enum AUTHENTICATOR_RESPONSE {
    SUCCESS = 1,
    MEMORY_ALLOCATION_ERROR= 99999999,
    E_FAILURE = 80004005,
    REGISTER_AGAIN = 800900013,
    ABORT=80004004,
    USER_CANCELED = 80090036,
    UNKNOWN_ERROR = 800015151515,
    INVALID_PARAMETER = 80090027,
};

#endif //MOCK_SERVER_H
