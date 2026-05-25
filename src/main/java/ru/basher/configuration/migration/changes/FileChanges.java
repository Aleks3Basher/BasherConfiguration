package ru.basher.configuration.migration.changes;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.basher.configuration.CommentFileConfiguration;
import ru.basher.configuration.migration.MigrationContext;

@Getter
public class FileChanges extends MigrationChanges {

    private final String oldFileName;
    private final String newFileName;

    public FileChanges(int fromVersion, String oldFileName, String newFileName) {
        super(fromVersion);
        this.oldFileName = oldFileName;
        this.newFileName = newFileName;
    }

    @Override
    public void migrate(@NotNull MigrationContext ctx) throws Exception {
        if(getSetChangesTask() == null) return;

        CommentFileConfiguration source = ctx.fs(oldFileName);
        CommentFileConfiguration target = ctx.resource(newFileName);
        getSetChangesTask().setChanges(source, target);
        if(commonRelocate) {
            relocateCommonSections(source, target);
        }
    }


}
