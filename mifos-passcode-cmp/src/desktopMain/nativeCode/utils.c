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
        return FALSE;
    }
    return TRUE;
}

EXPORT wchar_t* Base64UrlEncode(const BYTE* pbData, DWORD cbData) {
    DWORD dwStrLen = 0; 
    wchar_t* pwszEncoded = NULL;

    if (!CryptBinaryToStringW(pbData, cbData, CRYPT_STRING_BASE64 | CRYPT_STRING_NOCRLF, NULL, &dwStrLen)) {
        return NULL;
    }

    pwszEncoded = (wchar_t*)malloc(dwStrLen * sizeof(wchar_t));
    if (pwszEncoded == NULL) {
        return NULL;
    }

    memset(pwszEncoded, 0, dwStrLen * sizeof(wchar_t));

    if (!CryptBinaryToStringW(pbData, cbData, CRYPT_STRING_BASE64 | CRYPT_STRING_NOCRLF, pwszEncoded, &dwStrLen)) {
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

   
    size_t input_len = wcslen(pwszBase64Url);
    
    size_t padding_needed = 0;
    if (input_len % 4 != 0) {
        padding_needed = 4 - (input_len % 4);
    }

    size_t temp_str_len = input_len + padding_needed + 1;
    wchar_t* pwszTempBase64 = (wchar_t*)malloc(temp_str_len * sizeof(wchar_t));
    if (pwszTempBase64 == NULL) {
        *pcbDecodedData = 0;
        return NULL;
    }

    wcscpy_s(pwszTempBase64, temp_str_len, pwszBase64Url);
    for (size_t i = 0; i < input_len; i++) {
        if (pwszTempBase64[i] == L'-') {
            pwszTempBase64[i] = L'+';
        } else if (pwszTempBase64[i] == L'_') {
            pwszTempBase64[i] = L'/';
        }
    }

    for (size_t i = 0; i < padding_needed; i++) {
        pwszTempBase64[input_len + i] = L'=';
    }
    pwszTempBase64[input_len + padding_needed] = L'\0'; 

    DWORD dwDecodedDataLen = 0;
    BYTE* pbDecodedData = NULL;

    if (!CryptStringToBinaryW(pwszTempBase64, 0, CRYPT_STRING_BASE64, NULL, &dwDecodedDataLen, NULL, NULL)) {
        free(pwszTempBase64);
        *pcbDecodedData = 0;
        return NULL;
    }

    pbDecodedData = (BYTE*)malloc(dwDecodedDataLen);
    if (pbDecodedData == NULL) {
        free(pwszTempBase64);
        *pcbDecodedData = 0;
        return NULL;
    }


    
    if (!CryptStringToBinaryW(pwszTempBase64, 0, CRYPT_STRING_BASE64, pbDecodedData, &dwDecodedDataLen, NULL, NULL)) {
        free(pbDecodedData);    
        free(pwszTempBase64);  
        *pcbDecodedData = 0;
        return NULL;
    }

    free(pwszTempBase64); 
    *pcbDecodedData = dwDecodedDataLen;
    return pbDecodedData;
}


EXPORT char *wcharToChar(const wchar_t *wstr) {
    if (wstr == NULL) {
        return NULL;
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
        return NULL;
    }

    wchar_t *wcharString = (wchar_t *)malloc((required_wchars + 1) * sizeof(wchar_t));
    if (wcharString == NULL) {
        return NULL;
    }

    size_t converted_wchars = mbstowcs(wcharString, str, required_wchars + 1);
    if (converted_wchars == (size_t)-1 || converted_wchars > required_wchars) {
        free(wcharString);
        return NULL;
    }

    wcharString[converted_wchars] = L'\0';

    return wcharString;
}
