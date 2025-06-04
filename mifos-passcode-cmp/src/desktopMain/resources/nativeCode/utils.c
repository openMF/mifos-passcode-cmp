#include <stdint.h>
#include <stdio.h>
#include <windows.h>
#include <wincrypt.h>
#include <bcrypt.h>
#include <winternl.h>
#include "utils.h"


EXPORT BOOL GenerateRandomCryptBytes(BYTE* pbChallenge, DWORD cbChallenge) {
    NTSTATUS status = BCryptGenRandom(NULL, pbChallenge, cbChallenge, BCRYPT_USE_SYSTEM_PREFERRED_RNG);
    if (!NT_SUCCESS(status)) {
        fprintf(stderr, "Failed to generate random challenge: 0x%lx\n", status);
        return FALSE;
    }
    return TRUE;
}

EXPORT wchar_t* Base64UrlEncode(const BYTE* pbData, DWORD cbData) {
    DWORD dwStrLen = 0; // This will hold the buffer size INCLUDING null terminator
    wchar_t* pwszEncoded = NULL;

    if (!CryptBinaryToStringW(pbData, cbData, CRYPT_STRING_BASE64 | CRYPT_STRING_NOCRLF, NULL, &dwStrLen)) {
        fprintf(stderr, "Base64UrlEncode: Error getting Base64 string length (first call): 0x%lx\n", GetLastError());
        return NULL;
    }

    pwszEncoded = (wchar_t*)malloc(dwStrLen * sizeof(wchar_t));
    if (pwszEncoded == NULL) {
        fprintf(stderr, "Base64UrlEncode: Memory allocation failed.\n");
        return NULL;
    }

    memset(pwszEncoded, 0, dwStrLen * sizeof(wchar_t));

    if (!CryptBinaryToStringW(pbData, cbData, CRYPT_STRING_BASE64 | CRYPT_STRING_NOCRLF, pwszEncoded, &dwStrLen)) {
        fprintf(stderr, "Base64UrlEncode: Error performing Base64 encoding (second call): 0x%lx\n", GetLastError());
        free(pwszEncoded);
        return NULL;
    }

    DWORD actual_string_length_before_url_encoding = wcslen(pwszEncoded);

    for (DWORD i = 0; i < actual_string_length_before_url_encoding; i++) {
        if (pwszEncoded[i] == L'+') {
            pwszEncoded[i] = L'-';
        } else if (pwszEncoded[i] == L'/') {
            pwszEncoded[i] = L'_';
        } else if (pwszEncoded[i] == L'=') {
            // Found padding. Null-terminate here.
            pwszEncoded[i] = L'\0';
            break;
        }
    }

    return pwszEncoded;
}

EXPORT BYTE* Base64UrlDecode(const wchar_t* pwszBase64Url, DWORD* pcbDecodedData) {
    if (pwszBase64Url == NULL) {
        *pcbDecodedData = 0;
        return NULL;
    }

    // 1. Create a modifiable copy of the input string and convert Base64URL chars to standard Base64 chars
    size_t input_len = wcslen(pwszBase64Url);
    // Base64 strings are always a multiple of 4. If not, padding is needed.
    // Calculate required padding (0, 1, 2, or 3 '=' characters)
    size_t padding_needed = 0;
    if (input_len % 4 != 0) {
        padding_needed = 4 - (input_len % 4);
    }

    // Allocate space for the temporary string (original + padding + null terminator)
    size_t temp_str_len = input_len + padding_needed + 1;
    wchar_t* pwszTempBase64 = (wchar_t*)malloc(temp_str_len * sizeof(wchar_t));
    if (pwszTempBase64 == NULL) {
        fprintf(stderr, "Base64UrlDecode: Memory allocation failed for temp string.\n");
        *pcbDecodedData = 0;
        return NULL;
    }

    // Copy and replace characters
    wcscpy_s(pwszTempBase64, temp_str_len, pwszBase64Url); // Use wcscpy_s for safety
    for (size_t i = 0; i < input_len; i++) {
        if (pwszTempBase64[i] == L'-') {
            pwszTempBase64[i] = L'+';
        } else if (pwszTempBase64[i] == L'_') {
            pwszTempBase64[i] = L'/';
        }
        // No need to explicitly handle '=' here as CryptStringToBinaryW will handle it.
    }

    // Add padding characters
    for (size_t i = 0; i < padding_needed; i++) {
        pwszTempBase64[input_len + i] = L'=';
    }
    pwszTempBase64[input_len + padding_needed] = L'\0'; // Null-terminate the temporary string

    DWORD dwDecodedDataLen = 0;
    BYTE* pbDecodedData = NULL;

    // 2. Get required buffer size for decoded binary data (using standard Base64 flag)
    // No dwchSkip and pdwchError for this basic usage.
    if (!CryptStringToBinaryW(pwszTempBase64, 0, CRYPT_STRING_BASE64, NULL, &dwDecodedDataLen, NULL, NULL)) {
        fprintf(stderr, "Base64UrlDecode: Error getting decoded data length (first call): 0x%lx\n", GetLastError());
        free(pwszTempBase64);
        *pcbDecodedData = 0;
        return NULL;
    }

    pbDecodedData = (BYTE*)malloc(dwDecodedDataLen);
    if (pbDecodedData == NULL) {
        fprintf(stderr, "Base64UrlDecode: Memory allocation failed for decoded data.\n");
        free(pwszTempBase64);
        *pcbDecodedData = 0;
        return NULL;
    }
    // memset(pbDecodedData, 0, dwDecodedDataLen); // Optional, CryptStringToBinaryW should fill it

    // 3. Perform Base64 decoding
    if (!CryptStringToBinaryW(pwszTempBase64, 0, CRYPT_STRING_BASE64, pbDecodedData, &dwDecodedDataLen, NULL, NULL)) {
        fprintf(stderr, "Base64UrlDecode: Error performing Base64 decoding (second call): 0x%lx\n", GetLastError());
        free(pbDecodedData);    // Clean up allocated memory for output data
        free(pwszTempBase64);   // Clean up allocated memory for temp string
        *pcbDecodedData = 0;
        return NULL;
    }

    free(pwszTempBase64); // IMPORTANT: Free the temporary string buffer
    *pcbDecodedData = dwDecodedDataLen; // Output the actual decoded length
    return pbDecodedData;
}


EXPORT char *wcharToChar(const wchar_t *wstr) {
    if (wstr == NULL) {
        return NULL; // Handle NULL input gracefully
    }

    size_t required_buffer_size = wcstombs(NULL, wstr, 0);
    if (required_buffer_size == (size_t)-1) {
        return NULL;
    }

    char *charString = (char *)malloc(required_buffer_size + 1);

    if (charString == NULL) {
        return NULL;
    }

    size_t bytes_converted = wcstombs(charString, wstr, required_buffer_size + 1);

    if (bytes_converted == (size_t)-1) {
        free(charString);
        return NULL;
    }

    return charString;
}


EXPORT wchar_t *charToWchar(const char *str) {
    if (str == NULL) {
        return NULL;
    }

    size_t required_wchars = mbstowcs(NULL, str, 0);
    if (required_wchars == (size_t)-1) {
        fprintf(stderr, "Error converting multi-byte to wide char: Invalid sequence.\n");
        return NULL;
    }

    wchar_t *wcharString = (wchar_t *)malloc((required_wchars + 1) * sizeof(wchar_t));
    if (wcharString == NULL) {
        fprintf(stderr, "Error: malloc failed for wcharString.\n");
        return NULL; // Handle malloc failure
    }

    size_t converted_wchars = mbstowcs(wcharString, str, required_wchars + 1);
    if (converted_wchars == (size_t)-1 || converted_wchars > required_wchars) {
        fprintf(stderr, "Error: mbstowcs conversion failed or buffer overflow.\n");
        free(wcharString);
        return NULL;
    }

    wcharString[converted_wchars] = L'\0';

    return wcharString;
}
