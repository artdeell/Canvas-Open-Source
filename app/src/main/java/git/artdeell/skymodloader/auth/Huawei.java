package git.artdeell.skymodloader.auth;

import com.tgc.sky.accounts.SystemAccountType;

import git.artdeell.skymodloader.auth.WebLogin;

public class Huawei extends WebLogin {
    public Huawei() {
        super("Huawei", SystemAccountType.kSystemAccountType_Huawei);
    }
}
