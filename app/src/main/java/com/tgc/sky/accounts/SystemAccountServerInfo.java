package com.tgc.sky.accounts;

public class SystemAccountServerInfo {
    public String accountId;
    public String alias;
    public SystemAccountServerState state = SystemAccountServerState.kSystemAccountServerState_Initializing;
    public SystemAccountType type = SystemAccountType.kSystemAccountType_Count;
}
