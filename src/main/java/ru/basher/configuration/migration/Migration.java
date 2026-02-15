package ru.basher.configuration.migration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.basher.configuration.CommentConfigurationSection;
import ru.basher.configuration.CommentFileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Migration {

    private final File dataFolder;
    private final List<MigratableFile> files;
    private final String versionFileName;
    private final String versionSection;
    private final boolean needBackup;

    public boolean migrateIfNeeded() {
        if(files.isEmpty()) return true;
        try {
            MigrationContext ctx = new MigrationContext(dataFolder);

            CommentConfigurationSection fsVersionFile = ctx.fs(versionFileName);
            CommentConfigurationSection resVersionFile = ctx.resource(versionFileName);

            int fsVersion = fsVersionFile.getInt(versionSection, 1);
            int resVersion = resVersionFile.getInt(versionSection, 1);

            if (fsVersion == resVersion) return true;

            for (MigratableFile file : files) {
                try {
                    file.migrate(ctx, fsVersion, resVersion);
                } catch (Exception ignored) {
                }
            }

            if(needBackup) ctx.backupFs(fsVersion);

            resVersionFile.set(versionSection, resVersion);
            for (MigratableFile file : files) {
                CommentFileConfiguration config = ctx.resource(file.fileName());
                ctx.save(file.fileName(), config);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static class Builder {
        private File dataFolder = null;
        private final List<MigratableFile> files = new ArrayList<>();
        private String versionFileName = null;
        private String versionSection = null;
        private boolean needBackup = false;

        public @NotNull Builder dataFolder(@NotNull File dataFolder) {
            this.dataFolder = dataFolder;
            return this;
        }

        public @NotNull Builder addFiles(@NotNull MigratableFile...files) {
            this.files.addAll(Arrays.asList(files));
            return this;
        }

        public @NotNull Builder versionFileName(@NotNull String versionFileName) {
            this.versionFileName = versionFileName;
            return this;
        }

        public @NotNull Builder versionSection(@NotNull String versionSection) {
            this.versionSection = versionSection;
            return this;
        }

        public @NotNull Builder needBackup(boolean needBackup) {
            this.needBackup = needBackup;
            return this;
        }

        public @NotNull Migration build() {
            if(dataFolder == null) throw new IllegalArgumentException("dataFolder is null");
            if(versionFileName == null) throw new IllegalArgumentException("versionFileName is null");
            if(versionSection == null) throw new IllegalArgumentException("versionSection is null");

            return new Migration(dataFolder, files, versionFileName, versionSection, needBackup);
        }

    }
}