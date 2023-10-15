package git.artdeell.skymodloader.auth;

import com.tgc.sky.accounts.SystemAccountType;
import git.artdeell.skymodloader.auth.WebLogin;


public class Google extends WebLogin {
    public Google() {
        super("Google", SystemAccountType.kSystemAccountType_Google); }
}
