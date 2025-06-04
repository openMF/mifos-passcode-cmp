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

    printf("%lx\n", hr);

    switch (hr) {
        case S_OK:
            printf("Successfully verified credential\n");
            verificationDataToPost.authenticationResult = SUCCESS;
            break;
        case S_FALSE:
            printf("Failed registration\n");
            verificationDataToPost.authenticationResult = false;
            break;
        case E_ABORT:
            printf("Aborted operation\n");
            verificationDataToPost.authenticationResult = ABORT;
            break;
        case E_ACCESSDENIED:
            printf("Access denied\n");
            break;
        case E_FAIL:
            printf("Failure\n");
            verificationDataToPost.authenticationResult = E_FAILURE;
            break;
        case E_HANDLE:
            printf("Invalid handle\n");
            break;
        case E_INVALIDARG:
            printf("One or more arguments are not valid (E_INVALIDARG)\n");
            break;
        case E_OUTOFMEMORY:
            printf("Out of memory\n");
            break;
        case E_NOINTERFACE:
            printf("No such interface supported\n");
            break;
        case E_NOTIMPL:
            printf("Not implemented\n");
            break;
        case E_POINTER:
            printf("Pointer is invalid\n");
            break;
        case E_UNEXPECTED:
            printf("Unexpected error\n");
            break;
        case NTE_USER_CANCELLED:
            printf("Passkey creation cancelled by user.\n");
            verificationDataToPost.authenticationResult = USER_CANCELED;
            break;
        case NTE_AUTHENTICATION_IGNORED:
            printf("Passkey authentication ignored user.\n");
            break;
        case NTE_INVALID_PARAMETER:
            printf("One or more invalid parameters.\n");
            verificationDataToPost.authenticationResult = INVALID_PARAMETER;
            break;
        default:
            printf("Unknown error. HRESULT = 0x%lx\n", hr);
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
                fprintf(stderr, "ERROR: Failed to malloc for authenticatorDataBytes\n");
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
                    fprintf(stderr, "ERROR: Failed to malloc for signatureDataBytes\n");
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
                    fprintf(stderr, "ERROR: Failed to malloc for userHandle\n");
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

    printf("\n\n\nVerification Result\n\n\n");
    if (verificationDataToPost.authenticationResult ==SUCCESS ) {
        printf("Origin: %s\n", verificationDataToPost.origin);
        printf("Challenge: %s\n", verificationDataToPost.challenge);
        printf("Type: %s\n", verificationDataToPost.type);
        printf("AuthenticatorData  Length: %i\n", verificationDataToPost.authenticatorDataLength);
        printf("Authenticator Data Bytes: ");
        for (DWORD i = 0; i <  verificationDataToPost.authenticatorDataLength; ++i) {
            printf("%02X ", verificationDataToPost.authenticatorDataBytes[i]);
        }
        printf("\n");

        printf("Signature Length: %i\n", verificationDataToPost.signatureBytesLength);
        printf("Signature Data Bytes: ");
        for (DWORD i = 0; i <  verificationDataToPost.signatureBytesLength; ++i) {
            printf("%02X ", verificationDataToPost.signatureBytes[i]);
        }
        printf("\n");

        printf("User handle Length: %i\n", verificationDataToPost.userHandleLength);
        printf("User Handle Bytes: ");
        for (DWORD i = 0; i <  verificationDataToPost.userHandleLength; ++i) {
            printf("%02X ", verificationDataToPost.userHandle[i]);
        }
        printf("\n");
    } else {
        printf("Verification failed.\n");
    }

    WebAuthNFreeAssertion(pWebauthNAssertion);
    pWebauthNAssertion = NULL;

    return verificationDataToPost;
}
