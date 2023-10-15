package com.tgc.sky.accounts;

public enum SystemAccountServerState {
    kSystemAccountServerState_Initializing,
    kSystemAccountServerState_UnLinking,
    kSystemAccountServerState_UnLinked,
    kSystemAccountServerState_UnLinkedOther,
    kSystemAccountServerState_Linking,
    kSystemAccountServerState_Linked,
    kSystemAccountServerState_LinkedOther,
    kSystemAccountServerState_LinkError
}
