#include "mock_server.h"
#include <stdio.h>
#include <windows.h>
#include <webauthn.h>
#include <stdlib.h>
#include <string.h>
#include <wchar.h>
#include <conio.h>
#include <stdbool.h>
#include "registration.h"


EXPORT RegistrationDataPOST initiateUserRegistration(
    wchar_t *origin,
    wchar_t *challenge,
    const int timeout,
    const wchar_t *rpId,
    const wchar_t *rpName,
    char *userID,
    const wchar_t *account_name,
    const wchar_t *display_name
) {
    RegistrationDataPOST registrationData= {
        .authenticationResult = false
    };

    wchar_t *base64_url_encoded_challenge = challenge;

    registrationData.challenge = wcharToChar(base64_url_encoded_challenge);
    registrationData.origin = wcharToChar(origin);
    registrationData.type = "webauthn.create";

    char* utf8ClientDataJSON = generateClientDataJson(
        origin,
        base64_url_encoded_challenge,
        "webauthn.create"
    );


    WEBAUTHN_RP_ENTITY_INFORMATION rp_entity_information= {
        .dwVersion = WEBAUTHN_RP_ENTITY_INFORMATION_CURRENT_VERSION,
        .pwszId = rpId,
        .pwszName = rpName,
        .pwszIcon = NULL,
    };

    WEBAUTHN_USER_ENTITY_INFORMATION user_entity_info = {
        .dwVersion = WEBAUTHN_USER_ENTITY_INFORMATION_CURRENT_VERSION,
        .pbId = (PBYTE) userID,
        .cbId = (DWORD)strlen(userID),
        .pwszName = account_name,
        .pwszIcon = NULL,
        .pwszDisplayName = display_name,
    };

    // Define multiple COSE credential parameters
    WEBAUTHN_COSE_CREDENTIAL_PARAMETER cose_credential_parameters_array[] = {
        {
            .dwVersion = WEBAUTHN_COSE_CREDENTIAL_PARAMETER_CURRENT_VERSION,
            .pwszCredentialType = WEBAUTHN_CREDENTIAL_TYPE_PUBLIC_KEY,
            .lAlg = WEBAUTHN_COSE_ALGORITHM_ECDSA_P256_WITH_SHA256 // ES256
        },
        {
            .dwVersion = WEBAUTHN_COSE_CREDENTIAL_PARAMETER_CURRENT_VERSION,
            .pwszCredentialType = WEBAUTHN_CREDENTIAL_TYPE_PUBLIC_KEY,
            .lAlg = WEBAUTHN_COSE_ALGORITHM_RSASSA_PKCS1_V1_5_WITH_SHA256 // RS256
        }
    };

    WEBAUTHN_COSE_CREDENTIAL_PARAMETERS cose_credential_parameters = {
        .cCredentialParameters = ARRAYSIZE(cose_credential_parameters_array),
        .pCredentialParameters = cose_credential_parameters_array
    };


    WEBAUTHN_CLIENT_DATA client_data = {
        .dwVersion = WEBAUTHN_CLIENT_DATA_CURRENT_VERSION,
        .cbClientDataJSON = (DWORD)(strlen(utf8ClientDataJSON)),
        .pbClientDataJSON = (PBYTE)utf8ClientDataJSON,
        .pwszHashAlgId = WEBAUTHN_HASH_ALGORITHM_SHA_256
    };

    // WEBAUTHN_CREDENTIAL credential = {
    //     .dwVersion = WEBAUTHN_CREDENTIAL_CURRENT_VERSION,
    //     .cbId = (DWORD)strlen(userID),
    //     .pwszCredentialType = WEBAUTHN_CREDENTIAL_TYPE_PUBLIC_KEY,
    //     .pbId = (PBYTE)userID,
    // };
    //
    // WEBAUTHN_CREDENTIALS webauthn_credentials = {
    //     .cCredentials = 1,
    //     .pCredentials = &credential
    // };

    WEBAUTHN_AUTHENTICATOR_MAKE_CREDENTIAL_OPTIONS authenticator_make_credential_options = {
        .dwVersion = WEBAUTHN_AUTHENTICATOR_MAKE_CREDENTIAL_OPTIONS_CURRENT_VERSION,
        .dwTimeoutMilliseconds = timeout,
        .CredentialList = {0},
        .dwAuthenticatorAttachment = WEBAUTHN_AUTHENTICATOR_ATTACHMENT_PLATFORM,
        .bRequireResidentKey = FALSE,
        .dwUserVerificationRequirement = WEBAUTHN_USER_VERIFICATION_REQUIREMENT_REQUIRED,
        .dwAttestationConveyancePreference = WEBAUTHN_ATTESTATION_CONVEYANCE_PREFERENCE_NONE,
        .pExcludeCredentialList = NULL,
        .bPreferResidentKey = TRUE,
    };

    WEBAUTHN_CREDENTIAL_ATTESTATION *pWebAuthNCredentialAttestation = NULL;

    HWND hWnd = GetForegroundWindow();
    if (hWnd == NULL) {
        fprintf(stderr, "Failed to get console window handle. Continuing with NULL HWND.\n");
    }


    HRESULT hr = WebAuthNAuthenticatorMakeCredential(
        hWnd,
        &rp_entity_information,
        &user_entity_info,
        &cose_credential_parameters,
        &client_data,
        &authenticator_make_credential_options,
        &pWebAuthNCredentialAttestation
    );

    printf("%lx\n", hr);

    switch (hr) {
        case S_OK:
            printf("Successfully created credential\n");
            registrationData.authenticationResult = SUCCESS;
            break;
        case S_FALSE:
            printf("Failed registration\n");
            registrationData.authenticationResult = false;
            break;
        case E_ABORT:
            printf("Aborted operation\n");
            registrationData.authenticationResult = ABORT;
            break;
        case E_ACCESSDENIED:
            printf("Access denied\n");
            break;
        case E_FAIL:
            printf("Failure\n");
            registrationData.authenticationResult = E_FAILURE;
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
            registrationData.authenticationResult = USER_CANCELED;
            break;
        case NTE_AUTHENTICATION_IGNORED:
            printf("Passkey authentication ignored user.\n");
            break;
        case NTE_INVALID_PARAMETER:
            printf("One or more invalid parameters.\n");
            registrationData.authenticationResult = INVALID_PARAMETER;
            break;
        default:
            printf("Unknown error. HRESULT = 0x%lx\n", hr);
            break;
    }

    if (registrationData.authenticationResult == SUCCESS) { // This should be set to true if hr == S_OK
        // --- Populate attestationObjectBytes with a deep copy ---
        if (pWebAuthNCredentialAttestation->pbAttestationObject && pWebAuthNCredentialAttestation->cbAttestationObject > 0) {
            registrationData.attestationObjectBytes = (byte *)malloc(pWebAuthNCredentialAttestation->cbAttestationObject);
            if (registrationData.attestationObjectBytes) {
                memcpy(registrationData.attestationObjectBytes,
                       pWebAuthNCredentialAttestation->pbAttestationObject,
                       pWebAuthNCredentialAttestation->cbAttestationObject);
                registrationData.attestationObjectLength = (int)pWebAuthNCredentialAttestation->cbAttestationObject;
            } else {
                fprintf(stderr, "ERROR: Failed to malloc for attestationObjectBytes\n");
                registrationData.attestationObjectBytes = NULL; // Ensure it's NULL on failure
                registrationData.attestationObjectLength = 0;
                registrationData.authenticationResult = MEMORY_ALLOCATION_ERROR;
            }

            if (pWebAuthNCredentialAttestation->pbCredentialId && pWebAuthNCredentialAttestation->cbCredentialId > 0) {
                registrationData.credentialIdBytes = (byte *)malloc(pWebAuthNCredentialAttestation->cbCredentialId);
                if (registrationData.credentialIdBytes) {
                    memcpy(
                        registrationData.credentialIdBytes,
                        pWebAuthNCredentialAttestation->pbCredentialId,
                        pWebAuthNCredentialAttestation->cbCredentialId
                    );
                    registrationData.credentialIdLength = (int)pWebAuthNCredentialAttestation->cbCredentialId;
                }else {
                    fprintf(stderr, "ERROR: Failed to malloc for credentialID\n");
                    registrationData.attestationObjectBytes = NULL; // Ensure it's NULL on failure
                    registrationData.attestationObjectLength = 0;
                    registrationData.authenticationResult = MEMORY_ALLOCATION_ERROR;
                    registrationData.credentialIdBytes = NULL;
                    registrationData.credentialIdLength = 0;
                }
            }else {
                registrationData.attestationObjectBytes = NULL;
                registrationData.attestationObjectLength = 0;
                registrationData.authenticationResult = MEMORY_ALLOCATION_ERROR;
                registrationData.credentialIdBytes = NULL;
                registrationData.credentialIdLength = 0;
            }

        } else {
            registrationData.attestationObjectBytes = NULL;
            registrationData.attestationObjectLength = 0;
            registrationData.authenticationResult = MEMORY_ALLOCATION_ERROR;
            registrationData.credentialIdBytes = NULL;
            registrationData.credentialIdLength = 0;
        }

    } else { // If WebAuthNAuthenticatorMakeCredential failed
        registrationData.attestationObjectBytes = NULL;
        registrationData.attestationObjectLength = 0;
        registrationData.credentialIdBytes = NULL;
        registrationData.credentialIdLength = 0;
    }

    printf("Registration Result\n");
    if (registrationData.authenticationResult == SUCCESS) {
        printf("Origin: %s\n", registrationData.origin);
        printf("Challenge: %s\n", registrationData.challenge);
        printf("Type: %s\n", registrationData.type);
        printf("Attestation Object Length: %i\n", registrationData.attestationObjectLength);
        printf("Attestation Object Bytes: ");
        for (DWORD i = 0; i <  registrationData.attestationObjectLength; ++i) {
            printf("%02X ", registrationData.attestationObjectBytes[i]);
        }
        printf("\n");

        printf("Credential Id Length: %i\n", registrationData.credentialIdLength);
        printf("Credential ID Bytes: ");
        for (DWORD i = 0; i <  registrationData.credentialIdLength; ++i) {
            printf("%02X ", registrationData.credentialIdBytes[i]);
        }
        printf("\n");
    } else {
        printf("Registration failed.\n");
    }

    WebAuthNFreeCredentialAttestation(pWebAuthNCredentialAttestation);
    pWebAuthNCredentialAttestation = NULL; // Good practice

    return registrationData;
}
