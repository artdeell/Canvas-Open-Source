package com.tgc.sky.accounts;

import com.tgc.sky.GameActivity;

public class GooglePlayGames implements SystemAccountInterface {
    private SystemAccountClientInfo m_accountClientInfo;
    private SystemAccountServerInfo m_accountServerInfo;

    public GooglePlayGames() {
    }

    public SystemAccountClientInfo GetClientInfo() {
        return this.m_accountClientInfo;
    }

    public SystemAccountServerInfo GetServerInfo() {
        return this.m_accountServerInfo;
    }

    public SystemAccountInterface Initialize(GameActivity gameActivity, SystemAccountInterface.UpdateClientInfoCallback updateClientInfoCallback, SystemAccountType systemAccountType) {
        SystemAccountClientInfo systemAccountClientInfo = new SystemAccountClientInfo();
        this.m_accountClientInfo = systemAccountClientInfo;
        systemAccountClientInfo.accountType = systemAccountType;
        this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_NotAvailable;

        SystemAccountServerInfo systemAccountServerInfo = new SystemAccountServerInfo();
        this.m_accountServerInfo = systemAccountServerInfo;
        systemAccountServerInfo.type = systemAccountType;
        systemAccountServerInfo.state = SystemAccountServerState.kSystemAccountServerState_UnLinked;

        if (updateClientInfoCallback != null) {
            updateClientInfoCallback.UpdateClientInfo(this.m_accountClientInfo);
        }
        return this;
    }

    public void SignIn() {
    }

    public void SignOut() {
    }

    public void RefreshCredentials() {
    }

    public void onResume() {
    }

    public boolean IsAchievementUnlocked(String id) {
        return false;
    }

    public void ShowAchievementsUI() {
    }

    public boolean UnlockAchievement(String id, double percent) {
        return false;
    }
}
