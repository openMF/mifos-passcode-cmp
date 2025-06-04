#include "mock_server.h"

#include <stdbool.h>
#include <stdio.h>
#include <windows.h>
#include <string.h>
#include <stdlib.h>
#include "utils.h"
#include "mock_server.h"


EXPORT wchar_t* generateBase64UrlEncodedUID() {
    wchar_t *UID = NULL;

    BYTE UID_BYTES[16];
    if (!GenerateRandomCryptBytes(UID_BYTES, sizeof(UID_BYTES))) {
        fprintf(stderr, "Main: Failed to generate random challenge.\n");
        return '\0';
    }

    UID = Base64UrlEncode(UID_BYTES, sizeof(UID_BYTES));
    if (UID == NULL) {
        fprintf(stderr, "Main: Base64UrlEncode failed.\n");
        return '\0';
    }

    return UID;
}


// ReSharper disable once CppNonInlineFunctionDefinitionInHeaderFile
EXPORT wchar_t* generateBase64UrlEncodedChallenge() {
    wchar_t *base64UrlEncodedChallenge = NULL;

    BYTE challengeBytes[32];
    if (!GenerateRandomCryptBytes(challengeBytes, sizeof(challengeBytes))) {
        fprintf(stderr, "Main: Failed to generate random challenge.\n");
        return '\0';
    }

    base64UrlEncodedChallenge = Base64UrlEncode(challengeBytes, sizeof(challengeBytes));
    if (base64UrlEncodedChallenge == NULL) {
        fprintf(stderr, "Main: Base64UrlEncode failed.\n");
        return '\0';
    }

    return base64UrlEncodedChallenge;
}

// ReSharper disable once CppNonInlineFunctionDefinitionInHeaderFile
EXPORT char* generateClientDataJson(
    wchar_t *rp_id,
    wchar_t *base64UrlEncodedChallenge,
    const char *type
) {
    wchar_t *RP_ID = rp_id;

    char* utf8ClientDataJSON = NULL;

    printf("DEBUG: base64UrlEncodedChallenge (DYNAMIC): \"%s\"\n", wcharToChar(base64UrlEncodedChallenge)); // Use dynamic challenge
    printf("DEBUG: wcslen(base64UrlEncodedChallenge): %llu\n", wcslen(base64UrlEncodedChallenge));
    printf("DEBUG: wcslen(RP_ID): %llu\n", wcslen(RP_ID));


    wchar_t dynamicClientDataJSON[256]; // Buffer for the wide string (UTF-16)
    int chars_written = swprintf_s(dynamicClientDataJSON, sizeof(dynamicClientDataJSON) / sizeof(wchar_t),
                                   L"{\"challenge\":\"%s\",\"origin\":\"%s\",\"type\":\"%hs\"}",
                                   base64UrlEncodedChallenge, RP_ID, type);

    printf("DEBUG: swprintf_s returned chars_written: %d\n", chars_written);

    if (chars_written < 0) {
        fprintf(stderr, "Error formatting clientDataJSON string. swprintf_s returned: %d\n", chars_written);
        free(base64UrlEncodedChallenge);
        return '\0';
    }


    printf("DEBUG: dynamicClientDataJSON content (after swprintf_s):\n\"%s\"\n", wcharToChar(dynamicClientDataJSON));
    printf("DEBUG: wcslen(dynamicClientDataJSON) chars: %llu\n", wcslen(dynamicClientDataJSON));



    int utf8Len = WideCharToMultiByte(CP_UTF8, 0, dynamicClientDataJSON, -1, NULL, 0, NULL, NULL);
    if (utf8Len <= 0) {
        fprintf(stderr, "Failed to get UTF-8 buffer length. Error: %lu\n", GetLastError());
        free(base64UrlEncodedChallenge);
        return '\0';
    }

    utf8ClientDataJSON = (char*)malloc(utf8Len);
    if (!utf8ClientDataJSON) {
        fprintf(stderr, "Failed to allocate memory for UTF-8 client data.\n");
        free(base64UrlEncodedChallenge);
        return '\0';
    }

    WideCharToMultiByte(CP_UTF8, 0, dynamicClientDataJSON, -1, utf8ClientDataJSON, utf8Len, NULL, NULL);

    printf("DEBUG: UTF-8 ClientDataJSON content:\n\"%s\"\n", utf8ClientDataJSON);
    printf("DEBUG: strlen(utf8ClientDataJSON) chars: %llu\n", strlen(utf8ClientDataJSON));

    free(base64UrlEncodedChallenge);
    printf("DEBUG: wcslen(base64UrlEncodedChallenge): %llu\n", wcslen(base64UrlEncodedChallenge));

    free(RP_ID);
    return utf8ClientDataJSON;
}
