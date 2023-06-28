package com.tgc.sky.accounts;

import com.tgc.sky.GameActivity;
import com.tgc.sky.accounts.SystemAccountInterface;

public class AppleGameCenter implements SystemAccountInterface {
    private SystemAccountClientInfo m_accountClientInfo;
    private SystemAccountServerInfo m_accountServerInfo;

    public void RefreshCredentials(SystemAccountClientRequestState systemAccountClientRequestState) {
    }

    public void SignIn() {
    }

    public void SignOut() {
    }

    public void Initialize(GameActivity gameActivity, SystemAccountInterface.UpdateClientInfoCallback updateClientInfoCallback) {
        SystemAccountClientInfo systemAccountClientInfo = new SystemAccountClientInfo();
        this.m_accountClientInfo = systemAccountClientInfo;
        systemAccountClientInfo.accountType = SystemAccountType.kSystemAccountType_AppleGameCenter;
        this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_NotAvailable;
        SystemAccountServerInfo systemAccountServerInfo = new SystemAccountServerInfo();
        this.m_accountServerInfo = systemAccountServerInfo;
        systemAccountServerInfo.type = SystemAccountType.kSystemAccountType_AppleGameCenter;
        this.m_accountServerInfo.state = SystemAccountServerState.kSystemAccountServerState_UnLinked;
    }

    public SystemAccountClientInfo GetClientInfo() {
        return this.m_accountClientInfo;
    }

    public SystemAccountServerInfo GetServerInfo() {
        return this.m_accountServerInfo;
    }
}
