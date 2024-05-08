//
// Created by maks on 05.09.2022.
//

#ifndef SKY_MODLOADER_PROCUTILS_H
#define SKY_MODLOADER_PROCUTILS_H

#ifdef __cplusplus
extern "C" {
#endif
int procutils_read_mprotection(void* addr);
#ifdef __cplusplus
};
#endif

#endif //SKY_MODLOADER_PROCUTILS_H
