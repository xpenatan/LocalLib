package com.xpenatan.gradle.locallib.internal;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.UnknownConfigurationException;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.artifacts.dependencies.DefaultDependencyArtifact;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency;
import org.gradle.api.internal.file.collections.DefaultConfigurableFileCollection;
import org.gradle.api.invocation.Gradle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class LocalLib {

    private static final String PROPERTY_LIB_REPLACE = "locallib.replace";
    private static final String PROPERTY_LIB_INCLUDE_BUILD = "locallib.includeBuild";

    private HashMap<String, String> dependencyToChange = new HashMap<>();
    private ArrayList<String> includeBuild = new ArrayList<>();

    public void init(Settings settings) {

        configureGradleProperties(settings);

        Gradle gradle = settings.getGradle();

        gradle.afterProject(new Action<Project>() {
            @Override
            public void execute(Project project) {
                processProject(project);
            }
        });

        gradle.rootProject(new Action<Project>() {
            @Override
            public void execute(Project project) {


                project.afterEvaluate(new Action<Project>() {
                    @Override
                    public void execute(Project project) {


                        System.out.println("");
                    }
                });

                System.out.println("");
            }
        });
    }

    private void configureGradleProperties(Settings settings) {
        List<Properties> allGradleProperties = PropertiesUtil.findAllGradleProperties(settings);

        Iterator<Properties> it = allGradleProperties.iterator();

        while(it.hasNext()) {
            Properties next = it.next();
            String propertyReplace = next.getProperty(LocalLib.PROPERTY_LIB_REPLACE, "").trim();
            String propertyIncludeBuild = next.getProperty(LocalLib.PROPERTY_LIB_INCLUDE_BUILD, "").trim();

            if(!propertyReplace.isEmpty())
                configureGradlePropertyDependencies(propertyReplace);
            if(!propertyIncludeBuild.isEmpty())
                configureGradlePropertyIncludeBuild(settings, propertyIncludeBuild);
        }

//        String toChange = "files(\"D:\\Dev\\Projects\\Eclipse\\Libgdx\\/gdx/libs/gdx-natives.jar\")";
//        dependencyToChange.put("com.badlogicgames.gdx:gdx-platform:1.9.10-SNAPSHOT:natives-desktop", toChange);
    }

    private void configureGradlePropertyDependencies(String properties) {

        String[] split1 = properties.split(",");

        for(int i = 0; i < split1.length; i++) {
            String property = split1[i];
            if(property.contains("=")) {
                String[] split = property.split("=");
                if(split.length == 2) {
                    String key = split[0].trim();
                    if(!key.isEmpty()) {
                        String value = split[1].trim().replace("\"", "");
                        if(!value.isEmpty()) {
                            dependencyToChange.put(key, value);
                        }
                    }
                }
            }
        }
    }

    private void configureGradlePropertyIncludeBuild(Settings settings, String property) {
        String[] split = property.split(",");
        for(int i = 0; i < split.length; i++) {
            String dir = split[i].trim();
            if(!dir.isEmpty()) {
                includeBuild.add(dir);
            }
        }

        for(int i = 0; i < includeBuild.size(); i++) {
            String dir = includeBuild.get(i);
            settings.includeBuild(dir);
        }
    }

    private void processProject(Project project) {
        try {
            ConfigurationContainer configurations = project.getConfigurations();
            Configuration implementation = configurations.getByName("implementation");
            DependencySet dependencies = implementation.getDependencies();
            processDependencies(project, dependencies);
        }
        catch (UnknownConfigurationException e) {}
    }

    private void processDependencies(Project project, DependencySet dependencies) {

        Iterator<Dependency> iterator = dependencies.iterator();

        ArrayList<Dependency> toAdd = new ArrayList<>();

        while(iterator.hasNext()) {
            Dependency next = iterator.next();

            Dependency newDependency = processDependency(project, next);
            boolean toRemove = false;
            if(newDependency != null) {
                toAdd.add(newDependency);
                toRemove = true;
            }
            if(toRemove)
                iterator.remove();
        }

        int size = toAdd.size();
        for(int i = 0; i < size; i++) {
            dependencies.add(toAdd.get(i));
        }
        toAdd.clear();
    }

    /**
     * returning a new dependency will remove the old one
     */
    private Dependency processDependency(Project project, Dependency dependency) {

        String toChange = null;
        String group = dependency.getGroup();
        String name = dependency.getName();
        String version = dependency.getVersion();

        if(dependency instanceof DefaultExternalModuleDependency) {
            DefaultExternalModuleDependency dep = (DefaultExternalModuleDependency)dependency;
            String key = group + ":" + name + ":" + version;
            Set<DependencyArtifact> artifacts = dep.getArtifacts();
            if(artifacts != null) {
                Iterator<DependencyArtifact> iterator = artifacts.iterator();
                while(iterator.hasNext()) {
                    DependencyArtifact next = iterator.next();
                    String classifier = next.getClassifier();
                    key += ":" + classifier;
                }
            }
            toChange = dependencyToChange.get(key);
        }
        else if(dependency instanceof DefaultSelfResolvingDependency) {


        }
        else if(dependency instanceof DefaultProjectDependency) {

        }


        Dependency depToChange = null;
        if(toChange != null) {
            toChange = toChange.trim();
            if(!toChange.isEmpty())
                depToChange = updateDependency(project, toChange);
        }

        return depToChange;
    }

    private Dependency updateDependency(Project project, String newDependency) {

        if(newDependency.startsWith("files")) {
            newDependency = newDependency.replace("files(" , "");
            newDependency = newDependency.replace(")" , "");
            newDependency = newDependency.replace("\"" , "");
            return LocalLib.createFileDependency(project, newDependency);
        }

        return null;
    }

    private void processProjectRoot() {

    }

    private static Dependency createModuleDependency(String group, String name, String version, String artifact) {
        DefaultExternalModuleDependency dependency = new DefaultExternalModuleDependency(group, name, version, "default");
        if(artifact != null && !artifact.isEmpty()) {
            dependency.addArtifact(new DefaultDependencyArtifact(name, "jar", "jar", artifact, null));
        }
        return dependency;
    }

    private static Dependency createFileDependency(Project project, String path) {
        DefaultConfigurableFileCollection fileTree = (DefaultConfigurableFileCollection)project.files(path);
        return new DefaultSelfResolvingDependency(fileTree);
    }

}
