package com.teklif.system;

import java.io.File;

public class AppPathManager {

    private static final String APP_NAME = "TeklifProgrami";

    // ⭐ Kullanıcının AppData klasörü
    public static File getAppDataDir() {

        String userHome = System.getProperty("user.home");

        File dir = new File(userHome,
                "AppData" + File.separator +
                "Local" + File.separator +
                APP_NAME);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    // ⭐ DB yolu
    public static File getDatabaseFile() {
        return new File(getAppDataDir(), "teklif.db");
    }
}