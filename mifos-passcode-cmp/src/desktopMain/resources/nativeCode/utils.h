#ifndef UTILS_H
#define UTILS_H
#define EXPORT __declspec(dllexport)

#include <windows.h>

EXPORT BOOL GenerateRandomCryptBytes(BYTE* pbChallenge, DWORD cbChallenge);

EXPORT wchar_t* Base64UrlEncode(const BYTE* pbData, DWORD cbData);

EXPORT BYTE* Base64UrlDecode(const wchar_t* pwszBase64Url, DWORD* pcbDecodedData);

EXPORT char *wcharToChar(const wchar_t *wstr);

EXPORT wchar_t *charToWchar(const char *str);

#endif //UTILS_H
