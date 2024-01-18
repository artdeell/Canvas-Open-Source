package git.artdeell.skymodloader.elfmod;

import android.util.ArrayMap;

import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfSectionHeader;
import net.fornwall.jelf.ElfStringTable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import git.artdeell.skymodloader.ElfLoader;
import git.artdeell.skymodloader.LibrarySelectorListener;

public class ElfRefcountLoader extends ElfLoader{
    private final List<ElfFileReference> elfFileReferences = new ArrayList<>();
    private final Map<String, ElfModMetadata> metadataByName = new ArrayMap<>();
    private final File modsFolder;
    public ElfRefcountLoader(String defaultPaths, File modsFolder) throws IOException {
        super(defaultPaths+":"+modsFolder.getAbsolutePath());
        this.modsFolder = modsFolder;
        if(!modsFolder.exists()) {
            modsFolder.mkdirs();
        }
    }

    public void load() throws IOException, InvalidModException {
        File[] modFiles = modsFolder.listFiles(new SharedObjectFileFilter());
        if(modFiles == null) return;
        for(File f : modFiles) {
            try {
                addElf(f);
            }catch (Exception e) {
                throw new InvalidModException("Failed to load mod" + f.getName());
            }
        }
        scanDeps();
        Collections.sort(elfFileReferences);
        for(ElfFileReference ref : elfFileReferences) {
            loadLib(ref.modMeta.name);
        }
    }

    @Override
    public void loadNative(String absolutePath, String name) {
        if(absolutePath.startsWith(modsFolder.getAbsolutePath())) {
            ElfModMetadata metadata = metadataByName.get(name);
            if(metadata == null) throw new IllegalStateException("WTF? No saved metadata for mod library " +name);
            LibrarySelectorListener.onModLibrary(absolutePath, metadata.displaysUI, metadata.displayName != null?metadata.displayName:metadata.name, metadata.dev, metadata.selfManagedUI);
        }else {
            super.loadNative(absolutePath, name);
        }
    }

    public void addElf(File file) throws Exception {
        ElfFile elf = ElfFile.from(file);
        RandomAccessFile raf = new RandomAccessFile(file,"r");
        ElfStringTable shstrtab = elf.getSectionNameStringTable();
        long secoff_config=-1; long secsz_config = -1;
        for(int i = 0; i < elf.e_shnum; i++) {
            ElfSectionHeader shdr = elf.getSection(i).header;
            String shname = shstrtab.get(shdr.sh_name);
            if(".config".equals(shname)) {
                secoff_config = shdr.sh_offset;
                secsz_config = shdr.sh_size;
            }
        }
        if(secoff_config == -1 || secsz_config == -1) {
            raf.close();
            throw new InvalidModException("no SEC_CONFIG in "+file.getName());
        }
        if(secsz_config > 0 && secsz_config < Integer.MAX_VALUE) {
            byte[] config = new byte[(int) secsz_config];
            raf.seek(secoff_config);
            raf.readFully(config);
            JSONObject jsonConfig = new JSONObject(new String(config, 0, config.length));
            ElfModMetadata modMetadata = new ElfModMetadata();
            modMetadata.name = jsonConfig.getString("name");
            modMetadata.majorVersion = jsonConfig.getInt("majorVersion");
            modMetadata.minorVersion = jsonConfig.getInt("minorVersion");
            modMetadata.patchVersion = jsonConfig.getInt("patchVersion");
            modMetadata.displayName = jsonConfig.optString("displayName");
            modMetadata.displaysUI = jsonConfig.optBoolean("displaysUI");
            modMetadata.dev = jsonConfig.optBoolean("dev");
            modMetadata.selfManagedUI = jsonConfig.optBoolean("selfManagedUI");
            JSONArray jdeps = jsonConfig.getJSONArray("dependencies");
            ElfModMetadata[] dependencies = new ElfModMetadata[jdeps.length()];
            for(int i = 0; i < dependencies.length; i++) {
                JSONObject jsonDependency = jdeps.getJSONObject(i);
                ElfModMetadata dependency = new ElfModMetadata();
                dependency.modIsValid = true;
                dependency.name = jsonDependency.getString("name");
                dependency.majorVersion = jsonDependency.getInt("majorVersion");
                dependency.minorVersion = jsonDependency.getInt("minorVersion");
                dependency.patchVersion = jsonDependency.getInt("patchVersion");
                dependencies[i] = dependency;
            }
            modMetadata.dependencies = dependencies;
            modMetadata.modIsValid = true;
            elfFileReferences.add(new ElfFileReference(modMetadata));
            metadataByName.put(modMetadata.name, modMetadata);
        }
        raf.close();
    }
    public void scanDeps() {
        ElfFileReference dummyReference = new ElfFileReference(null);
        for(ElfFileReference reference : elfFileReferences) {
            for(ElfModMetadata deps : reference.modMeta.dependencies) {
                dummyReference.modMeta = deps;
                int index = elfFileReferences.indexOf(dummyReference);
                if(index == -1) throw new IllegalStateException("Can't find dependency "+deps.name);
                else {
                    ElfFileReference target = elfFileReferences.get(index);
                    ElfModMetadata metadata = target.modMeta;
                    if(metadata.majorVersion != deps.majorVersion) throw new IllegalStateException(metadata.name+ ": Amajor "+metadata.majorVersion+" != Dmajor "+deps.majorVersion);
                    if(metadata.minorVersion < deps.minorVersion) throw new IllegalStateException(metadata.name+": Aminor "+metadata.minorVersion+" < Dminor "+deps.minorVersion);
                    target.referenceCount++;
                }
            }
        }
    }
}
