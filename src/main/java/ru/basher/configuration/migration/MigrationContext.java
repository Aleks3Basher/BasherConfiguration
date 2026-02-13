package ru.basher.configuration.migration;

import com.google.common.base.Charsets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.basher.configuration.CommentConfigurationSection;
import ru.basher.configuration.CommentFileConfiguration;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrationContext {

    private final File dataFolder;
    private final ClassLoader classLoader;

    private final Map<String, CommentFileConfiguration> fsConfigs = new HashMap<>();
    private final Map<String, CommentFileConfiguration> resourceConfigs = new HashMap<>();

    public MigrationContext(File dataFolder) {
        this.dataFolder = dataFolder;
        classLoader = getClass().getClassLoader();
    }

    public @NotNull CommentFileConfiguration fs(@NotNull String fileName) throws Exception {
        CommentFileConfiguration result = fsConfigs.get(fileName);
        if (result != null) return result;

        File file = new File(dataFolder, fileName);
        if (!file.exists()) throw new FileNotFoundException(fileName + " does not exist");

        CommentFileConfiguration config = new CommentFileConfiguration();
        config.load(file);
        fsConfigs.put(fileName, config);
        return config;
    }

    public @NotNull CommentFileConfiguration resource(@NotNull String fileName) throws Exception {
        CommentFileConfiguration result = resourceConfigs.get(fileName);
        if (result != null) return result;

        try (InputStream in = getResource(fileName)) {
            if (in == null) throw new IllegalStateException("Resource not found: " + fileName);

            CommentFileConfiguration config = new CommentFileConfiguration();
            config.load(new InputStreamReader(in, Charsets.UTF_8));
            resourceConfigs.put(fileName, config);
            return config;
        }
    }

    private @Nullable InputStream getResource(@NotNull String fileName) {
        try {
            URL url = classLoader.getResource(fileName);
            if (url == null) {
                return null;
            } else {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            }
        } catch (IOException e) {
            return null;
        }
    }

    public void save(@NotNull String fileName, @NotNull CommentFileConfiguration config) {
        File file = new File(dataFolder, fileName);
        config.save(file);
    }

    public void backupFs(int fsVersion) {
        for (Map.Entry<String, CommentFileConfiguration> entry : fsConfigs.entrySet()) {
            File file = new File(dataFolder, entry.getKey());
            File renamedFile = new File(dataFolder, file.getName().replace(".yml", "-backup-v" + fsVersion + ".yml"));
            if (renamedFile.exists()) renamedFile.delete();
            file.renameTo(renamedFile);
        }
    }

    public void relocateCommonSections(@NotNull CommentConfigurationSection source, @NotNull CommentConfigurationSection target) {
        Map<String, Object> sourceMap = source.getMap();

        for (Map.Entry<String, Object> entry : new HashMap<>(target.getMap()).entrySet()) {
            Object targetValue = entry.getValue();
            Object sourceValue = sourceMap.get(entry.getKey());

            if (sourceValue == null) continue;
            if (sourceValue.getClass() != targetValue.getClass()) continue;

            if (targetValue instanceof CommentConfigurationSection) {
                relocateCommonSections((CommentConfigurationSection) sourceValue, (CommentConfigurationSection) targetValue);
            } else if (targetValue instanceof List) {
                if (listsWithSameElementTypes((List<?>) sourceValue, (List<?>) targetValue)) {
                    target.getMap().put(entry.getKey(), sourceValue);
                }
            } else {
                target.getMap().put(entry.getKey(), sourceValue);
            }
        }
    }

    private boolean listsWithSameElementTypes(@NotNull List<?> list1, @NotNull List<?> list2) {
        if (list1.isEmpty() && list2.isEmpty()) return true;
        if (list1.isEmpty() || list2.isEmpty()) return false;

        Class<?> class1 = list1.get(0).getClass();
        Class<?> class2 = list2.get(0).getClass();

        return class1 == class2;
    }

}
