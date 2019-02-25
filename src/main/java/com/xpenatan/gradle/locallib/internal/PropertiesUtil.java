package com.xpenatan.gradle.locallib.internal;

import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertiesUtil {

    private static final String PROPERTY_GRADLE_LOCAL = "local.properties";
    private static final String PROPERTY_GRADLE = "gradle.properties";

    public static List<Properties> findAllGradleProperties(Settings settings) {
        File projectDir = settings.getRootProject().getProjectDir();
        Properties usersGradleProperties = findUsersGradleProperties(settings.getGradle());
        Properties localProperties = findLocalProperties(projectDir);
        Properties gradleProperties = findGradleProperties(projectDir);
        ArrayList<Properties> list = new ArrayList<>();
        if(usersGradleProperties != null)
            list.add(usersGradleProperties);
        if(localProperties != null)
            list.add(localProperties);
        if(gradleProperties != null)
            list.add(gradleProperties);
        return list;
    }

    private static Properties findLocalProperties(File projectDirectory) {
        File localProperties = new File(projectDirectory, PropertiesUtil.PROPERTY_GRADLE_LOCAL);
        if (localProperties.exists())
            return readProperties(localProperties);
        return null;
    }

    private static Properties findUsersGradleProperties(Gradle gradle) {
        File gradleProperties = new File(gradle.getGradleHomeDir(), PropertiesUtil.PROPERTY_GRADLE);

        if (gradleProperties.exists()) {
            return readProperties(gradleProperties);
        }
        return null;
    }

    private static Properties findGradleProperties(File projectDirectory) {
        File localProperties = new File(projectDirectory, PropertiesUtil.PROPERTY_GRADLE);
        if (localProperties.exists()) {
            return readProperties(localProperties);
        }
        return null;
    }

    private  static Properties readProperties(File file) {
        Properties prof = new Properties();
        if (file.exists()) {
            try {
                prof.load(new FileInputStream(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return prof;
    }
}