package git.artdeell.skymodloader.appauth;

public enum AppAuthDiscoverable {
    GOOGLE("https://accounts.google.com/.well-known/openid-configuration","google");
    public final String discoveryURL;
    public final String name;
    AppAuthDiscoverable(String discoveryURL, String cacheFileName) {
        this.discoveryURL = discoveryURL;
        this.name = cacheFileName;
    }
}
