package com.tgc.sky.accounts;

public class SystemAccountClientInfo {
    public String accountId;
    public SystemAccountType accountType = SystemAccountType.kSystemAccountType_Count;
    public String alias;
    public String error;
    public String permissions;
    public SystemAccountClientRequestState requestState = SystemAccountClientRequestState.kSystemAccountClientRequestState_Idle;
    public String signature;
    public SystemAccountClientState state = SystemAccountClientState.kSystemAccountClientState_Initializing;
}
