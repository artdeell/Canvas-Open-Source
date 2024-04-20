package com.tgc.sky.ui.text;

import com.tgc.sky.GameActivity;
import java.util.ArrayList;

import git.artdeell.skymodloader.SMLApplication;

/* renamed from: com.tgc.sky.ui.text.LocalizationManager */
public class LocalizationManager {
    private static int kMaxLocalizedStrings = 512;
    private GameActivity m_activity;
    private boolean m_enableHaptics = true;
    private boolean m_gamepad = false;
    private boolean m_invertCamera = false;
    private boolean m_invertFlight = false;
    private boolean m_leftHanded = false;
    /* access modifiers changed from: private */
    public ArrayList<LocalizedStringArgs> m_localizedStrings;
    private boolean m_proControls = false;
    private boolean m_touchControls = true;

    public native boolean FreeTextId(int i);

    public LocalizationManager(GameActivity gameActivity) {
        this.m_activity = gameActivity;
        this.m_localizedStrings = new ArrayList<>(kMaxLocalizedStrings);
        for (int i = 0; i < kMaxLocalizedStrings; i++) {
            this.m_localizedStrings.add(new LocalizedStringArgs());
        }
        this.m_localizedStrings.get(0).baseText = null;
        this.m_localizedStrings.get(0).localizedString = "";
        this.m_localizedStrings.get(0).args = null;
        this.m_localizedStrings.get(0).lastChangeCounter = -1;
        this.m_localizedStrings.get(0).compounded = null;
    }

    public void SetGameInputConfig(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7) {
        this.m_touchControls = z;
        this.m_proControls = z2;
        this.m_leftHanded = z3;
        this.m_gamepad = z4;
        this.m_invertFlight = z5;
        this.m_invertCamera = z6;
        this.m_enableHaptics = z7;
    }

    public String LocalizeString(String r10) {
        if(r10 == null) return null;
        int id = SMLApplication.skyRes.getIdentifier(r10, "string", SMLApplication.skyPName);
        if(id != 0) {
            return SMLApplication.skyRes.getString(id);
        }
        return r10;
    }

    public LocalizedStringArgs GetLocalizedStringArgs(int i) {
        int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        return (LocalizedStringArgs) (GetAllocatedTextIdx < kMaxLocalizedStrings ? this.m_localizedStrings.get(GetAllocatedTextIdx) : this.m_localizedStrings.get(0));
    }

    public void FreeLocalizedText(final int i) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx >= kMaxLocalizedStrings) {
            return;
        }
        if (this.m_localizedStrings.get(GetAllocatedTextIdx).compounded == null) {
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).baseText = null;
                    ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).localizedString = null;
                    ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).args = null;
                    ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).lastChangeCounter = 0;
                    ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).compounded = null;
                    LocalizationManager.this.FreeTextId(i);
                }
            });
            return;
        }
        for (int FreeLocalizedText : this.m_localizedStrings.get(GetAllocatedTextIdx).compounded) {
            FreeLocalizedText(FreeLocalizedText);
        }
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).baseText = null;
                ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).localizedString = null;
                ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).args = null;
                ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).lastChangeCounter = 0;
                ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).compounded = null;
                LocalizationManager.this.FreeTextId(i);
            }
        });
    }

    public void SetLocalizedText(int i, final String str) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx < kMaxLocalizedStrings) {
            if (this.m_localizedStrings.get(GetAllocatedTextIdx).args != null || this.m_localizedStrings.get(GetAllocatedTextIdx).compounded != null || this.m_localizedStrings.get(GetAllocatedTextIdx).baseText == null || !this.m_localizedStrings.get(GetAllocatedTextIdx).baseText.equals(str)) {
                this.m_activity.runOnUiThread(new Runnable() {
                    public void run() {
                        LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                        localizedStringArgs.baseText = str;
                        localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str);
                        localizedStringArgs.args = null;
                        localizedStringArgs.lastChangeCounter++;
                        localizedStringArgs.refresh = null;
                        localizedStringArgs.compounded = null;
                    }
                });
            }
        }
    }

    public void SetLocalizedTextWithArgs(int i, final String str, float f) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx < kMaxLocalizedStrings) {
            final String FormatFloat = FormatFloat(f);
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                    localizedStringArgs.baseText = str;
                    localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str);
                    localizedStringArgs.args = new ArrayList<Object>() {
                        {
                            add(FormatFloat);
                        }
                    };
                    localizedStringArgs.lastChangeCounter++;
                    localizedStringArgs.refresh = null;
                    localizedStringArgs.compounded = null;
                }
            });
        }
    }

    public void SetLocalizedTextWithArgs(int i, final String str, final String str2) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx < kMaxLocalizedStrings) {
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                    localizedStringArgs.baseText = str;
                    localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str);
                    localizedStringArgs.args = new ArrayList<Object>() {
                        {
                            add(str2 != null ? str2 : "");
                        }
                    };
                    localizedStringArgs.lastChangeCounter++;
                    localizedStringArgs.refresh = null;
                    localizedStringArgs.compounded = null;
                }
            });
        }
    }

    public void SetLocalizedTextWithArgs(int i, String str, float f, float f2) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx < kMaxLocalizedStrings) {
            final String FormatFloat = FormatFloat(f);
            final String FormatFloat2 = FormatFloat(f2);
            final String str2 = str;
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                    localizedStringArgs.baseText = str2;
                    localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str2);
                    localizedStringArgs.args = new ArrayList<Object>() {
                        {
                            add(FormatFloat);
                            add(FormatFloat2);
                        }
                    };
                    localizedStringArgs.lastChangeCounter++;
                    localizedStringArgs.refresh = null;
                    localizedStringArgs.compounded = null;
                }
            });
        }
    }

    public void SetLocalizedTextWithArgs(int i, String str, float f, String str2) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx < kMaxLocalizedStrings) {
            final String FormatFloat = FormatFloat(f);
            final String str3 = str;
            final String str4 = str2;
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                    localizedStringArgs.baseText = str3;
                    localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str3);
                    localizedStringArgs.args = new ArrayList<Object>() {
                        {
                            add(FormatFloat);
                            add(str4 != null ? str4 : "");
                        }
                    };
                    localizedStringArgs.lastChangeCounter++;
                    localizedStringArgs.refresh = null;
                    localizedStringArgs.compounded = null;
                }
            });
        }
    }

    public void SetLocalizedTextWithArgs(int i, String str, String str2, float f) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx < kMaxLocalizedStrings) {
            final String FormatFloat = FormatFloat(f);
            final String str3 = str;
            final String str4 = str2;
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                    localizedStringArgs.baseText = str3;
                    localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str3);
                    localizedStringArgs.args = new ArrayList<Object>() {
                        {
                            add(str4 != null ? str4 : "");
                            add(FormatFloat);
                        }
                    };
                    localizedStringArgs.lastChangeCounter++;
                    localizedStringArgs.refresh = null;
                    localizedStringArgs.compounded = null;
                }
            });
        }
    }

    public void SetLocalizedTextWithArgs(int i, String str, String str2, String str3) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx < kMaxLocalizedStrings) {
            final String str4 = str;
            final String str5 = str2;
            final String str6 = str3;
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                    localizedStringArgs.baseText = str4;
                    localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str4);
                    localizedStringArgs.args = new ArrayList<Object>() {
                        {
                            String str = "";
                            add(str5 != null ? str5 : str);
                            add(str6 != null ? str6 : str);
                        }
                    };
                    localizedStringArgs.lastChangeCounter++;
                    localizedStringArgs.refresh = null;
                    localizedStringArgs.compounded = null;
                }
            });
        }
    }

    public void SetLocalizedTextWithArgs(int i, String str, float f, float f2, float f3) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx < kMaxLocalizedStrings) {
            final String FormatFloat = FormatFloat(f);
            final String FormatFloat2 = FormatFloat(f2);
            final String FormatFloat3 = FormatFloat(f3);
            final String str2 = str;
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                    localizedStringArgs.baseText = str2;
                    localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str2);
                    localizedStringArgs.args = new ArrayList<Object>() {
                        {
                            add(FormatFloat);
                            add(FormatFloat2);
                            add(FormatFloat3);
                        }
                    };
                    localizedStringArgs.lastChangeCounter++;
                    localizedStringArgs.refresh = null;
                    localizedStringArgs.compounded = null;
                }
            });
        }
    }

    public void SetLocalizedTextWithArgs(int i, String str, String str2, String str3, String str4) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx < kMaxLocalizedStrings) {
            final String str5 = str;
            final String str6 = str2;
            final String str7 = str3;
            final String str8 = str4;
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                    localizedStringArgs.baseText = str5;
                    localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str5);
                    localizedStringArgs.args = new ArrayList<Object>() {
                        {
                            String str = "";
                            add(str6 != null ? str6 : str);
                            add(str7 != null ? str7 : str);
                            add(str8 != null ? str8 : str);
                        }
                    };
                    localizedStringArgs.lastChangeCounter++;
                    localizedStringArgs.refresh = null;
                    localizedStringArgs.compounded = null;
                }
            });
        }
    }

    public void SetLocalizedTextCompounded(int i, final int[] iArr) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx < kMaxLocalizedStrings) {
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).baseText = null;
                    ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).localizedString = null;
                    ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).args = null;
                    ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).lastChangeCounter++;
                    ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).compounded = iArr;
                }
            });
        }
    }

    public void SetPreLocalizedText(int i, final String str) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx < kMaxLocalizedStrings) {
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                    localizedStringArgs.baseText = null;
                    localizedStringArgs.localizedString = str;
                    localizedStringArgs.args = null;
                    localizedStringArgs.lastChangeCounter++;
                    localizedStringArgs.refresh = null;
                    localizedStringArgs.compounded = null;
                }
            });
        }
    }

    private int GetAllocatedTextIdx(int i) {
        return i != -1 ? i & 511 : kMaxLocalizedStrings;
    }

    private String FormatFloat(float f) {
        long j = (long) f;
        if (f == ((float) j)) {
            return Long.toString(j);
        }
        return Double.toString((double) f);
    }
}
