//
// Created by maks on 06.12.2022.
//
#include <unordered_map>
#include "include/imgui/imgui_internal.h"
#include "include/misc/visibility.h"

PRIVATE_API void do_scroll() {
    ImGuiContext &ctx = *ImGui::GetCurrentContext();
    ImGuiWindow *window = ctx.CurrentWindow;
    bool hovered = false, held = false;
    ImVec2 &windowScrollMax = window->ScrollMax;
    if (windowScrollMax.y != 0) {
        if (ctx.HoveredId == 0) {
            ImGui::ButtonBehavior(window->Rect(), window->GetID("###Canvas_scroll"), &hovered,
                                  &held,
                                  ImGuiButtonFlags_MouseButtonLeft);
        }
        if (hovered && !held) {
            ImVec2 &mouseDelta = ImGui::GetIO().MouseDelta;
            ImGui::SetScrollY(window, window->Scroll.y - mouseDelta.y);
        }
    }
}
