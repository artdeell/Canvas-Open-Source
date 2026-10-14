package com.tgc.sky.accounts;

import com.tgc.sky.GameActivity;

public interface SystemAccountInterface {

    public interface UpdateClientInfoCallback {
        void UpdateClientInfo(SystemAccountClientInfo systemAccountClientInfo);
    }

    SystemAccountClientInfo GetClientInfo();

    default SystemAccountServerInfo GetServerInfo() {
        return null;
    }

    default SystemAccountInterface Initialize(GameActivity gameActivity, UpdateClientInfoCallback updateClientInfoCallback, SystemAccountType systemAccountType) {
        Initialize(gameActivity, updateClientInfoCallback);
        return this;
    }

    default void Initialize(GameActivity gameActivity, UpdateClientInfoCallback updateClientInfoCallback) {
    }

    default void RefreshCredentials() {
        RefreshCredentials(SystemAccountClientRequestState.kSystemAccountClientRequestState_Idle);
    }

    default void RefreshCredentials(SystemAccountClientRequestState systemAccountClientRequestState) {
        RefreshCredentials();
    }

    void SignIn();

    void SignOut();

    default boolean IsAchievementUnlocked(String str) {
        return false;
    }

    default void ShowAchievementsUI() {
    }

    default boolean UnlockAchievement(String str, double d) {
        return false;
    }

    default void onResume() {
    }
}
