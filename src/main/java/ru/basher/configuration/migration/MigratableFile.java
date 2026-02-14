package ru.basher.configuration.migration;

import org.jetbrains.annotations.NotNull;

public interface MigratableFile {

    @NotNull String fileName();

    void migrate(@NotNull MigrationContext ctx, int fromVersion, int toVersion) throws Exception;

}
