#include <stdio.h>
#include <windows.h>
#include <webauthn.h>
#include <string.h>
#include <wchar.h>
#include <conio.h>
#include "userVerification.h"

#include <stdbool.h>

#include "mock_server.h"


EXPORT VerificationDataPOST initializeUserVerification(
    wchar_t *origin,
    wchar_t *challenge,
    byte *userID,
    long userIDLength,
    const wchar_t *rpId,
    const int timeout
) {
    VerificationDataPOST verificationDataToPost = {
        .authenticationResult = false
    };

    const int originLen = strlen(wcharToChar(origin)) + 1;
    const int challengeLen = strlen(wcharToChar(challenge)) + 1;

    verificationDataToPost.origin = (char *)malloc(originLen);
    memcpy(
        verificationDataToPost.origin,
        wcharToChar(origin),
        originLen
    );
    verificationDataToPost.challenge = (char *)malloc(challengeLen);
    memcpy(
        verificationDataToPost.challenge,
        wcharToChar(challenge),
        challengeLen
    );
    verificationDataToPost.type = "webauthn.get";

    wchar_t *base64_url_encoded_challenge = challenge;

    char* utf8ClientDataJSON = generateClientDataJson(
    origin,
        base64_url_encoded_challenge,
        "webauthn.get"
    );

    WEBAUTHN_CLIENT_DATA client_data = {
        .dwVersion = WEBAUTHN_CLIENT_DATA_CURRENT_VERSION,
        .cbClientDataJSON = (DWORD)(strlen(utf8ClientDataJSON)),
        .pbClientDataJSON = (PBYTE)utf8ClientDataJSON,
        .pwszHashAlgId = WEBAUTHN_HASH_ALGORITHM_SHA_256
    };

    WEBAUTHN_CREDENTIAL credential = {
        .dwVersion = WEBAUTHN_CREDENTIAL_CURRENT_VERSION,
        .cbId = userIDLength,
        .pwszCredentialType = WEBAUTHN_CREDENTIAL_TYPE_PUBLIC_KEY,
        .pbId = userID,
    };

    const WEBAUTHN_CREDENTIALS webauthn_credentials = {
        .cCredentials = 1,
        .pCredentials = &credential
    };

    const WEBAUTHN_AUTHENTICATOR_GET_ASSERTION_OPTIONS assertion_options = {
        .dwVersion = WEBAUTHN_AUTHENTICATOR_GET_ASSERTION_OPTIONS_CURRENT_VERSION,
        .dwTimeoutMilliseconds = timeout,
        .CredentialList = webauthn_credentials,
        .dwAuthenticatorAttachment = WEBAUTHN_AUTHENTICATOR_ATTACHMENT_PLATFORM,
        .dwUserVerificationRequirement =  WEBAUTHN_USER_VERIFICATION_REQUIREMENT_REQUIRED,
        .pbU2fAppId = NULL,
        .dwCredLargeBlobOperation = WEBAUTHN_CRED_LARGE_BLOB_OPERATION_NONE,
        .pbCredLargeBlob = NULL,
        .cbCredLargeBlob = 0
    };

    WEBAUTHN_ASSERTION *pWebauthNAssertion = NULL;

    HWND hWnd = GetForegroundWindow();
    if (hWnd == NULL) {
        fprintf(stderr, "Failed to get console window handle. Continuing with NULL HWND.\n");
    }

    const HRESULT hr = WebAuthNAuthenticatorGetAssertion(
        hWnd,
        rpId,
        &client_data,
        &assertion_options,
        &pWebauthNAssertion
    );

    switch (hr) {
        case S_OK:
            verificationDataToPost.authenticationResult = SUCCESS;
            break;
        case S_FALSE:
            verificationDataToPost.authenticationResult = false;
            break;
        case NTE_NO_KEY:
            verificationDataToPost.authenticationResult = REGISTER_AGAIN;
            break;
        case E_ABORT:
            verificationDataToPost.authenticationResult = ABORT;
            break;
        case E_ACCESSDENIED:
            break;
        case E_FAIL:
            verificationDataToPost.authenticationResult = E_FAILURE;
            break;
        case E_HANDLE:
            break;
        case E_INVALIDARG:
            break;
        case E_OUTOFMEMORY:
            break;
        case E_NOINTERFACE:
            break;
        case E_NOTIMPL:
            break;
        case E_POINTER:
            break;
        case E_UNEXPECTED:
            break;
        case NTE_USER_CANCELLED:
            verificationDataToPost.authenticationResult = USER_CANCELED;
            break;
        case NTE_AUTHENTICATION_IGNORED:
            break;
        case NTE_INVALID_PARAMETER:
            verificationDataToPost.authenticationResult = INVALID_PARAMETER;
            break;
        default:
            break;
    }

    if (verificationDataToPost.authenticationResult == SUCCESS) {
        if (pWebauthNAssertion->pbAuthenticatorData && pWebauthNAssertion->cbAuthenticatorData >0) {
            verificationDataToPost.authenticatorDataBytes = (byte *) malloc(pWebauthNAssertion->cbAuthenticatorData);

            if (verificationDataToPost.authenticatorDataBytes) {
                memcpy(
                    verificationDataToPost.authenticatorDataBytes,
                    pWebauthNAssertion->pbAuthenticatorData,
                    pWebauthNAssertion->cbAuthenticatorData
                );
                verificationDataToPost.authenticatorDataLength = (int) pWebauthNAssertion->cbAuthenticatorData;
            }else {
                verificationDataToPost.authenticatorDataBytes = NULL;
                verificationDataToPost.authenticatorDataLength = 0;
                verificationDataToPost.authenticationResult = MEMORY_ALLOCATION_ERROR;
            }

            if (pWebauthNAssertion->pbSignature && pWebauthNAssertion->cbSignature > 0) {
                verificationDataToPost.signatureBytes = (byte *) malloc(pWebauthNAssertion->cbSignature);
                if (verificationDataToPost.signatureBytes) {
                    memcpy(
                        verificationDataToPost.signatureBytes,
                        pWebauthNAssertion->pbSignature,
                        pWebauthNAssertion->cbSignature
                    );
                    verificationDataToPost.signatureBytesLength = (int) pWebauthNAssertion->cbSignature;
                }else {
                    verificationDataToPost.signatureBytes = NULL;
                    verificationDataToPost.signatureBytesLength = 0;
                    verificationDataToPost.authenticatorDataBytes = NULL;
                    verificationDataToPost.authenticatorDataLength = 0;
                    verificationDataToPost.authenticationResult = MEMORY_ALLOCATION_ERROR;
                }
            }

            if (pWebauthNAssertion->pbUserId && pWebauthNAssertion->cbUserId > 0) {
                verificationDataToPost.userHandle = (byte *) malloc(pWebauthNAssertion->cbUserId);
                if (verificationDataToPost.userHandle) {
                    memcpy(
                        verificationDataToPost.userHandle,
                        pWebauthNAssertion->pbUserId,
                        pWebauthNAssertion->cbUserId
                    );
                    verificationDataToPost.userHandleLength = (int) pWebauthNAssertion->cbUserId;
                } else {
                    verificationDataToPost.userHandle = NULL;
                    verificationDataToPost.userHandleLength = 0;
                    verificationDataToPost.signatureBytes = NULL;
                    verificationDataToPost.signatureBytesLength = 0;
                    verificationDataToPost.authenticatorDataBytes = NULL;
                    verificationDataToPost.authenticatorDataLength = 0;
                    verificationDataToPost.authenticationResult = MEMORY_ALLOCATION_ERROR;
                }
            }
        }

    }else {
        verificationDataToPost.authenticatorDataBytes = NULL;
        verificationDataToPost.authenticatorDataLength = 0;
        verificationDataToPost.signatureBytes = NULL;
        verificationDataToPost.signatureBytesLength = 0;
        verificationDataToPost.userHandle = NULL;
        verificationDataToPost.userHandleLength = 0;
    }

    WebAuthNFreeAssertion(pWebauthNAssertion);
    pWebauthNAssertion = NULL;

    return verificationDataToPost;
}
