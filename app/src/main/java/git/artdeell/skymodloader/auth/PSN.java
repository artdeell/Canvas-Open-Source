package git.artdeell.skymodloader.auth;

import com.tgc.sky.accounts.SystemAccountType;

import git.artdeell.skymodloader.auth.WebLogin;

public class PSN extends WebLogin {
    public PSN() {
        super("Sony", SystemAccountType.kSystemAccountType_PSN);
    }
}
