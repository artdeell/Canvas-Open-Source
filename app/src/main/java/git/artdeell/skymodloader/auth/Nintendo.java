package git.artdeell.skymodloader.auth;

import com.tgc.sky.accounts.SystemAccountType;

import git.artdeell.skymodloader.auth.WebLogin;

public class Nintendo extends WebLogin {
    public Nintendo() {
        super("Nintendo", SystemAccountType.kSystemAccountType_Nintendo);
    }
}
