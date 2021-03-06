package org.shipkit.internal.gradle.plugin;

import com.gradle.publish.PluginBundleExtension;
import com.gradle.publish.PluginConfig;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

/**
 * This plugin discovers gradle plugins and adds them to the {@link PluginBundleExtension}.
 *
 * Maintaining plugins manually is error-prone. E.g. someone might easily forget about adding a new plugin. This plugin
 * will automatically pick up available gradle plugins (discovered via properties files in META-INF/gradle-plugins) and
 * will configure the pluginBundle extension (provided via 'com.gradle.plugin-publish' plugin) accordingly.
 */
public class PluginDiscoveryPlugin implements Plugin<Project> {

    private static Logger LOG = Logging.getLogger(PluginDiscoveryPlugin.class);
    private static final String DOT_PROPERTIES = ".properties";

    @Override
    public void apply(final Project project) {
        project.getPlugins().withId("com.gradle.plugin-publish", new Action<Plugin>() {
            @Override
            public void execute(Plugin plugin) {
                PluginBundleExtension extension = project.getExtensions().findByType(PluginBundleExtension.class);

                Set<File> pluginPropertyFiles = discoverGradlePluginPropertyFiles(project);
                LOG.lifecycle("  Adding {} discovered Gradle plugins to 'pluginBundle'", pluginPropertyFiles.size());
                for (File pluginPropertyFile : pluginPropertyFiles) {
                    PluginConfig config = new PluginConfig(generatePluginName(pluginPropertyFile.getName()));
                    config.setId(pluginPropertyFile.getName().substring(0, pluginPropertyFile.getName().lastIndexOf(DOT_PROPERTIES)));
                    config.setDisplayName(getImplementationClass(pluginPropertyFile));
                    LOG.info("Discovered plugin " + config);
                    extension.getPlugins().add(config);
                }
            }
        });
    }

    private Set<File> discoverGradlePluginPropertyFiles(Project project) {
        final JavaPluginConvention java = project.getConvention().getPlugin(JavaPluginConvention.class);
        FileTree resources = java.getSourceSets().getByName("main").getResources();
        FileTree plugins = resources.matching(new PatternSet().include("META-INF/gradle-plugins/*.properties"));
        return plugins.getFiles();
    }

    static String generatePluginName(String fileName) {
        String pluginName = fileName.substring(0, fileName.lastIndexOf(DOT_PROPERTIES));
        pluginName = pluginName.substring(pluginName.lastIndexOf(".") + 1);
        String[] split = pluginName.split("-");
        StringBuilder sb = new StringBuilder();
        for (String string : split) {
            if(sb.length() == 0) {
                sb.append(string.substring(0, 1).toLowerCase()).append(string.substring(1));
            } else {
                sb.append(string.substring(0, 1).toUpperCase()).append(string.substring(1));
            }
        }
        return sb.toString();
    }

    static String getImplementationClass(File file) {
        Properties properties = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            properties.load(is);
            return properties.getProperty("implementation-class");
        } catch (Exception e) {
            throw new RuntimeException("error while reading " + file, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
