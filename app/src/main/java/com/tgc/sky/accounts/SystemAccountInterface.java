package com.tgc.sky.accounts;

import com.tgc.sky.GameActivity;

public interface SystemAccountInterface {

    public interface UpdateClientInfoCallback {
        void UpdateClientInfo(SystemAccountClientInfo systemAccountClientInfo);
    }

    SystemAccountClientInfo GetClientInfo();

    SystemAccountServerInfo GetServerInfo();

    void Initialize(GameActivity gameActivity, UpdateClientInfoCallback updateClientInfoCallback);

    void RefreshCredentials(SystemAccountClientRequestState systemAccountClientRequestState);

    void SignIn();

    void SignOut();
}
