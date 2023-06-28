package com.tgc.sky.io;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.text.SpannableStringBuilder;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.tgc.sky.GameActivity;
import com.tgc.sky.SystemUI_android;
import com.tgc.sky.ui.Utils;
import java.io.IOException;

/* renamed from: com.tgc.sky.io.NFCSessionManager */
public class NFCSessionManager implements GameActivity.OnActivityIntentListener {
    /* access modifiers changed from: private */
    public GameActivity m_activity;
    private NfcAdapter m_adapter;
    /* access modifiers changed from: private */
    public AlertDialog m_alert;
    private IntentFilter[] m_intentFiltersArrayForReading;
    private IntentFilter[] m_intentFiltersArrayForWriting;
    private PendingIntent m_pendingIntent;
    private boolean m_scanMode;
    private boolean m_sessionActive;
    private String[][] m_techListsArray;
    private String m_url;

    public NFCSessionManager(GameActivity gameActivity) {
        this.m_activity = gameActivity;
        NfcAdapter defaultAdapter = NfcAdapter.getDefaultAdapter(gameActivity);
        this.m_adapter = defaultAdapter;
        if (defaultAdapter != null) {
            GameActivity gameActivity2 = this.m_activity;
            GameActivity gameActivity3 = this.m_activity;
            this.m_pendingIntent = PendingIntent.getActivity(gameActivity2, 0, new Intent(gameActivity3, gameActivity3.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
            IntentFilter intentFilter = new IntentFilter("android.nfc.action.NDEF_DISCOVERED");
            intentFilter.addDataScheme("https");
            intentFilter.addDataSchemeSpecificPart("//sky.thatg.co/?s=", 1);
            this.m_intentFiltersArrayForReading = new IntentFilter[]{intentFilter};
            IntentFilter intentFilter2 = new IntentFilter("android.nfc.action.NDEF_DISCOVERED");
            try {
                intentFilter2.addDataType("*/*");
            } catch (IntentFilter.MalformedMimeTypeException unused) {
            }
            this.m_intentFiltersArrayForWriting = new IntentFilter[]{intentFilter2};
            this.m_techListsArray = new String[][]{new String[]{NfcA.class.getName()}};
            this.m_sessionActive = false;
        }
    }

    public boolean SupportsForegroundScanning() {
        return this.m_adapter != null;
    }

    public boolean SupportsBackgroundScanning() {
        NfcAdapter nfcAdapter = this.m_adapter;
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }

    public boolean ScanNFCTag() {
        if (!SupportsForegroundScanning()) {
            return false;
        }
        this.m_scanMode = true;
        this.m_url = null;
        StartSession("star_tag_scanner_scan_title", this.m_intentFiltersArrayForReading);
        return true;
    }

    public boolean ReadNFCTag() {
        if (!SupportsForegroundScanning()) {
            return false;
        }
        this.m_scanMode = false;
        this.m_url = null;
        StartSession("star_tag_scanner_bind_title", this.m_intentFiltersArrayForReading);
        return true;
    }

    public boolean WriteNFCTag(String str) {
        if (!SupportsForegroundScanning()) {
            return false;
        }
        this.m_scanMode = false;
        this.m_url = str;
        StartSession("star_tag_scanner_write_title", this.m_intentFiltersArrayForWriting);
        return true;
    }

    public boolean onNewIntent(Intent intent) {
        if (this.m_scanMode) {
            this.m_activity.onOpenedWithURLNative(intent.getDataString(), true);
        } else {
            Tag tag = (Tag) intent.getParcelableExtra("android.nfc.extra.TAG");
            if (tag != null) {
                NfcA nfcA = NfcA.get(tag);
                if (nfcA != null) {
                    try {
                        nfcA.connect();
                        byte[] id = tag.getId();
                        byte[] transceive = nfcA.transceive(new byte[]{57, 2});
                        byte[] transceive2 = nfcA.transceive(new byte[]{60, 0});
                        nfcA.close();
                        this.m_activity.onNFCTagScannedNative(byteArrayToHexString(id), byteArrayToInteger(transceive), byteArrayToHexString(transceive2), intent.getDataString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void onResume() {
        StopSession();
    }

    private void StartSession(String str, IntentFilter[] intentFilterArr) {
        if (this.m_sessionActive) {
            return;
        }
        if (!this.m_adapter.isEnabled()) {
            StartPermissionDialog();
            return;
        }
        StartSessionDialog(str);
        this.m_activity.AddOnActivityIntentListener(this);
        this.m_adapter.enableForegroundDispatch(this.m_activity, this.m_pendingIntent, intentFilterArr, this.m_techListsArray);
        this.m_sessionActive = true;
    }

    /* access modifiers changed from: private */
    public void StopSession() {
        if (this.m_sessionActive) {
            this.m_adapter.disableForegroundDispatch(this.m_activity);
            this.m_activity.RemoveOnActivityIntentListeners(this);
            this.m_sessionActive = false;
        }
        if (this.m_alert != null) {
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    if (NFCSessionManager.this.m_alert != null) {
                        NFCSessionManager.this.m_alert.dismiss();
                        AlertDialog unused = NFCSessionManager.this.m_alert = null;
                    }
                }
            });
        }
    }

    private void StartPermissionDialog() {
        SystemUI_android instance = SystemUI_android.getInstance();
        String LocalizeString = instance.LocalizeString("star_tag_scanner_settings_title");
        String LocalizeString2 = instance.LocalizeString("star_tag_scanner_settings_message");
        final String LocalizeString3 = instance.LocalizeString("system_button_settings");
        final String LocalizeString4 = instance.LocalizeString("system_button_cancel");
        final SpannableStringBuilder GetMarkedUpString = instance.GetMarkedUpString(LocalizeString, instance.DefaultMarkupWithBoldFontSize(26.0f), false);
        final SpannableStringBuilder GetMarkedUpString2 = instance.GetMarkedUpString(LocalizeString2, instance.DefaultMarkupWithFontSize(20.0f), false);
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(NFCSessionManager.this.m_activity);
                TextView textView = new TextView(NFCSessionManager.this.m_activity);
                textView.setText(GetMarkedUpString, TextView.BufferType.SPANNABLE);
                int dp2px = Utils.dp2px(20.0f);
                FrameLayout frameLayout = new FrameLayout(NFCSessionManager.this.m_activity);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
                layoutParams.leftMargin = dp2px;
                layoutParams.rightMargin = dp2px;
                layoutParams.topMargin = dp2px;
                frameLayout.addView(textView, layoutParams);
                builder.setCustomTitle(frameLayout);
                TextView textView2 = new TextView(NFCSessionManager.this.m_activity);
                textView2.setText(GetMarkedUpString2, TextView.BufferType.SPANNABLE);
                FrameLayout frameLayout2 = new FrameLayout(NFCSessionManager.this.m_activity);
                FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-1, -2);
                layoutParams2.leftMargin = dp2px;
                layoutParams2.rightMargin = dp2px;
                layoutParams2.topMargin = dp2px;
                frameLayout2.addView(textView2, layoutParams2);
                builder.setView(frameLayout2);
                builder.setPositiveButton(LocalizeString3, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NFCSessionManager.this.m_activity.startActivity(new Intent("android.settings.NFC_SETTINGS"));
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton(LocalizeString4, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog unused = NFCSessionManager.this.m_alert = builder.create();
                GameActivity.hideNavigationFullScreen(NFCSessionManager.this.m_alert.getWindow().getDecorView());
                NFCSessionManager.this.m_alert.getWindow().setFlags(8, 8);
                NFCSessionManager.this.m_alert.setCanceledOnTouchOutside(false);
                NFCSessionManager.this.m_alert.show();
                NFCSessionManager.this.m_alert.getWindow().clearFlags(8);
                NFCSessionManager.this.m_alert.getButton(-1).setTextColor(-16776961);
            }
        });
    }

    private void StartSessionDialog(String str) {
        SystemUI_android instance = SystemUI_android.getInstance();
        String LocalizeString = instance.LocalizeString(str);
        String LocalizeString2 = instance.LocalizeString("star_tag_scanner_message");
        final String LocalizeString3 = instance.LocalizeString("system_button_cancel");
        final SpannableStringBuilder GetMarkedUpString = instance.GetMarkedUpString(LocalizeString, instance.DefaultMarkupWithBoldFontSize(26.0f), false);
        final SpannableStringBuilder GetMarkedUpString2 = instance.GetMarkedUpString(LocalizeString2, instance.DefaultMarkupWithFontSize(20.0f), false);
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(NFCSessionManager.this.m_activity);
                TextView textView = new TextView(NFCSessionManager.this.m_activity);
                textView.setText(GetMarkedUpString, TextView.BufferType.SPANNABLE);
                int dp2px = Utils.dp2px(20.0f);
                FrameLayout frameLayout = new FrameLayout(NFCSessionManager.this.m_activity);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
                layoutParams.leftMargin = dp2px;
                layoutParams.rightMargin = dp2px;
                layoutParams.topMargin = dp2px;
                frameLayout.addView(textView, layoutParams);
                builder.setCustomTitle(frameLayout);
                TextView textView2 = new TextView(NFCSessionManager.this.m_activity);
                textView2.setText(GetMarkedUpString2, TextView.BufferType.SPANNABLE);
                FrameLayout frameLayout2 = new FrameLayout(NFCSessionManager.this.m_activity);
                FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-1, -2);
                layoutParams2.leftMargin = dp2px;
                layoutParams2.rightMargin = dp2px;
                layoutParams2.topMargin = dp2px;
                frameLayout2.addView(textView2, layoutParams2);
                builder.setView(frameLayout2);
                builder.setPositiveButton(LocalizeString3, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog unused = NFCSessionManager.this.m_alert = null;
                        NFCSessionManager.this.StopSession();
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog unused = NFCSessionManager.this.m_alert = builder.create();
                GameActivity.hideNavigationFullScreen(NFCSessionManager.this.m_alert.getWindow().getDecorView());
                NFCSessionManager.this.m_alert.getWindow().setFlags(8, 8);
                NFCSessionManager.this.m_alert.setCanceledOnTouchOutside(false);
                NFCSessionManager.this.m_alert.show();
                NFCSessionManager.this.m_alert.getWindow().clearFlags(8);
                NFCSessionManager.this.m_alert.getButton(-1).setTextColor(-16776961);
            }
        });
    }

    public static String byteArrayToHexString(byte[] bArr) {
        StringBuilder sb = new StringBuilder(bArr.length * 2);
        int length = bArr.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", new Object[]{Byte.valueOf(bArr[i])}));
        }
        return sb.toString();
    }

    public static int byteArrayToInteger(byte[] bArr) {
        return ((bArr[2] & 255) << 16) | (bArr[0] & 255) | ((bArr[1] & 255) << 8);
    }
}
