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

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0066  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0084 A[EDGE_INSN: B:38:0x0084->B:34:0x0084 ?: BREAK  , SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String LocalizeString(java.lang.String r10) {
        /*
            r9 = this;
            r0 = 0
            if (r10 != 0) goto L_0x0004
            return r0
        L_0x0004:
            java.lang.String r1 = "dev:"
            boolean r1 = r10.startsWith(r1)
            if (r1 == 0) goto L_0x0011
            r2 = 4
            java.lang.String r10 = r10.substring(r2)
        L_0x0011:
            java.lang.String r2 = "tutorial_"
            boolean r2 = r10.startsWith(r2)
            java.lang.String r3 = "string"
            if (r2 == 0) goto L_0x0084
            java.lang.String r2 = "!"
            boolean r2 = r10.endsWith(r2)
            r4 = 1
            r5 = 0
            if (r2 == 0) goto L_0x0030
            int r2 = r10.length()
            int r2 = r2 - r4
            java.lang.String r10 = r10.substring(r5, r2)
        L_0x002e:
            r2 = r0
            goto L_0x0063
        L_0x0030:
            boolean r2 = r9.m_gamepad
            if (r2 == 0) goto L_0x0042
            java.lang.Object[] r2 = new java.lang.Object[r4]
            r2[r5] = r10
            java.lang.String r4 = "%s_ps"
            java.lang.String r2 = java.lang.String.format(r4, r2)
        L_0x003e:
            r8 = r2
            r2 = r0
            r0 = r8
            goto L_0x0063
        L_0x0042:
            boolean r2 = r9.m_touchControls
            if (r2 == 0) goto L_0x002e
            boolean r2 = r9.m_proControls
            if (r2 == 0) goto L_0x002e
            boolean r2 = r9.m_leftHanded
            if (r2 == 0) goto L_0x0058
            java.lang.Object[] r0 = new java.lang.Object[r4]
            r0[r5] = r10
            java.lang.String r2 = "%s_pro_lh"
            java.lang.String r0 = java.lang.String.format(r2, r0)
        L_0x0058:
            java.lang.Object[] r2 = new java.lang.Object[r4]
            r2[r5] = r10
            java.lang.String r4 = "%s_pro"
            java.lang.String r2 = java.lang.String.format(r4, r2)
            goto L_0x003e
        L_0x0063:
            r4 = 2
            if (r5 >= r4) goto L_0x0084
            if (r5 != 0) goto L_0x006a
            r4 = r2
            goto L_0x006b
        L_0x006a:
            r4 = r0
        L_0x006b:
            if (r4 == 0) goto L_0x0081
            com.tgc.sky.GameActivity r6 = r9.m_activity
            android.content.res.Resources r6 = r6.getResources()
            com.tgc.sky.GameActivity r7 = r9.m_activity
            java.lang.String r7 = r7.getPackageName()
            int r6 = r6.getIdentifier(r4, r3, r7)
            if (r6 == 0) goto L_0x0081
            r10 = r4
            goto L_0x0084
        L_0x0081:
            int r5 = r5 + 1
            goto L_0x0063
        L_0x0084:
            if (r1 != 0) goto L_0x009e
            com.tgc.sky.GameActivity r0 = r9.m_activity
            android.content.res.Resources r0 = r0.getResources()
            com.tgc.sky.GameActivity r1 = r9.m_activity
            java.lang.String r1 = r1.getPackageName()
            int r0 = r0.getIdentifier(r10, r3, r1)
            if (r0 == 0) goto L_0x009e
            com.tgc.sky.GameActivity r10 = r9.m_activity
            java.lang.String r10 = r10.getString(r0)
        L_0x009e:
            return r10
        */
        if(r10 == null) return null;
        int id = SMLApplication.skyRes.getIdentifier(r10, "string", SMLApplication.skyPName);
        if(id != 0) {
            return SMLApplication.skyRes.getString(id);
        }
        return r10;
        //throw new UnsupportedOperationException("Method not decompiled: com.tgc.sky.p018ui.text.LocalizationManager.LocalizeString(java.lang.String):java.lang.String");
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
