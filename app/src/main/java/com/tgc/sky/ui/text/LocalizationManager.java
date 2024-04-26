package com.tgc.sky.ui.text;

import android.annotation.SuppressLint;

import androidx.constraintlayout.core.motion.utils.TypedValues;
import androidx.core.app.FrameMetricsAggregator;
import com.tgc.sky.GameActivity;
import java.util.ArrayList;

import git.artdeell.skymodloader.SMLApplication;

/* loaded from: classes2.dex */
public class LocalizationManager {
    private static int kMaxLocalizedStrings = 512;
    private GameActivity m_activity;
    private boolean m_touchControls = true;
    private boolean m_proControls = false;
    private boolean m_leftHanded = false;
    private boolean m_gamepad = false;
    private boolean m_invertFlight = false;
    private boolean m_invertCamera = false;
    private boolean m_enableHaptics = true;
    private ArrayList<LocalizedStringArgs> m_localizedStrings = new ArrayList<>(kMaxLocalizedStrings);

    public native boolean FreeTextId(int i);

    public LocalizationManager(GameActivity gameActivity) {
        this.m_activity = gameActivity;
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

    /* JADX WARN: Removed duplicated region for block: B:25:0x0059  */
    /* JADX WARN: Removed duplicated region for block: B:38:0x0077 A[EDGE_INSN: B:38:0x0077->B:33:0x0077 ?: BREAK  , SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.lang.String LocalizeString(java.lang.String r9) {
        /*
            r8 = this;
            r0 = 0
            if (r9 != 0) goto L4
            return r0
        L4:
            java.lang.String r1 = "tutorial_"
            boolean r1 = r9.startsWith(r1)
            java.lang.String r2 = "string"
            if (r1 == 0) goto L77
            java.lang.String r1 = "!"
            boolean r1 = r9.endsWith(r1)
            r3 = 1
            r4 = 0
            if (r1 == 0) goto L23
            int r1 = r9.length()
            int r1 = r1 - r3
            java.lang.String r9 = r9.substring(r4, r1)
        L21:
            r1 = r0
            goto L56
        L23:
            boolean r1 = r8.m_gamepad
            if (r1 == 0) goto L35
            java.lang.Object[] r1 = new java.lang.Object[r3]
            r1[r4] = r9
            java.lang.String r3 = "%s_ps"
            java.lang.String r1 = java.lang.String.format(r3, r1)
        L31:
            r7 = r1
            r1 = r0
            r0 = r7
            goto L56
        L35:
            boolean r1 = r8.m_touchControls
            if (r1 == 0) goto L21
            boolean r1 = r8.m_proControls
            if (r1 == 0) goto L21
            boolean r1 = r8.m_leftHanded
            if (r1 == 0) goto L4b
            java.lang.Object[] r0 = new java.lang.Object[r3]
            r0[r4] = r9
            java.lang.String r1 = "%s_pro_lh"
            java.lang.String r0 = java.lang.String.format(r1, r0)
        L4b:
            java.lang.Object[] r1 = new java.lang.Object[r3]
            r1[r4] = r9
            java.lang.String r3 = "%s_pro"
            java.lang.String r1 = java.lang.String.format(r3, r1)
            goto L31
        L56:
            r3 = 2
            if (r4 >= r3) goto L77
            if (r4 != 0) goto L5d
            r3 = r1
            goto L5e
        L5d:
            r3 = r0
        L5e:
            if (r3 == 0) goto L74
            com.tgc.sky.GameActivity r5 = r8.m_activity
            android.content.res.Resources r5 = r5.getResources()
            com.tgc.sky.GameActivity r6 = r8.m_activity
            java.lang.String r6 = r6.getPackageName()
            int r5 = r5.getIdentifier(r3, r2, r6)
            if (r5 == 0) goto L74
            r9 = r3
            goto L77
        L74:
            int r4 = r4 + 1
            goto L56
        L77:
            com.tgc.sky.GameActivity r0 = r8.m_activity
            android.content.res.Resources r0 = r0.getResources()
            com.tgc.sky.GameActivity r1 = r8.m_activity
            java.lang.String r1 = r1.getPackageName()
            int r0 = r0.getIdentifier(r9, r2, r1)
            if (r0 == 0) goto L8f
            com.tgc.sky.GameActivity r9 = r8.m_activity
            java.lang.String r9 = r9.getString(r0)
        L8f:
            return r9
        */
        if(r9 == null) return null;
        @SuppressLint("DiscouragedApi") int id = SMLApplication.SkyResources.getIdentifier(r9, "string", SMLApplication.skyPName);
        if(id != 0) {
            return SMLApplication.SkyResources.getString(id);
        }
        return r9;
        //throw new UnsupportedOperationException("Method not decompiled: com.tgc.sky.ui.text.LocalizationManager.LocalizeString(java.lang.String):java.lang.String");
    }

    @SuppressLint("DiscouragedApi")
    public boolean HasLocalizedString(String str) {
        return str != null && this.m_activity.getResources().getIdentifier(str, TypedValues.Custom.S_STRING, this.m_activity.getPackageName()) != 0;
    }

    public LocalizedStringArgs GetLocalizedStringArgs(int i) {
        int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        return GetAllocatedTextIdx < kMaxLocalizedStrings ? this.m_localizedStrings.get(GetAllocatedTextIdx) : this.m_localizedStrings.get(0);
    }

    public void FreeLocalizedText(final int i) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx < kMaxLocalizedStrings) {
            if (this.m_localizedStrings.get(GetAllocatedTextIdx).compounded == null) {
                this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.text.LocalizationManager.1
                    @Override // java.lang.Runnable
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
            for (int i2 : this.m_localizedStrings.get(GetAllocatedTextIdx).compounded) {
                FreeLocalizedText(i2);
            }
            this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.text.LocalizationManager.2
                @Override // java.lang.Runnable
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
    }

    public void SetLocalizedText(int i, final String str) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx >= kMaxLocalizedStrings) {
            return;
        }
        if (this.m_localizedStrings.get(GetAllocatedTextIdx).args == null && this.m_localizedStrings.get(GetAllocatedTextIdx).compounded == null && this.m_localizedStrings.get(GetAllocatedTextIdx).baseText != null && this.m_localizedStrings.get(GetAllocatedTextIdx).baseText.equals(str)) {
            return;
        }
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.text.LocalizationManager.3
            @Override // java.lang.Runnable
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

    public void SetLocalizedTextWithArgs(int i, final String str, float f) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx >= kMaxLocalizedStrings) {
            return;
        }
        final String FormatFloat = FormatFloat(f);
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.text.LocalizationManager.4
            @Override // java.lang.Runnable
            public void run() {
                LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                localizedStringArgs.baseText = str;
                localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str);
                localizedStringArgs.args = new ArrayList<Object>() { // from class: com.tgc.sky.ui.text.LocalizationManager.4.1
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

    public void SetLocalizedTextWithArgs(int i, final String str, final String str2) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx >= kMaxLocalizedStrings) {
            return;
        }
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.text.LocalizationManager.5
            @Override // java.lang.Runnable
            public void run() {
                LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                localizedStringArgs.baseText = str;
                localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str);
                localizedStringArgs.args = new ArrayList<Object>() { // from class: com.tgc.sky.ui.text.LocalizationManager.5.1
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

    public void SetLocalizedTextWithArgs(int i, final String str, float f, float f2) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx >= kMaxLocalizedStrings) {
            return;
        }
        final String FormatFloat = FormatFloat(f);
        final String FormatFloat2 = FormatFloat(f2);
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.text.LocalizationManager.6
            @Override // java.lang.Runnable
            public void run() {
                LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                localizedStringArgs.baseText = str;
                localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str);
                localizedStringArgs.args = new ArrayList<Object>() { // from class: com.tgc.sky.ui.text.LocalizationManager.6.1
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

    public void SetLocalizedTextWithArgs(int i, final String str, float f, final String str2) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx >= kMaxLocalizedStrings) {
            return;
        }
        final String FormatFloat = FormatFloat(f);
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.text.LocalizationManager.7
            @Override // java.lang.Runnable
            public void run() {
                LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                localizedStringArgs.baseText = str;
                localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str);
                localizedStringArgs.args = new ArrayList<Object>() { // from class: com.tgc.sky.ui.text.LocalizationManager.7.1
                    {
                        add(FormatFloat);
                        add(str2 != null ? str2 : "");
                    }
                };
                localizedStringArgs.lastChangeCounter++;
                localizedStringArgs.refresh = null;
                localizedStringArgs.compounded = null;
            }
        });
    }

    public void SetLocalizedTextWithArgs(int i, final String str, final String str2, float f) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx >= kMaxLocalizedStrings) {
            return;
        }
        final String FormatFloat = FormatFloat(f);
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.text.LocalizationManager.8
            @Override // java.lang.Runnable
            public void run() {
                LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                localizedStringArgs.baseText = str;
                localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str);
                localizedStringArgs.args = new ArrayList<Object>() { // from class: com.tgc.sky.ui.text.LocalizationManager.8.1
                    {
                        add(str2 != null ? str2 : "");
                        add(FormatFloat);
                    }
                };
                localizedStringArgs.lastChangeCounter++;
                localizedStringArgs.refresh = null;
                localizedStringArgs.compounded = null;
            }
        });
    }

    public void SetLocalizedTextWithArgs(int i, final String str, final String str2, final String str3) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx >= kMaxLocalizedStrings) {
            return;
        }
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.text.LocalizationManager.9
            @Override // java.lang.Runnable
            public void run() {
                LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                localizedStringArgs.baseText = str;
                localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str);
                localizedStringArgs.args = new ArrayList<Object>() { // from class: com.tgc.sky.ui.text.LocalizationManager.9.1
                    {
                        add(str2 != null ? str2 : "");
                        add(str3 != null ? str3 : "");
                    }
                };
                localizedStringArgs.lastChangeCounter++;
                localizedStringArgs.refresh = null;
                localizedStringArgs.compounded = null;
            }
        });
    }

    public void SetLocalizedTextWithArgs(int i, final String str, float f, float f2, float f3) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx >= kMaxLocalizedStrings) {
            return;
        }
        final String FormatFloat = FormatFloat(f);
        final String FormatFloat2 = FormatFloat(f2);
        final String FormatFloat3 = FormatFloat(f3);
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.text.LocalizationManager.10
            @Override // java.lang.Runnable
            public void run() {
                LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                localizedStringArgs.baseText = str;
                localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str);
                localizedStringArgs.args = new ArrayList<Object>() { // from class: com.tgc.sky.ui.text.LocalizationManager.10.1
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

    public void SetLocalizedTextWithArgs(int i, final String str, final String str2, final String str3, final String str4) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx >= kMaxLocalizedStrings) {
            return;
        }
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.text.LocalizationManager.11
            @Override // java.lang.Runnable
            public void run() {
                LocalizedStringArgs localizedStringArgs = (LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx);
                localizedStringArgs.baseText = str;
                localizedStringArgs.localizedString = LocalizationManager.this.LocalizeString(str);
                localizedStringArgs.args = new ArrayList<Object>() { // from class: com.tgc.sky.ui.text.LocalizationManager.11.1
                    {
                        add(str2 != null ? str2 : "");
                        add(str3 != null ? str3 : "");
                        add(str4 != null ? str4 : "");
                    }
                };
                localizedStringArgs.lastChangeCounter++;
                localizedStringArgs.refresh = null;
                localizedStringArgs.compounded = null;
            }
        });
    }

    public void SetLocalizedTextCompounded(int i, final int[] iArr) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx >= kMaxLocalizedStrings) {
            return;
        }
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.text.LocalizationManager.12
            @Override // java.lang.Runnable
            public void run() {
                ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).baseText = null;
                ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).localizedString = null;
                ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).args = null;
                ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).lastChangeCounter++;
                ((LocalizedStringArgs) LocalizationManager.this.m_localizedStrings.get(GetAllocatedTextIdx)).compounded = iArr;
            }
        });
    }

    public void SetPreLocalizedText(int i, final String str) {
        final int GetAllocatedTextIdx = GetAllocatedTextIdx(i);
        if (GetAllocatedTextIdx >= kMaxLocalizedStrings) {
            return;
        }
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.text.LocalizationManager.13
            @Override // java.lang.Runnable
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

    private int GetAllocatedTextIdx(int i) {
        return i != -1 ? i & FrameMetricsAggregator.EVERY_DURATION : kMaxLocalizedStrings;
    }

    private String FormatFloat(float f) {
        long j = (long) f;
        if (f == ((float) j)) {
            return Long.toString(j);
        }
        return Double.toString(f);
    }
}
