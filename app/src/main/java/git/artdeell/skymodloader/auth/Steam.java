package git.artdeell.skymodloader.auth;

import com.tgc.sky.accounts.SystemAccountType;
import git.artdeell.skymodloader.auth.WebLogin;

public class Steam extends WebLogin {
    public Steam() {
        super("Steam", SystemAccountType.kSystemAccountType_Steam);
    }
}
