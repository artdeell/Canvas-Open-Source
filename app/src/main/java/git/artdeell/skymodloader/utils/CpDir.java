package git.artdeell.skymodloader.utils;


import android.util.Log;

import java.io.*;

public class CpDir {
    public long begin = 0;
    public long end = 0;
    public String dir1, dir2;

    public CpDir(String from, String to) throws Exception {
        this.dir1 = from;
        this.dir2 = to;
        try {
            copyDir(new File(from), new File(to));
        }catch(Exception e){
            throw e;
        }
    }

    public static void copyFile(File file, File file1) {
        try{
            if(!file1.exists()){
                file1.delete();
                file1.mkdirs();
            }
            if(file1.isDirectory()){
                file1.delete();
                file1.createNewFile();
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        try (FileInputStream fis = new FileInputStream(file); FileOutputStream fos = new FileOutputStream(file1)) {
            byte[] bys = new byte[1024];
            int len;
            while ((len = fis.read(bys)) != -1) {
                fos.write(bys, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void copyDir(File file, File file1) throws Exception {
        try {
            if (!file.isDirectory()) {
                throw new Exception("Cannot find library dir.");
            }
            if (!file1.exists()) {
                file1.mkdirs();
            }
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    copyDir(f, new File(file1.getPath(), f.getName()));
                } else if (f.isFile()) {
                    copyFile(f, new File(file1.getPath(), f.getName()));
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static void createFile(String path) {
        File mFile = new File(path);
        if (mFile.exists()) {
            mFile.delete();
        }
        try {
            mFile.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

