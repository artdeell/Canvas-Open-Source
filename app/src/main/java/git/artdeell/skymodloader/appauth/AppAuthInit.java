package git.artdeell.skymodloader.appauth;


import android.util.ArrayMap;

import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.AuthorizationServiceDiscovery;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AppAuthInit implements Runnable{
    private static final Map<String, AuthorizationServiceConfiguration> authorizationServiceConfigurations = new ArrayMap<>();
    private final AppAuthDiscoverable discoverable;
    private final AppAuthInitIface finishRunnable;
    private final File cacheDir;

    private AppAuthInit(AppAuthDiscoverable discoverable, AppAuthInitIface finishRunnable, File cacheDir) {
        this.finishRunnable = finishRunnable;
        this.cacheDir = cacheDir;
        this.discoverable = discoverable;
    }

    public static void postInit(AppAuthDiscoverable discoverable, AppAuthInitIface r, File cacheDir) {
        if(authorizationServiceConfigurations.containsKey(discoverable.name)) r.finish(true);
        else new Thread(new AppAuthInit(discoverable, r, cacheDir)).start();
    }
    public static AuthorizationServiceConfiguration getConfiguration(AppAuthDiscoverable discoverable) {
        if(authorizationServiceConfigurations.containsKey(discoverable.name)) {
            return authorizationServiceConfigurations.get(discoverable.name);
        }else {
            throw new IllegalArgumentException("The discoverable hasn't been discovered yet, did you forget the listener?");
        }
    }

    @Override
    public void run() {
        try {
            File openid_config = new File(cacheDir, discoverable.name+"-oid-config");
            if (openid_config.canRead() && openid_config.isFile()) {
                authorizationServiceConfigurations.put(discoverable.name, new AuthorizationServiceConfiguration(
                        new AuthorizationServiceDiscovery(new JSONObject(dump(new FileInputStream(openid_config))))));
            }else{
                String config = dump(new URL(discoverable.discoveryURL).openStream());
                authorizationServiceConfigurations.put(discoverable.name, new AuthorizationServiceConfiguration(
                        new AuthorizationServiceDiscovery(new JSONObject(config))));
                try {
                    dumpTo(openid_config, config);
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
            finishRunnable.finish(true);
        }catch (IOException | JSONException | AuthorizationServiceDiscovery.MissingArgumentException e) {
            e.printStackTrace();
            finishRunnable.finish(false);
        }
    }
    public static String dump(InputStream is) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        byte[] buf = new byte[256];
        int len;
        while((len = is.read(buf)) != -1) {
            stringBuilder.append(new String(buf,0,len));
        }
        is.close();
        return stringBuilder.toString();
    }
    static void dumpTo(File file, String str) throws IOException{
        boolean _do = true;
        if(file.exists()) _do = file.delete();
        if(_do) {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(str.getBytes(StandardCharsets.UTF_8));
            fos.close();
        }
    }
}
