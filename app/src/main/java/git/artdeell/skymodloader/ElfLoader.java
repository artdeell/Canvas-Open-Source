package git.artdeell.skymodloader;

import android.util.Log;

import androidx.collection.ArrayMap;

import net.fornwall.jelf.ElfDynamicSection;
import net.fornwall.jelf.ElfFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElfLoader {
    final List<String> alreadyLoadedLibs = new ArrayList<>();
    final List<ArrayMap<String, File>> dirCache = new ArrayList<>(); //Directory cache - here are all the files in loader path. Well, they would appear there after class' construction
    /*
     * Constructs an ElfSequencer with specified search paths. Search paths should be a list of directory paths separated by :
     */
    public ElfLoader(String libraryPaths) throws IOException{
        for(String path : libraryPaths.split(":")) {
            ArrayMap<String, File> caching = new ArrayMap<>();
            File[] filesInDir = new File(path).listFiles((pathname)->pathname.isFile() && pathname.exists());
            if(filesInDir != null) {
                for (File file : filesInDir) {
                    caching.put(file.getName(), file);
                }
                dirCache.add(caching);
            }else{
                Log.w("ElfLoader","Omitted directory during initialization: "+path);
            }
        }
        BufferedReader selfMapsReader = new BufferedReader(new FileReader(new File("/proc/self/maps")));
        String mapLine = null;
        while((mapLine = selfMapsReader.readLine()) != null) {
            if(mapLine.endsWith(".so")) {
                String map = mapLine.substring(mapLine.lastIndexOf("/")+1);
                addLoaded(map);
            }
        }
    }
    private void addLoaded(String libName) {
        if(!alreadyLoadedLibs.contains(libName)) {
            alreadyLoadedLibs.add(libName);
        }
    }
    /*
     * Loads a library by it's name, searching in the specified search paths.
     */
    public void loadLib(String libName) throws IOException {
        if(alreadyLoadedLibs.contains(libName))
            return;
        File library = getLibrary(libName);
        if(library == null) {
            Log.w("ElfSequencer","Library " +libName + " not found in search paths");
            return;
        }
        ElfFile file = ElfFile.from(library);
        ElfDynamicSection section = file.firstSectionByType(ElfDynamicSection.class);
        List<String> needed = section.getNeededLibraries();
        for(String neededLibrary : needed) {
            Log.i("ElfSequencer","Needed: "+neededLibrary);
            loadLib(neededLibrary);
        }
        Log.i("ElfLoader", "Loading library "+library.getName());
        loadNative(library.getAbsolutePath(), library.getName());
        alreadyLoadedLibs.add(libName);
    }

    public void loadNative(String absolutePath, String name) {
        System.load(absolutePath);
    }

    public File getLibrary(String libName) {
        for(ArrayMap<String, File> fileCache : dirCache) {
            if(fileCache.containsKey(libName))
                return fileCache.get(libName);
        }
        return null;
    }
}