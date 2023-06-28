//
// Created by maks on 11.12.2022.
//

#ifndef SKY_MODLOADER_FILESELECTOR_H
#define SKY_MODLOADER_FILESELECTOR_H

#include <cstdlib>

/**
 * Callback function for requestFile
 * fd - the file descriptor
 * You need to close it yourself
 */
typedef void (*callback_function)(int fd);

/**
 * Request a file from the user
 * mime_type - the MIME type that you want to select
 * callback - the callback function that will be called when a file is selected
 * save - whether you need to save (true) or load (false) a file with the selector
 * Returns:
 * true if the file selection was requested
 * false if the selector is already busy
 */
bool requestFile(const char* mime_type, callback_function callback, bool save);

#endif //SKY_MODLOADER_FILESELECTOR_H
