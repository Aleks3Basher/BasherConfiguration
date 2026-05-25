package ru.basher.configuration.migration;

import com.google.common.base.Charsets;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.basher.configuration.CommentFileConfiguration;
import ru.basher.configuration.migration.changes.MigrationChanges;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrationContext {

    private final File dataFolder;
    private final ClassLoader classLoader;

    private final Map<String, CommentFileConfiguration> fsConfigs = new HashMap<>();
    private final Map<String, CommentFileConfiguration> resourceConfigs = new HashMap<>();

    @Getter
    private final List<MigrationChanges> changes = new ArrayList<>();

    public MigrationContext(File dataFolder) {
        this.dataFolder = dataFolder;
        classLoader = getClass().getClassLoader();
    }

    public void addChanges(@NotNull MigrationChanges changes) {
        this.changes.add(changes);
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

}
