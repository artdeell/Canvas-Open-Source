package com.tgc.sky;

import git.artdeell.skymodloader.auth.Apple;
import com.tgc.sky.accounts.AppleGameCenter;
import git.artdeell.skymodloader.auth.Facebook;
import git.artdeell.skymodloader.auth.Google;
import com.tgc.sky.accounts.GooglePlayGames;
import git.artdeell.skymodloader.auth.Huawei;
import git.artdeell.skymodloader.auth.Nintendo;
import git.artdeell.skymodloader.auth.Sony;
import git.artdeell.skymodloader.auth.Steam;
import git.artdeell.skymodloader.auth.Twitch;

import com.tgc.sky.accounts.SystemAccountClientInfo;
import com.tgc.sky.accounts.SystemAccountClientRequestState;
import com.tgc.sky.accounts.SystemAccountInterface;
import com.tgc.sky.accounts.SystemAccountServerInfo;
import com.tgc.sky.accounts.SystemAccountServerState;
import com.tgc.sky.accounts.SystemAccountType;

import java.util.Hashtable;
import java.util.Map;

public class SystemAccounts_android implements SystemAccountInterface.UpdateClientInfoCallback {
    private static volatile SystemAccounts_android sInstance;
    private final Hashtable<SystemAccountType, SystemAccountInterface> m_systemAccounts;
    private GameActivity m_activity;

    public native void OnSystemAccount(SystemAccountClientInfo systemAccountClientInfo);

    SystemAccounts_android(GameActivity gameActivity) {
        this.m_activity = gameActivity;
        this.m_systemAccounts = new Hashtable<>();

        AppleGameCenter appleGameCenter = new AppleGameCenter();
        appleGameCenter.Initialize(gameActivity, this);
        this.m_systemAccounts.put(SystemAccountType.kSystemAccountType_AppleGameCenter, appleGameCenter);
        this.m_systemAccounts.put(SystemAccountType.kSystemAccountType_AppleGameCenterVN, appleGameCenter);

        Google google = new Google();
        google.Initialize(gameActivity, this);
        this.m_systemAccounts.put(SystemAccountType.kSystemAccountType_Google, google);

        GooglePlayGames googlePlayGames = new GooglePlayGames();
        googlePlayGames.Initialize(gameActivity, this, SystemAccountType.kSystemAccountType_GooglePlayGames);
        this.m_systemAccounts.put(SystemAccountType.kSystemAccountType_GooglePlayGames, googlePlayGames);
        this.m_systemAccounts.put(SystemAccountType.kSystemAccountType_GooglePlayGamesVN, googlePlayGames);

        Facebook facebook = new Facebook();
        facebook.Initialize(gameActivity, this);
        this.m_systemAccounts.put(SystemAccountType.kSystemAccountType_Facebook, facebook);

        Apple apple = new Apple();
        apple.Initialize(gameActivity, this);
        this.m_systemAccounts.put(SystemAccountType.kSystemAccountType_Apple, apple);
        this.m_systemAccounts.put(SystemAccountType.kSystemAccountType_AppleVN, apple);

        Nintendo nintendo = new Nintendo();
        nintendo.Initialize(gameActivity, this);
        this.m_systemAccounts.put(SystemAccountType.kSystemAccountType_Nintendo, nintendo);

        Huawei huawei = new Huawei();
        huawei.Initialize(gameActivity, this);
        this.m_systemAccounts.put(SystemAccountType.kSystemAccountType_Huawei, huawei);

        Sony playstation = new Sony();
        playstation.Initialize(gameActivity, this);
        this.m_systemAccounts.put(SystemAccountType.kSystemAccountType_Sony, playstation);

        Steam steam = new Steam();
        steam.Initialize(gameActivity, this);
        this.m_systemAccounts.put(SystemAccountType.kSystemAccountType_Steam, steam);

        Twitch twitch = new Twitch();
        twitch.Initialize(gameActivity, this);
        this.m_systemAccounts.put(SystemAccountType.kSystemAccountType_Twitch, twitch);

        sInstance = this;
    }

    public static SystemAccounts_android getInstance() {
        return sInstance;
    }

    public SystemAccountInterface GetSystemAccount(SystemAccountType systemAccountType) {
        if (systemAccountType == null) return null;
        return this.m_systemAccounts.get(systemAccountType);
    }

    SystemAccountType GetGooglePlayGamesAccountType() {
        if ("com.tgc.sky.android".contains(".vn.")) {
            return SystemAccountType.kSystemAccountType_GooglePlayGamesVN;
        }
        return SystemAccountType.kSystemAccountType_GooglePlayGames;
    }

    public boolean IsAchievementUnlocked(String str) {
        SystemAccountInterface acc = GetSystemAccount(GetGooglePlayGamesAccountType());
        if (acc != null) {
            return acc.IsAchievementUnlocked(str);
        }
        return false;
    }

    public void ShowAchievementsUI() {
        SystemAccountInterface acc = GetSystemAccount(GetGooglePlayGamesAccountType());
        if (acc != null) {
            acc.ShowAchievementsUI();
        }
    }

    public boolean UnlockAchievement(String str, double d) {
        SystemAccountInterface acc = GetSystemAccount(GetGooglePlayGamesAccountType());
        if (acc != null) {
            return acc.UnlockAchievement(str, d);
        }
        return false;
    }

    void onResume() {
        for (Map.Entry<SystemAccountType, SystemAccountInterface> entry : this.m_systemAccounts.entrySet()) {
            if (entry.getValue() != null) {
                entry.getValue().onResume();
            }
        }
    }

    public void InitializeCredentials(int i) {
        if (i < 0 || i >= SystemAccountType.values().length) return;
        SystemAccountType type = SystemAccountType.values()[i];
        android.util.Log.i("SystemAccounts", "InitializeCredentials called for: " + type + " (index " + i + ")");
        SystemAccountInterface acc = GetSystemAccount(type);
        if (acc != null) {
            UpdateClientInfo(acc.GetClientInfo());
        } else {
            android.util.Log.w("SystemAccounts", "No handler for: " + type);
        }
    }

    public void SignIn(int i) {
        if (i < 0 || i >= SystemAccountType.values().length) return;
        SystemAccountType type = SystemAccountType.values()[i];
        android.util.Log.i("SystemAccounts", "SignIn called for: " + type + " (index " + i + ")");
        SystemAccountInterface acc = GetSystemAccount(type);
        if (acc != null) {
            acc.SignIn();
        } else {
            android.util.Log.w("SystemAccounts", "No handler for SignIn: " + type);
        }
    }

    public void SignOut(int i) {
        if (i < 0 || i >= SystemAccountType.values().length) return;
        SystemAccountInterface acc = GetSystemAccount(SystemAccountType.values()[i]);
        if (acc != null) {
            acc.SignOut();
        }
    }

    public void RefreshCredentials(int i) {
        if (i < 0 || i >= SystemAccountType.values().length) return;
        SystemAccountInterface acc = GetSystemAccount(SystemAccountType.values()[i]);
        if (acc != null) {
            acc.RefreshCredentials();
        }
    }

    public void RefreshCredentials(int i, int i2) {
        if (i < 0 || i >= SystemAccountType.values().length) return;
        SystemAccountInterface acc = GetSystemAccount(SystemAccountType.values()[i]);
        if (acc != null) {
            if (i2 >= 0 && i2 < SystemAccountClientRequestState.values().length) {
                acc.RefreshCredentials(SystemAccountClientRequestState.values()[i2]);
            } else {
                acc.RefreshCredentials();
            }
        }
    }

    public void UpdateServerInfo(int i, int i2, String str, String str2) {
        if (i < 0 || i >= SystemAccountType.values().length) return;
        SystemAccountInterface acc = GetSystemAccount(SystemAccountType.values()[i]);
        if (acc != null) {
            SystemAccountServerInfo serverInfo = acc.GetServerInfo();
            if (serverInfo != null) {
                if (i2 >= 0 && i2 < SystemAccountServerState.values().length) {
                    serverInfo.state = SystemAccountServerState.values()[i2];
                }
                serverInfo.accountId = str;
                serverInfo.alias = str2;
            }
        }
    }

    public void UpdateClientInfo(SystemAccountClientInfo systemAccountClientInfo) {
        OnSystemAccount(systemAccountClientInfo);
    }

    public static String systemAccountTypeToString(SystemAccountType systemAccountType) {
        if (systemAccountType == null) return null;
        switch (systemAccountType) {
            case kSystemAccountType_Local:
                return "Local";
            case kSystemAccountType_AppleGameCenter:
                return "AppleGameCenter";
            case kSystemAccountType_AppleGameCenterVN:
                return "AppleGameCenterVN";
            case kSystemAccountType_Google:
                return "Google";
            case kSystemAccountType_GooglePlayGames:
                return "GooglePlayGames";
            case kSystemAccountType_GooglePlayGamesVN:
                return "GooglePlayGamesVN";
            case kSystemAccountType_Facebook:
                return "Facebook";
            case kSystemAccountType_Apple:
                return "Apple";
            case kSystemAccountType_AppleVN:
                return "AppleVN";
            case kSystemAccountType_Nintendo:
                return "Nintendo";
            case kSystemAccountType_Huawei:
                return "Huawei";
            case kSystemAccountType_Sony:
                return "Sony";
            case kSystemAccountType_Steam:
                return "Steam";
            case kSystemAccountType_Twitch:
                return "Twitch";
            default:
                return null;
        }
    }
}
