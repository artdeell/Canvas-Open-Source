package com.tgc.sky.accounts;

public enum SystemAccountClientState {
    kSystemAccountClientState_Initializing,
    kSystemAccountClientState_SigningOut,
    kSystemAccountClientState_SignedOut,
    kSystemAccountClientState_NeedsToShowUI,
    kSystemAccountClientState_SigningIn,
    kSystemAccountClientState_SignedIn,
    kSystemAccountClientState_NotAvailable,
    kSystemAccountClientState_Error
}
