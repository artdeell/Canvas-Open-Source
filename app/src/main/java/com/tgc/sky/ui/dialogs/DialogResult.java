package com.tgc.sky.ui.dialogs;

/* renamed from: com.tgc.sky.ui.dialogs.DialogResult */
public class DialogResult {
    public int option = 0;
    public Response response = Response.kInvalid;
    public String stringBuffer;

    /* renamed from: com.tgc.sky.ui.dialogs.DialogResult$Response */
    public enum Response {
        kInvalid,
        kWaiting,
        kResponded,
        kClosed
    }
}
