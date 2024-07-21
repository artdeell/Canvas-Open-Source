package git.artdeell.skymodloader.ui.home;

public class BaseDevItem {
    public BaseDevItem(String name, String gitHubLink, String telegramLink) {
        this.name = name;
        this.gitHubLink = gitHubLink;
        this.telegramLink = telegramLink;
    }

    public String name;
    public String gitHubLink;
    public String telegramLink;

}
