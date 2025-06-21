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
        return '\0';
    }

    UID = Base64UrlEncode(UID_BYTES, sizeof(UID_BYTES));
    if (UID == NULL) {
        return '\0';
    }

    return UID;
}


// ReSharper disable once CppNonInlineFunctionDefinitionInHeaderFile
EXPORT wchar_t* generateBase64UrlEncodedChallenge() {
    wchar_t *base64UrlEncodedChallenge = NULL;

    BYTE challengeBytes[32];
    if (!GenerateRandomCryptBytes(challengeBytes, sizeof(challengeBytes))) {
        return '\0';
    }

    base64UrlEncodedChallenge = Base64UrlEncode(challengeBytes, sizeof(challengeBytes));
    if (base64UrlEncodedChallenge == NULL) {
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

    wchar_t dynamicClientDataJSON[256]; // Buffer for the wide string (UTF-16)
    int chars_written = swprintf_s(dynamicClientDataJSON, sizeof(dynamicClientDataJSON) / sizeof(wchar_t),
                                   L"{\"challenge\":\"%s\",\"origin\":\"%s\",\"type\":\"%hs\"}",
                                   base64UrlEncodedChallenge, RP_ID, type);

    if (chars_written < 0) {
        free(base64UrlEncodedChallenge);
        return '\0';
    }

    int utf8Len = WideCharToMultiByte(CP_UTF8, 0, dynamicClientDataJSON, -1, NULL, 0, NULL, NULL);
    if (utf8Len <= 0) {
        free(base64UrlEncodedChallenge);
        return '\0';
    }

    utf8ClientDataJSON = (char*)malloc(utf8Len);
    if (!utf8ClientDataJSON) {
        free(base64UrlEncodedChallenge);
        return '\0';
    }

    WideCharToMultiByte(CP_UTF8, 0, dynamicClientDataJSON, -1, utf8ClientDataJSON, utf8Len, NULL, NULL);

    free(base64UrlEncodedChallenge);
    free(RP_ID);
    return utf8ClientDataJSON;
}
