package ru.basher.configuration.migration.changes;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import ru.basher.configuration.CommentConfigurationSection;
import ru.basher.configuration.CommentFileConfiguration;
import ru.basher.configuration.migration.MigrationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class MigrationChanges {

    protected final int fromVersion;
    protected Task setChangesTask;
    @Setter
    protected boolean commonRelocate = true;

    public MigrationChanges(int fromVersion) {
        this.fromVersion = fromVersion;
    }

    public void setChangesTask(@NotNull Task setChangesTask) {
        this.setChangesTask = setChangesTask;
    }

    public abstract void migrate(@NotNull MigrationContext ctx) throws Exception;

    protected void relocateCommonSections(@NotNull CommentConfigurationSection source, @NotNull CommentConfigurationSection target) {
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

    protected boolean listsWithSameElementTypes(@NotNull List<?> list1, @NotNull List<?> list2) {
        if (list1.isEmpty() && list2.isEmpty()) return true;
        if (list1.isEmpty() || list2.isEmpty()) return false;

        Class<?> class1 = list1.get(0).getClass();
        Class<?> class2 = list2.get(0).getClass();

        return class1 == class2;
    }

    @FunctionalInterface
    public interface Task {
        void setChanges(@NotNull CommentFileConfiguration from, @NotNull CommentFileConfiguration to);
    }

}
