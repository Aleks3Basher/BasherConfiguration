package ru.basher.configuration.migration.changes;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.basher.configuration.CommentFileConfiguration;
import ru.basher.configuration.migration.MigrationContext;

@Getter
public class SectionChanges extends MigrationChanges {

    private final String fileName;

    public SectionChanges(int fromVersion, String fileName) {
        super(fromVersion);
        this.fileName = fileName;
    }

    @Override
    public void migrate(@NotNull MigrationContext ctx) throws Exception {
        if(getSetChangesTask() == null) return;

        CommentFileConfiguration source = ctx.fs(fileName);
        CommentFileConfiguration target = ctx.resource(fileName);
        getSetChangesTask().setChanges(source, target);
        if(commonRelocate) {
            relocateCommonSections(source, target);
        }
    }

}
