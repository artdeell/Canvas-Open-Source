package com.tgc.sky.ui.text;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import com.tgc.sky.GameActivity;
import com.tgc.sky.ui.NtRange;
import com.tgc.sky.ui.Utils;
import com.tgc.sky.ui.spans.CustomTypefaceSpan;
import com.tgc.sky.ui.spans.EmbeddedImageSpan;
import com.tgc.sky.ui.spans.ShadowSpan;
import com.tgc.sky.ui.spans.StrokeSpan;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import git.artdeell.skymodloader.R;
import git.artdeell.skymodloader.SMLApplication;

/* renamed from: com.tgc.sky.ui.text.Markup */
public class Markup {
    private final int adventurePassColor = Color.argb(255, 255, 128, 51);
    private Drawable attachmentBeacon;
    private Drawable attachmentCamera;
    private Drawable attachmentCandle;
    private Drawable attachmentCape;
    private Drawable attachmentCog;
    private Drawable attachmentDiamond;
    private Drawable attachmentExclamation;
    private Drawable attachmentGamepadA;
    private Drawable attachmentGamepadB;
    private Drawable attachmentGamepadX;
    private Drawable attachmentGamepadY;
    private Drawable attachmentHeart;
    private Drawable attachmentInvite;
    private Drawable attachmentLeftShoulder;
    private Drawable attachmentLeftTrigger;
    private Drawable attachmentLightBulb;
    private Drawable attachmentPrestige;
    private Drawable attachmentQuestion;
    private Drawable attachmentRightShoulder;
    private Drawable attachmentRightTrigger;
    private Map<String, Drawable> attachmentSeasonCandles;
    private Map<String, Drawable> attachmentSeasonHearts;
    private Map<String, Drawable> attachmentSeasonMasks;
    private Map<String, Drawable> attachmentSeasonPendants;
    private Drawable attachmentSeasonQuest;
    private Drawable attachmentSit;
    private Drawable attachmentSpellEarth;
    private Drawable attachmentSpellFire;
    private Drawable attachmentSpellMind;
    private Drawable attachmentSpellVoid;
    private Drawable attachmentSpellWater;
    private Drawable attachmentSpellWind;
    private Drawable attachmentStar;
    private Drawable attachmentSupport;
    private Drawable attachmentThumbstick;
    private Drawable attachmentToggleDive;
    private Drawable attachmentToggleFly;
    private Drawable attachmentToggleHover;
    private Drawable attachmentToggleSwim;
    private Drawable attachmentWingBuff;
    private GameActivity m_activity;
    private Map<String, String> m_buttonMap;
    private Typeface m_defaultFont;
    private final int menuGold = GetColor(1.0f, 0.9686f, 0.851f, 1.0f);
    private final int menuWhite = GetColor(1.0f, 1.0f, 1.0f, 1.0f);
    private final int shadowColor = GetColor(0.0f, 0.0f, 0.0f, 0.5f);

    public Markup(GameActivity gameActivity) {
        this.m_activity = gameActivity;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            m_defaultFont = gameActivity.getResources().getFont(R.font.lato);
        }
        this.m_buttonMap = new HashMap(16);
        this.attachmentBeacon = CreateAttachment("systemui_beacon");
        this.attachmentCamera = CreateAttachment("systemui_camera");
        this.attachmentCandle = CreateAttachment("systemui_candle");
        this.attachmentCape = CreateAttachment("systemui_cape");
        this.attachmentCog = CreateAttachment("systemui_cog");
        this.attachmentDiamond = CreateAttachment("systemui_diamond");
        this.attachmentExclamation = CreateAttachment("systemui_exclamation");
        this.attachmentGamepadA = CreateAttachment("systemui_gamepada");
        this.attachmentGamepadB = CreateAttachment("systemui_gamepadb");
        this.attachmentGamepadX = CreateAttachment("systemui_gamepadx");
        this.attachmentGamepadY = CreateAttachment("systemui_gamepady");
        this.attachmentHeart = CreateAttachment("systemui_heart");
        this.attachmentInvite = CreateAttachment("systemui_invite");
        this.attachmentLeftShoulder = CreateAttachment("systemui_leftshoulder");
        this.attachmentLeftTrigger = CreateAttachment("systemui_lefttrigger");
        this.attachmentLightBulb = CreateAttachment("systemui_lightbulb");
        this.attachmentPrestige = CreateAttachment("systemui_prestige");
        this.attachmentQuestion = CreateAttachment("systemui_question");
        this.attachmentRightShoulder = CreateAttachment("systemui_rightshoulder");
        this.attachmentRightTrigger = CreateAttachment("systemui_righttrigger");
        HashMap hashMap = new HashMap();
        this.attachmentSeasonCandles = hashMap;
        hashMap.put("1", CreateAttachment("systemui_seasoncandle01"));
        this.attachmentSeasonCandles.put("2", CreateAttachment("systemui_seasoncandle02"));
        this.attachmentSeasonCandles.put("3", CreateAttachment("systemui_seasoncandle03"));
        this.attachmentSeasonCandles.put("4", CreateAttachment("systemui_seasoncandle04"));
        this.attachmentSeasonCandles.put("5", CreateAttachment("systemui_seasoncandle01"));
        this.attachmentSeasonCandles.put("6", CreateAttachment("systemui_seasoncandle06"));
        this.attachmentSeasonCandles.put("7", CreateAttachment("systemui_seasoncandle07"));
        this.attachmentSeasonCandles.put("8", CreateAttachment("systemui_seasoncandle08"));
        this.attachmentSeasonCandles.put("9", CreateAttachment("systemui_seasoncandle09"));
        this.attachmentSeasonCandles.put("10", CreateAttachment("systemui_seasoncandle10"));
        this.attachmentSeasonCandles.put("11", CreateAttachment("systemui_seasoncandle11"));
        this.attachmentSeasonCandles.put("12", CreateAttachment("systemui_seasoncandle12"));
        this.attachmentSeasonCandles.put("13", CreateAttachment("systemui_seasoncandle13"));
        Object obj = "13";
        this.attachmentSeasonCandles.put("14", CreateAttachment("systemui_seasoncandle14"));
        Object obj2 = "14";
        this.attachmentSeasonCandles.put("default", CreateAttachment("systemui_seasoncandle14"));
        HashMap hashMap2 = new HashMap();
        this.attachmentSeasonHearts = hashMap2;
        hashMap2.put("1", CreateAttachment("systemui_seasonheart01"));
        this.attachmentSeasonHearts.put("2", CreateAttachment("systemui_seasonheart02"));
        this.attachmentSeasonHearts.put("3", CreateAttachment("systemui_seasonheart03"));
        this.attachmentSeasonHearts.put("4", CreateAttachment("systemui_seasonheart04"));
        this.attachmentSeasonHearts.put("5", CreateAttachment("systemui_seasonheart01"));
        this.attachmentSeasonHearts.put("6", CreateAttachment("systemui_seasonheart06"));
        this.attachmentSeasonHearts.put("7", CreateAttachment("systemui_seasonheart07"));
        this.attachmentSeasonHearts.put("8", CreateAttachment("systemui_seasonheart08"));
        this.attachmentSeasonHearts.put("9", CreateAttachment("systemui_seasonheart09"));
        this.attachmentSeasonHearts.put("10", CreateAttachment("systemui_seasonheart10"));
        this.attachmentSeasonHearts.put("11", CreateAttachment("systemui_seasonheart11"));
        this.attachmentSeasonHearts.put("12", CreateAttachment("systemui_seasonheart12"));
        this.attachmentSeasonHearts.put((String) obj, CreateAttachment("systemui_seasonheart13"));
        this.attachmentSeasonHearts.put((String) obj2, CreateAttachment("systemui_seasonheart14"));
        Object obj3 = "default";
        this.attachmentSeasonHearts.put((String) obj3, CreateAttachment("systemui_seasonheart14"));
        HashMap hashMap3 = new HashMap();
        this.attachmentSeasonMasks = hashMap3;
        hashMap3.put("1", CreateAttachment("systemui_seasonmask05"));
        this.attachmentSeasonMasks.put("2", CreateAttachment("systemui_seasonmask02"));
        this.attachmentSeasonMasks.put("3", CreateAttachment("systemui_seasonmask03"));
        this.attachmentSeasonMasks.put("4", CreateAttachment("systemui_seasonmask04"));
        this.attachmentSeasonMasks.put("5", CreateAttachment("systemui_seasonmask05"));
        this.attachmentSeasonMasks.put("6", CreateAttachment("systemui_seasonmask06"));
        this.attachmentSeasonMasks.put("7", CreateAttachment("systemui_seasonmask07"));
        this.attachmentSeasonMasks.put("8", CreateAttachment("systemui_seasonmask07"));
        this.attachmentSeasonMasks.put("9", CreateAttachment("systemui_seasonmask09"));
        this.attachmentSeasonMasks.put("10", CreateAttachment("systemui_seasonmask10"));
        this.attachmentSeasonMasks.put("11", CreateAttachment("systemui_seasonmask11"));
        this.attachmentSeasonMasks.put("12", CreateAttachment("systemui_seasonmask12"));
        this.attachmentSeasonMasks.put((String) obj, CreateAttachment("systemui_seasonmask13"));
        Object obj4 = obj2;
        this.attachmentSeasonMasks.put((String) obj4, CreateAttachment("systemui_seasonmask14"));
        this.attachmentSeasonMasks.put((String) obj3, CreateAttachment("systemui_seasonmask14"));
        HashMap hashMap4 = new HashMap();
        this.attachmentSeasonPendants = hashMap4;
        hashMap4.put("1", CreateAttachment("systemui_seasonpendant05"));
        this.attachmentSeasonPendants.put("2", CreateAttachment("systemui_seasonpendant02"));
        this.attachmentSeasonPendants.put("3", CreateAttachment("systemui_seasonpendant03"));
        this.attachmentSeasonPendants.put("4", CreateAttachment("systemui_seasonpendant04"));
        this.attachmentSeasonPendants.put("5", CreateAttachment("systemui_seasonpendant05"));
        this.attachmentSeasonPendants.put("6", CreateAttachment("systemui_seasonpendant06"));
        this.attachmentSeasonPendants.put("7", CreateAttachment("systemui_seasonpendant07"));
        this.attachmentSeasonPendants.put("8", CreateAttachment("systemui_seasonpendant08"));
        this.attachmentSeasonPendants.put("9", CreateAttachment("systemui_seasonpendant09"));
        this.attachmentSeasonPendants.put("10", CreateAttachment("systemui_seasonpendant10"));
        this.attachmentSeasonPendants.put("11", CreateAttachment("systemui_seasonpendant11"));
        this.attachmentSeasonPendants.put("12", CreateAttachment("systemui_seasonpendant12"));
        this.attachmentSeasonPendants.put((String) obj, CreateAttachment("systemui_seasonpendant13"));
        this.attachmentSeasonPendants.put((String) obj4, CreateAttachment("systemui_seasonpendant14"));
        this.attachmentSeasonPendants.put((String) obj3, CreateAttachment("systemui_seasonpendant14"));
        this.attachmentSeasonQuest = CreateAttachment("systemui_seasonquest");
        this.attachmentSit = CreateAttachment("systemui_sit");
        this.attachmentSpellEarth = CreateAttachment("systemui_spellearth");
        this.attachmentSpellFire = CreateAttachment("systemui_spellfire");
        this.attachmentSpellMind = CreateAttachment("systemui_spellmind");
        this.attachmentSpellVoid = CreateAttachment("systemui_spellvoid");
        this.attachmentSpellWater = CreateAttachment("systemui_spellwater");
        this.attachmentSpellWind = CreateAttachment("systemui_spellwind");
        this.attachmentStar = CreateAttachment("systemui_star");
        this.attachmentSupport = CreateAttachment("systemui_support");
        this.attachmentToggleFly = CreateAttachment("systemui_togglefly");
        this.attachmentToggleHover = CreateAttachment("systemui_togglehover");
        this.attachmentToggleSwim = CreateAttachment("systemui_toggleswim");
        this.attachmentToggleDive = CreateAttachment("systemui_toggledive");
        this.attachmentThumbstick = CreateAttachment("systemui_thumbstick");
        this.attachmentWingBuff = CreateAttachment("systemui_wingbuff");
    }

    private static int GetColor(float f, float f2, float f3, float f4) {
        return Color.argb((int) (f4 * 255.0f), (int) (f * 255.0f), (int) (f2 * 255.0f), (int) (f3 * 255.0f));
    }

    private Drawable CreateAttachment(String resName) {
        Drawable drawable;
        try {
            drawable = SMLApplication.skyRes.getDrawable(SMLApplication.skyRes.getIdentifier(resName, "drawable", SMLApplication.skyPName), null);
        }catch (Exception e) {
            drawable = new ColorDrawable(Color.WHITE);
        }
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        return drawable;
    }

    public void SetGamepadButtonMap(String[] strArr, String[] strArr2) {
        this.m_buttonMap.clear();
        for (int i = 0; i < strArr.length; i++) {
            this.m_buttonMap.put(strArr[i], strArr2[i]);
        }
    }

    public Typeface DefaultFont() {
        if (this.m_defaultFont == null) {
            this.m_defaultFont = Typeface.DEFAULT;
        }
        return this.m_defaultFont;
    }

    public Object[] DefaultFont(float f) {
        return new Object[]{new CustomTypefaceSpan(DefaultFont()), new AbsoluteSizeSpan((int) (f + 0.5f), true)};
    }

    public Object[] DefaultFontGame(float f) {
        return new Object[]{new CustomTypefaceSpan(DefaultFont()), new AbsoluteSizeSpan(Utils.sp2px(f), false)};
    }
    public ArrayList<Object> ProcessMarkupTag(String s, final Map<Object, Object> map, final StringBuilder sb) {
        final ArrayList list = new ArrayList<>();
        final int hashCode = s.hashCode();
        final int n = -1;
        int n2 = 0;
        Label_1678: {
            switch (hashCode) {
                case 1910952256: {
                    if (s.equals("scandle")) {
                        n2 = 26;
                        break Label_1678;
                    }
                    break;
                }
                case 1799980710: {
                    if (s.equals("lfire_img")) {
                        n2 = 58;
                        break Label_1678;
                    }
                    break;
                }
                case 1655054676: {
                    if (s.equals("diamond")) {
                        n2 = 39;
                        break Label_1678;
                    }
                    break;
                }
                case 1453070481: {
                    if (s.equals("toggle_hover")) {
                        n2 = 47;
                        break Label_1678;
                    }
                    break;
                }
                case 1247545208: {
                    if (s.equals("lwind_img")) {
                        n2 = 62;
                        break Label_1678;
                    }
                    break;
                }
                case 1152148322: {
                    if (s.equals("learth_img")) {
                        n2 = 57;
                        break Label_1678;
                    }
                    break;
                }
                case 1124446108: {
                    if (s.equals("warning")) {
                        n2 = 10;
                        break Label_1678;
                    }
                    break;
                }
                case 1001013270: {
                    if (s.equals("rbheart")) {
                        n2 = 45;
                        break Label_1678;
                    }
                    break;
                }
                case 999311042: {
                    if (s.equals("lmind_img")) {
                        n2 = 59;
                        break Label_1678;
                    }
                    break;
                }
                case 887575149: {
                    if (s.equals("exclamation")) {
                        n2 = 41;
                        break Label_1678;
                    }
                    break;
                }
                case 820174141: {
                    if (s.equals("rbcandle")) {
                        n2 = 44;
                        break Label_1678;
                    }
                    break;
                }
                case 391643908: {
                    if (s.equals("lvoid_img")) {
                        n2 = 60;
                        break Label_1678;
                    }
                    break;
                }
                case 109549023: {
                    if (s.equals("smask")) {
                        n2 = 32;
                        break Label_1678;
                    }
                    break;
                }
                case 109548807: {
                    if (s.equals("small")) {
                        n2 = 8;
                        break Label_1678;
                    }
                    break;
                }
                case 103389812: {
                    if (s.equals("lwind")) {
                        n2 = 56;
                        break Label_1678;
                    }
                    break;
                }
                case 103365632: {
                    if (s.equals("lvoid")) {
                        n2 = 54;
                        break Label_1678;
                    }
                    break;
                }
                case 103091902: {
                    if (s.equals("lmind")) {
                        n2 = 53;
                        break Label_1678;
                    }
                    break;
                }
                case 102883490: {
                    if (s.equals("lfire")) {
                        n2 = 52;
                        break Label_1678;
                    }
                    break;
                }
                case 99151942: {
                    if (s.equals("heart")) {
                        n2 = 35;
                        break Label_1678;
                    }
                    break;
                }
                case 97221289: {
                    if (s.equals("fbgrp")) {
                        n2 = 17;
                        break Label_1678;
                    }
                    break;
                }
                case 37544367: {
                    if (s.equals("lwater_img")) {
                        n2 = 61;
                        break Label_1678;
                    }
                    break;
                }
                case 3560141: {
                    if (s.equals("time")) {
                        n2 = 65;
                        break Label_1678;
                    }
                    break;
                }
                case 3540562: {
                    if (s.equals("star")) {
                        n2 = 30;
                        break Label_1678;
                    }
                    break;
                }
                case 3143115: {
                    if (s.equals("finv")) {
                        n2 = 19;
                        break Label_1678;
                    }
                    break;
                }
                case 3046099: {
                    if (s.equals("cape")) {
                        n2 = 38;
                        break Label_1678;
                    }
                    break;
                }
                case 115032: {
                    if (s.equals("tos")) {
                        n2 = 20;
                        break Label_1678;
                    }
                    break;
                }
                case 113886: {
                    if (s.equals("sit")) {
                        n2 = 40;
                        break Label_1678;
                    }
                    break;
                }
                case 104387: {
                    if (s.equals("img")) {
                        n2 = 64;
                        break Label_1678;
                    }
                    break;
                }
                case 98683: {
                    if (s.equals("cog")) {
                        n2 = 34;
                        break Label_1678;
                    }
                    break;
                }
                case 3584: {
                    if (s.equals("pp")) {
                        n2 = 21;
                        break Label_1678;
                    }
                    break;
                }
                case 3276: {
                    if (s.equals("h4")) {
                        n2 = 7;
                        break Label_1678;
                    }
                    break;
                }
                case 3275: {
                    if (s.equals("h3")) {
                        n2 = 6;
                        break Label_1678;
                    }
                    break;
                }
                case 3274: {
                    if (s.equals("h2")) {
                        n2 = 5;
                        break Label_1678;
                    }
                    break;
                }
                case 3273: {
                    if (s.equals("h1")) {
                        n2 = 4;
                        break Label_1678;
                    }
                    break;
                }
                case 3184: {
                    if (s.equals("cs")) {
                        n2 = 22;
                        break Label_1678;
                    }
                    break;
                }
                case 117: {
                    if (s.equals("u")) {
                        n2 = 13;
                        break Label_1678;
                    }
                    break;
                }
                case 115: {
                    if (s.equals("s")) {
                        n2 = 15;
                        break Label_1678;
                    }
                    break;
                }
                case 105: {
                    if (s.equals("i")) {
                        n2 = 12;
                        break Label_1678;
                    }
                    break;
                }
                case 103: {
                    if (s.equals("g")) {
                        n2 = 14;
                        break Label_1678;
                    }
                    break;
                }
                case 98: {
                    if (s.equals("b")) {
                        n2 = 11;
                        break Label_1678;
                    }
                    break;
                }
                case 97: {
                    if (s.equals("a")) {
                        n2 = 16;
                        break Label_1678;
                    }
                    break;
                }
                case 56: {
                    if (s.equals("8")) {
                        n2 = 1;
                        break Label_1678;
                    }
                    break;
                }
                case 51: {
                    if (s.equals("3")) {
                        n2 = 3;
                        break Label_1678;
                    }
                    break;
                }
                case 50: {
                    if (s.equals("2")) {
                        n2 = 2;
                        break Label_1678;
                    }
                    break;
                }
                case 49: {
                    if (s.equals("1")) {
                        n2 = 0;
                        break Label_1678;
                    }
                    break;
                }
                case -645528429: {
                    if (s.equals("toggle_swim")) {
                        n2 = 48;
                        break Label_1678;
                    }
                    break;
                }
                case -645988353: {
                    if (s.equals("toggle_dive")) {
                        n2 = 49;
                        break Label_1678;
                    }
                    break;
                }
                case -805148524: {
                    if (s.equals("apcolor")) {
                        n2 = 9;
                        break Label_1678;
                    }
                    break;
                }
                case -852120312: {
                    if (s.equals("toggle_fly")) {
                        n2 = 46;
                        break Label_1678;
                    }
                    break;
                }
                case -891050150: {
                    if (s.equals("survey")) {
                        n2 = 18;
                        break Label_1678;
                    }
                    break;
                }
                case -894670769: {
                    if (s.equals("squest")) {
                        n2 = 33;
                        break Label_1678;
                    }
                    break;
                }
                case -903462989: {
                    if (s.equals("sheart")) {
                        n2 = 27;
                        break Label_1678;
                    }
                    break;
                }
                case -1090115541: {
                    if (s.equals("lwater")) {
                        n2 = 55;
                        break Label_1678;
                    }
                    break;
                }
                case -1106740386: {
                    if (s.equals("learth")) {
                        n2 = 51;
                        break Label_1678;
                    }
                    break;
                }
                case -1112997154: {
                    if (s.equals("wingbuff")) {
                        n2 = 31;
                        break Label_1678;
                    }
                    break;
                }
                case -1183699191: {
                    if (s.equals("invite")) {
                        n2 = 36;
                        break Label_1678;
                    }
                    break;
                }
                case -1276224445: {
                    if (s.equals("prestige")) {
                        n2 = 28;
                        break Label_1678;
                    }
                    break;
                }
                case -1330662135: {
                    if (s.equals("buddyrequest")) {
                        n2 = 42;
                        break Label_1678;
                    }
                    break;
                }
                case -1367723251: {
                    if (s.equals("candle")) {
                        n2 = 25;
                        break Label_1678;
                    }
                    break;
                }
                case -1367751899: {
                    if (s.equals("camera")) {
                        n2 = 24;
                        break Label_1678;
                    }
                    break;
                }
                case -1377687758: {
                    if (s.equals("button")) {
                        n2 = 63;
                        break Label_1678;
                    }
                    break;
                }
                case -1393046460: {
                    if (s.equals("beacon")) {
                        n2 = 23;
                        break Label_1678;
                    }
                    break;
                }
                case -1473151104: {
                    if (s.equals("hinticon")) {
                        n2 = 43;
                        break Label_1678;
                    }
                    break;
                }
                case -1697979270: {
                    if (s.equals("thumbstick")) {
                        n2 = 50;
                        break Label_1678;
                    }
                    break;
                }
                case -1854767153: {
                    if (s.equals("support")) {
                        n2 = 37;
                        break Label_1678;
                    }
                    break;
                }
                case -2122869815: {
                    if (s.equals("spendant")) {
                        n2 = 29;
                        break Label_1678;
                    }
                    break;
                }
            }
            n2 = -1;
        }
        switch (n2) {
            case 65: {
                sb.append(DateFormat.getTimeFormat((Context)this.m_activity).format(new Date()));
                break;
            }
            case 64: {
                final boolean boolean1 = Boolean.getBoolean((String) map.getOrDefault("isTemplate", "true"));
                s = (String) map.get("src");
                if (s != null) {
                    list.add(new EmbeddedImageSpan(this.CreateAttachment(s.toLowerCase(Locale.ROOT)), boolean1, false));
                    break;
                }
                break;
            }
            case 63: {
                final Drawable drawable = null;
                final String s2 = (String) map.get("type");
                Drawable drawable2 = drawable;
                if (s2 != null) {
                    final String s3 = this.m_buttonMap.get(s2);
                    drawable2 = drawable;
                    if (s3 != null) {
                        int n3 = 0;
                        switch (s3.hashCode()) {
                            default: {
                                n3 = n;
                                break;
                            }
                            case 2060706535: {
                                n3 = n;
                                if (s3.equals("LeftShoulder")) {
                                    n3 = 4;
                                    break;
                                }
                                break;
                            }
                            case 1143089180: {
                                n3 = n;
                                if (s3.equals("RightTrigger")) {
                                    n3 = 7;
                                    break;
                                }
                                break;
                            }
                            case 89: {
                                n3 = n;
                                if (s3.equals("Y")) {
                                    n3 = 1;
                                    break;
                                }
                                break;
                            }
                            case 88: {
                                n3 = n;
                                if (s3.equals("X")) {
                                    n3 = 0;
                                    break;
                                }
                                break;
                            }
                            case 66: {
                                n3 = n;
                                if (s3.equals("B")) {
                                    n3 = 3;
                                    break;
                                }
                                break;
                            }
                            case 65: {
                                n3 = n;
                                if (s3.equals("A")) {
                                    n3 = 2;
                                    break;
                                }
                                break;
                            }
                            case -566808687: {
                                n3 = n;
                                if (s3.equals("LeftTrigger")) {
                                    n3 = 6;
                                    break;
                                }
                                break;
                            }
                            case -767034436: {
                                n3 = n;
                                if (s3.equals("RightShoulder")) {
                                    n3 = 5;
                                    break;
                                }
                                break;
                            }
                        }
                        switch (n3) {
                            default: {
                                drawable2 = drawable;
                                break;
                            }
                            case 7: {
                                drawable2 = this.attachmentRightTrigger;
                                break;
                            }
                            case 6: {
                                drawable2 = this.attachmentLeftTrigger;
                                break;
                            }
                            case 5: {
                                drawable2 = this.attachmentRightShoulder;
                                break;
                            }
                            case 4: {
                                drawable2 = this.attachmentLeftShoulder;
                                break;
                            }
                            case 3: {
                                drawable2 = this.attachmentGamepadB;
                                break;
                            }
                            case 2: {
                                drawable2 = this.attachmentGamepadA;
                                break;
                            }
                            case 1: {
                                drawable2 = this.attachmentGamepadY;
                                break;
                            }
                            case 0: {
                                drawable2 = this.attachmentGamepadX;
                                break;
                            }
                        }
                    }
                }
                if (drawable2 != null) {
                    list.add(new EmbeddedImageSpan(drawable2, true, true));
                }
                list.add((new ForegroundColorSpan(GetColor(0.5f, 1.0f, 1.0f, 1.0f))));
                break;
            }
            case 62: {
                list.add(new EmbeddedImageSpan(this.attachmentSpellWind, true, false));
                break;
            }
            case 61: {
                list.add(new EmbeddedImageSpan(this.attachmentSpellWater, true, false));
                break;
            }
            case 60: {
                list.add(new EmbeddedImageSpan(this.attachmentSpellVoid, true, false));
                break;
            }
            case 59: {
                list.add(new EmbeddedImageSpan(this.attachmentSpellMind, true, false));
                break;
            }
            case 58: {
                list.add(new EmbeddedImageSpan(this.attachmentSpellFire, true, false));
                break;
            }
            case 57: {
                list.add(new EmbeddedImageSpan(this.attachmentSpellEarth, true, false));
                break;
            }
            case 56: {
                list.add(new StyleSpan(1));
                list.add(new ForegroundColorSpan(GetColor(0.34117648f, 1.0f, 0.7921569f, 1.0f)));
                break;
            }
            case 55: {
                list.add(new StyleSpan(1));
                list.add(new ForegroundColorSpan(GetColor(0.38431373f, 0.5137255f, 1.0f, 1.0f)));
                break;
            }
            case 54: {
                list.add(new StyleSpan(1));
                list.add(new ForegroundColorSpan(GetColor(1.0f, 0.23137255f, 0.23137255f, 1.0f)));
                break;
            }
            case 53: {
                list.add(new StyleSpan(1));
                list.add(new ForegroundColorSpan(GetColor(0.6666667f, 0.33333334f, 1.0f, 1.0f)));
                break;
            }
            case 52: {
                list.add(new StyleSpan(1));
                list.add(new ForegroundColorSpan(GetColor(1.0f, 0.6117647f, 0.0f, 1.0f)));
                break;
            }
            case 51: {
                list.add(new StyleSpan(1));
                list.add(new ForegroundColorSpan(GetColor(0.5176471f, 0.81960785f, 0.29803923f, 1.0f)));
                break;
            }
            case 50: {
                list.add(new EmbeddedImageSpan(this.attachmentThumbstick, true, true));
                list.add(new ForegroundColorSpan(GetColor(0.5f, 1.0f, 1.0f, 1.0f)));
                break;
            }
            case 49: {
                list.add(new EmbeddedImageSpan(this.attachmentToggleDive, true, false));
                break;
            }
            case 48: {
                list.add(new EmbeddedImageSpan(this.attachmentToggleSwim, true, false));
                break;
            }
            case 47: {
                list.add(new EmbeddedImageSpan(this.attachmentToggleHover, true, false));
                break;
            }
            case 46: {
                list.add(new EmbeddedImageSpan(this.attachmentToggleFly, true, false));
                break;
            }
            case 45: {
                list.add(new EmbeddedImageSpan(this.attachmentHeart, true, false));
                break;
            }
            case 44: {
                list.add(new EmbeddedImageSpan(this.attachmentCandle, true, false));
                break;
            }
            case 43: {
                list.add(new EmbeddedImageSpan(this.attachmentLightBulb, true, false));
                list.add(new ForegroundColorSpan(this.menuWhite));
                break;
            }
            case 42: {
                list.add(new EmbeddedImageSpan(this.attachmentQuestion, false, false));
                break;
            }
            case 41: {
                list.add(new EmbeddedImageSpan(this.attachmentExclamation, true, false));
                list.add(new ForegroundColorSpan(this.menuWhite));
                break;
            }
            case 40: {
                list.add(new EmbeddedImageSpan(this.attachmentSit, true, false));
                list.add(new ForegroundColorSpan(this.menuGold));
                break;
            }
            case 39: {
                list.add(new EmbeddedImageSpan(this.attachmentDiamond, true, false));
                list.add(new ForegroundColorSpan(this.menuGold));
                break;
            }
            case 38: {
                list.add(new EmbeddedImageSpan(this.attachmentCape, true, false));
                break;
            }
            case 37: {
                list.add(new EmbeddedImageSpan(this.attachmentSupport, true, false));
                list.add(new ForegroundColorSpan(this.menuGold));
                break;
            }
            case 36: {
                list.add(new EmbeddedImageSpan(this.attachmentInvite, true, false));
                list.add(new ForegroundColorSpan(this.menuGold));
                break;
            }
            case 35: {
                list.add(new EmbeddedImageSpan(this.attachmentHeart, true, false));
                break;
            }
            case 34: {
                list.add(new EmbeddedImageSpan(this.attachmentCog, true, false));
                list.add(new ForegroundColorSpan(this.menuGold));
                break;
            }
            case 33: {
                list.add(new EmbeddedImageSpan(this.attachmentSeasonQuest, false, false));
                break;
            }
            case 32: {
                list.add(new EmbeddedImageSpan(this.ProcessSeasonArg(this.attachmentSeasonMasks, (String) map.get("season")), false, false));
                break;
            }
            case 31: {
                list.add(new EmbeddedImageSpan(this.attachmentWingBuff, true, false));
                break;
            }
            case 30: {
                list.add(new EmbeddedImageSpan(this.attachmentStar, true, false));
                break;
            }
            case 29: {
                list.add(new EmbeddedImageSpan(this.ProcessSeasonArg(this.attachmentSeasonPendants, (String) map.get("season")), false, false));
                break;
            }
            case 28: {
                list.add(new EmbeddedImageSpan(this.attachmentPrestige, true, false));
                break;
            }
            case 27: {
                list.add(new EmbeddedImageSpan(this.ProcessSeasonArg(this.attachmentSeasonHearts, (String) map.get("season")), true, false));
                list.add(new ForegroundColorSpan(this.adventurePassColor));
                break;
            }
            case 26: {
                list.add(new EmbeddedImageSpan(this.ProcessSeasonArg(this.attachmentSeasonCandles, (String) map.get("season")), true, false));
                list.add(new ForegroundColorSpan(this.adventurePassColor));
                break;
            }
            case 25: {
                list.add(new EmbeddedImageSpan(this.attachmentCandle, true, false));
                break;
            }
            case 24: {
                list.add(new EmbeddedImageSpan(this.attachmentCamera, true, false));
                break;
            }
            case 23: {
                list.add(new EmbeddedImageSpan(this.attachmentBeacon, true, false));
                break;
            }
            case 22: {
                list.add(new URLSpan("https://thatgamecompany.helpshift.com/a/sky-children-of-the-light/?contact=1"));
                break;
            }
            case 21: {
                list.add(new URLSpan("https://thatgamecompany.helpshift.com/a/sky-children-of-the-light/?s=legal&f=privacy-policy"));
                break;
            }
            case 20: {
                list.add(new URLSpan("https://thatgamecompany.helpshift.com/a/sky-children-of-the-light/?s=legal&f=eula-terms-of-service"));
                break;
            }
            case 19: {
                list.add(new URLSpan("https://goo.gl/forms/QWEjp98EpNKnraeT2"));
                break;
            }
            case 18: {
                list.add(new URLSpan("https://goo.gl/forms/JeSUNMOqYhXpqJv52"));
                break;
            }
            case 17: {
                list.add(new URLSpan("https://www.facebook.com/groups/1720963611544203/"));
                break;
            }
            case 16: {
                list.add(new URLSpan((String)map.getOrDefault("href", "itms-beta://beta.itunes.apple.com/v1/app/1219437821")));
                break;
            }
            case 15: {
                list.add(new ShadowSpan(this.shadowColor, new PointF(2.0f, 2.0f)));
                break;
            }
            case 14: {
                list.add(new ForegroundColorSpan(-1));
                list.add(new StrokeSpan(3, 6, this.shadowColor));
                break;
            }
            case 13: {
                list.add(new UnderlineSpan());
                break;
            }
            case 12: {
                list.add(new StyleSpan(2));
                break;
            }
            case 11: {
                list.add(new StyleSpan(1));
                break;
            }
            case 10: {
                list.add(new ForegroundColorSpan(GetColor(1.0f, 0.25f, 0.25f, 1.0f)));
                list.add(new StyleSpan(1));
                list.add(new ShadowSpan(this.shadowColor, new PointF(2.0f, 2.0f)));
                break;
            }
            case 9: {
                list.add(new ForegroundColorSpan(this.adventurePassColor));
                list.add(new StyleSpan(1));
                break;
            }
            case 8: {
                list.add(new RelativeSizeSpan(0.75f));
                break;
            }
            case 7: {
                list.add(new StyleSpan(1));
                list.add(new RelativeSizeSpan(1.1f));
                break;
            }
            case 6: {
                list.add(new StyleSpan(1));
                list.add(new RelativeSizeSpan(1.3f));
                break;
            }
            case 5: {
                list.add(new StyleSpan(1));
                list.add(new RelativeSizeSpan(1.5f));
                break;
            }
            case 4: {
                list.add(new StyleSpan(1));
                list.add(new RelativeSizeSpan(2.0f));
                break;
            }
            case 3: {
                list.add(new ForegroundColorSpan(GetColor(0.0f, 0.3f, 0.0f, 1.0f)));
                list.add(new StyleSpan(1));
                break;
            }
            case 2: {
                list.add(new ForegroundColorSpan(GetColor(1.0f, 0.5f, 0.0f, 1.0f)));
                list.add(new StyleSpan(1));
                break;
            }
            case 0:
            case 1: {
                list.add(new ForegroundColorSpan(GetColor(0.5f, 1.0f, 1.0f, 1.0f)));
                list.add(new StyleSpan(1));
                break;
            }
        }
        if (sb != null) {
            final Iterator<EmbeddedImageSpan> iterator = list.iterator();
            while (iterator.hasNext()) {
                if (iterator.next() instanceof EmbeddedImageSpan) {
                    sb.append(String.format("%C", 65532));
                    break;
                }
            }
        }
        return (ArrayList<Object>)list;
    }

    public SpannableStringBuilder GetMarkedUpString(String str, ArrayList<Object> arrayList, boolean z) {
        int i;
        int i2;
        String str2;
        ArrayList<Object> arrayList2 = arrayList;
        String str3 = str == null ? "" : str;
        if (str3.length() == 0 || z) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str3);
            InitAttributedStringWithAttributes(spannableStringBuilder, arrayList2);
            return spannableStringBuilder;
        }
        StringBuilder sb = new StringBuilder();
        ArrayList arrayList3 = new ArrayList();
        ArrayList arrayList4 = new ArrayList();
        ArrayList arrayList5 = new ArrayList();
        ArrayList arrayList6 = new ArrayList();
        StringBuilder sb2 = new StringBuilder(str3);
        int i3 = 0;
        while (i3 < sb2.length()) {
            int indexOf = sb2.indexOf("<", i3);
            if (indexOf >= 0) {
                String substring = sb2.substring(i3, indexOf);
                i = indexOf + 1;
                sb.append(substring);
            } else {
                String substring2 = sb2.substring(i3, sb2.length());
                i = sb2.length();
                sb.append(substring2);
            }
            int indexOf2 = sb2.indexOf(">", i);
            if (indexOf2 >= 0) {
                i2 = indexOf2 + 1;
                str2 = sb2.substring(i, indexOf2);
            } else {
                str2 = sb2.substring(i, sb2.length());
                i2 = sb2.length();
            }
            ProcessMarkupTag(sb, str2, arrayList5, arrayList6, arrayList4, arrayList3);
            i3 = i2;
        }
        SpannableStringBuilder spannableStringBuilder2 = new SpannableStringBuilder(sb);
        InitAttributedStringWithAttributes(spannableStringBuilder2, arrayList2);
        for (int i4 = 0; i4 < arrayList3.size(); i4++) {
            AddAttributesWithRange(spannableStringBuilder2, (ArrayList) arrayList4.get(i4), (NtRange) arrayList3.get(i4));
        }
        return spannableStringBuilder2;
    }

    private void ProcessMarkupTag(StringBuilder sb, String str, ArrayList<String> arrayList, ArrayList<Object> arrayList2, ArrayList<ArrayList<Object>> arrayList3, ArrayList<Object> arrayList4) {
        String str2;
        String str3 = str;
        ArrayList<String> arrayList5 = arrayList;
        ArrayList<Object> arrayList6 = arrayList2;
        ArrayList<Object> arrayList7 = arrayList4;
        boolean endsWith = str3.endsWith("/");
        boolean z = endsWith || str3.startsWith("/");
        List<String> arrayList8 = new ArrayList<>(Arrays.asList(TrimmingString(str3, '/').trim().split(" ")));
        arrayList8.removeIf(new Predicate<String>() {
            public boolean test(String str) {
                    return str.length() <= 0;
                }
        });
        if (arrayList8.size() != 0) {
            String str4 = (String) arrayList8.get(0);
            if (arrayList8.size() > 0) {
                arrayList8 = arrayList8.subList(1, arrayList8.size());
            }
            HashMap hashMap = new HashMap();
            if (arrayList8.size() > 0) {
                for (String split : arrayList8) {
                    ArrayList arrayList9 = new ArrayList(Arrays.asList(split.split("=")));
                    if (arrayList9.size() > 1) {
                        String str5 = (String) arrayList9.get(0);
                        if (arrayList9.size() > 2) {
                            str2 = componentsJoinedByString(arrayList9.subList(1, arrayList9.size()), "=");
                        } else {
                            str2 = (String) arrayList9.get(1);
                        }
                        if (str2.startsWith("\"") && str2.endsWith("\"")) {
                            hashMap.put(str5, TrimmingString(str2, '"'));
                        } else if (str2.startsWith("'") && str2.endsWith("'")) {
                            hashMap.put(str5, TrimmingString(str2, '\''));
                        }
                    }
                }
            }
            if (!z || endsWith) {
                try {
                    int length = sb.length();
                    ArrayList<Object> ProcessMarkupTag = ProcessMarkupTag(str4, hashMap, sb);
                    if (ProcessMarkupTag.size() != 0) {
                        NtRange ntRange = new NtRange(length, 0);
                        if (!endsWith) {
                            arrayList5.add(str4);
                            arrayList6.add(Integer.valueOf(arrayList3.size()));
                        } else {
                            ntRange.length = sb.length() - ntRange.location;
                        }
                        arrayList3.add(ProcessMarkupTag);
                        arrayList7.add(ntRange);
                    }
                } catch (Exception e) {
                    Log.e("SystemUI", e.getMessage());
                }
            } else if (arrayList.size() > 0) {
                String str6 = arrayList5.get(arrayList.size() - 1);
                arrayList5.remove(arrayList.size() - 1);
                if (!str4.equals(str6)) {
                    Log.e("UI", String.format("Mismatched closing tag '%s', expected '%s'", new Object[]{str4, str6}));
                }
                int intValue = ((Integer) arrayList6.get(arrayList2.size() - 1)).intValue();
                arrayList6.remove(arrayList2.size() - 1);
                NtRange ntRange2 = (NtRange) arrayList7.get(intValue);
                ntRange2.length = sb.length() - ntRange2.location;
                arrayList7.set(intValue, ntRange2);
            }
        }
    }

    private String TrimmingString(String str, char c) {
        int length = str.length();
        int i = 0;
        while (i < length && str.charAt(i) == c) {
            i++;
        }
        while (i < length && str.charAt(length - 1) == c) {
            length--;
        }
        return (i > 0 || length < str.length()) ? str.substring(i, length) : str;
    }

    private String componentsJoinedByString(List<String> list, String str) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < list.size()) {
            sb.append(list.get(i));
            i++;
            if (i < list.size()) {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    private void InitAttributedStringWithAttributes(SpannableStringBuilder spannableStringBuilder, ArrayList<Object> arrayList) {
        AddAttributesWithRange(spannableStringBuilder, arrayList, new NtRange(0, spannableStringBuilder.length()));
    }

    private void AddAttributeWithRange(SpannableString spannableString, Object obj, NtRange ntRange) {
        if (obj instanceof Object[]) {
            for (Object AddAttributeWithRange : (Object[]) obj) {
                AddAttributeWithRange(spannableString, AddAttributeWithRange, ntRange);
            }
            return;
        }
        spannableString.setSpan(obj, ntRange.location, ntRange.location + ntRange.length, 17);
    }

    private void AddAttributeWithRange(SpannableStringBuilder spannableStringBuilder, Object obj, NtRange ntRange) {
        if (obj instanceof Object[]) {
            for (Object AddAttributeWithRange : (Object[]) obj) {
                AddAttributeWithRange(spannableStringBuilder, AddAttributeWithRange, ntRange);
            }
            return;
        }
        spannableStringBuilder.setSpan(obj, ntRange.location, ntRange.location + ntRange.length, 17);
    }

    private void AddAttributesWithRange(SpannableStringBuilder spannableStringBuilder, ArrayList<Object> arrayList, NtRange ntRange) {
        Iterator<Object> it = arrayList.iterator();
        while (it.hasNext()) {
            AddAttributeWithRange(spannableStringBuilder, it.next(), ntRange);
        }
    }

    private Drawable ProcessSeasonArg(Map<String, Drawable> map, String str) {
        if (str == null) {
            str = this.m_activity.ResolveTemplateArgs("{{Seasons::season}}");
        } else if (str.equalsIgnoreCase("prev")) {
            str = this.m_activity.ResolveTemplateArgs("{{Seasons::prev}}");
        } else if (str.equalsIgnoreCase("next")) {
            str = this.m_activity.ResolveTemplateArgs("{{Seasons::next}}");
        }
        if (str.isEmpty() || !map.containsKey(str)) {
            return map.get("default");
        }
        return map.get(str);
    }
}
