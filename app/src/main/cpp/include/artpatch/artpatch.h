//
// Created by artDev on 04.09.2022.
//

#ifndef SKY_MODLOADER_ARTPATCH_H
#define SKY_MODLOADER_ARTPATCH_H

#ifdef __cplusplus
extern "C" {
#endif
#ifndef REDEFINE_PATCHT
typedef void* patch_t;
#endif
patch_t artpatch_new();
void artpatch_die(patch_t);
void artpatch_set_addr(patch_t, void*);
void artpatch_set_hex(patch_t ,const char*);
void artpatch_apply(patch_t);
void artpatch_restore(patch_t);
#ifdef __cplusplus
};
#endif

#endif //SKY_MODLOADER_ARTPATCH_H
