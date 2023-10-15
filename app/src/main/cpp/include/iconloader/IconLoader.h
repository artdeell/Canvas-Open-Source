//
// Created by maks on 01.10.2022.
//

#ifndef SKY_MODLOADER_ICONLOADER_H
#define SKY_MODLOADER_ICONLOADER_H
#include <string>
#include <unordered_map>
#include "../imgui/imgui.h"
#include <GLES3/gl3.h>
#include <android/asset_manager.h>

#define IL_NO_TEXTURE (ImTextureID)-1

class PrivateUIIcon {
public:
    std::string atlasName;
    ImTextureID atlasTexture;
    ImVec2 uv0;
    ImVec2 uv1;
};


class UIIcon {
public:
    ImTextureID textureId;
    ImVec2 uv0;
    ImVec2 uv1;
};
extern std::unordered_map<std::string, PrivateUIIcon*> icons;

class SkyImage {
public:
    ImTextureID textureId;
    ImVec2 size;
};
class IconLoader {
private:
    static std::unordered_map<std::string, SkyImage*> images;
    static std::unordered_map<std::string, SkyImage*> atlas_images;
    static SkyImage& uploadImageKtx(const char* name, const bool& is_atlas);
    static SkyImage &getAtlasImage(const std::string &name);
    static PrivateUIIcon* getIcon(const std::string& icon);
public:
    static struct AAssetManager* aAssetManager;
    static SkyImage& getImage(const std::string& name);
    static void icon(const std::string& name, const float& size, const ImVec4& color = ImVec4(1,1,1,1));
    static bool iconButton(const std::string& name, const float& size, const ImVec4& color = ImVec4(1,1,1,1));
    static void getUIIcon(const std::string &name, UIIcon* icon);
};


#endif //SKY_MODLOADER_ICONLOADER_H
