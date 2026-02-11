package ru.basher.configuration;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class CommentMemorySection implements CommentConfigurationSection {

    public static final char PATH_SEPARATOR = '.';

    private final @Nullable CommentConfigurationSection parent;
    private final String name;
    private final Map<String, Object> map;

    public CommentMemorySection(@Nullable CommentConfigurationSection parent, String name) {
        this(parent, name, new LinkedHashMap<>());
    }

    public CommentMemorySection(@Nullable CommentConfigurationSection parent, String name, Map<String, Object> map) {
        this.parent = parent;
        this.name = name;
        this.map = map;
    }

    @Override
    public boolean contains(@NotNull String section) {
        return get(section) != null;
    }

    @Override
    public boolean contains(@NotNull String section, boolean ignoreDefault) {
        return contains(section);
    }

    @Override
    public @NotNull String getCurrentPath() {
        StringBuilder path = new StringBuilder();
        CommentConfigurationSection parent;
        boolean first = true;
        while ((parent = getParent()) != null) {
            if (!first) {
                path.insert(0, parent.getName() + ".");
            } else {
                path.append(parent.getName());
                first = false;
            }
        }
        return path.toString();
    }

    @Override
    public @NotNull CommentConfigurationSection getRoot() {
        CommentConfigurationSection parent = getParent();
        if (parent == null) {
            return this;
        } else {
            return parent.getRoot();
        }
    }

    @Override
    public @Nullable Object get(@NotNull String path) {
        return get(path, null);
    }

    @Override
    public @Nullable Object get(@NotNull String path, @Nullable Object def) {
        if(path.isEmpty()) return this;
        CommentConfigurationSection section = this;
        int i = -1;
        int b;
        while ((i = path.indexOf(PATH_SEPARATOR, b = i + 1)) != -1) {
            String node = path.substring(b, i);
            section = section.getConfigurationSection(node);
            if (section == null) {
                return def;
            }
        }

        String key = path.substring(b);
        if (section == this) {
            Object result = map.get(key);
            return result == null ? def : result;
        } else {
            return section.get(key, def);
        }
    }

    @Override
    public void set(@NotNull String path, @NotNull Object value) {
        CommentConfigurationSection section = this;
        int i = -1;
        int b;
        while ((i = path.indexOf(PATH_SEPARATOR, b = i + 1)) != -1) {
            String node = path.substring(b, i);
            CommentConfigurationSection subSection = section.getConfigurationSection(node);
            if (subSection == null) {
                section = section.createSection(node);
            } else {
                section = subSection;
            }
        }

        String key = path.substring(b);
        if (section == this) {
            map.put(key, value);
        } else {
            section.set(key, value);
        }
    }

    @Override
    public void remove(@NotNull String path) {
        if(path.indexOf(PATH_SEPARATOR) != -1) {
            CommentConfigurationSection section = this;
            int i = -1;
            int b;
            while ((i = path.indexOf(PATH_SEPARATOR, b = i + 1)) != -1) {
                String node = path.substring(b, i);
                CommentConfigurationSection subSection = section.getConfigurationSection(node);
                if (subSection == null) break;
                section = subSection;
            }
            if(section == this) return;
            section.getParent().remove(section.getName());
        } else {
            map.remove(path);
        }
    }

    @Override
    public @NotNull CommentConfigurationSection createSection(@NotNull String path) {
        CommentConfigurationSection section = this;
        int i = -1;
        int b;
        while((i = path.indexOf(PATH_SEPARATOR, b = i + 1)) != -1) {
            String node = path.substring(b, i);
            CommentConfigurationSection subSection = section.getConfigurationSection(node);
            if (subSection == null) {
                section = section.createSection(node);
            } else {
                section = subSection;
            }
        }

        String key = path.substring(b);
        if (section == this) {
            CommentConfigurationSection newSection = new CommentMemorySection(this, path, new LinkedHashMap<>());
            map.put(key, newSection);
            return newSection;
        } else {
            return section.createSection(key);
        }
    }

    @Override
    public @Nullable String getString(@NotNull String section) {
        return getString(section, null);
    }

    @Override
    public @Nullable String getString(@NotNull String section, @Nullable String def) {
        Object obj = get(section);
        return obj instanceof String ? (String) obj : def;
    }

    @Override
    public boolean isString(@NotNull String section) {
        return get(section) instanceof String;
    }

    @Override
    public int getInt(@NotNull String section) {
        return getInt(section, 0);
    }

    @Override
    public int getInt(@NotNull String section, int def) {
        Object obj = get(section);
        return obj instanceof Number ? ((Number) obj).intValue() : def;
    }

    @Override
    public boolean isInt(@NotNull String section) {
        return get(section) instanceof Integer;
    }

    @Override
    public boolean getBoolean(@NotNull String section) {
        return getBoolean(section, false);
    }

    @Override
    public boolean getBoolean(@NotNull String section, boolean def) {
        Object obj = get(section);
        return obj instanceof Boolean ? (Boolean) obj : def;
    }

    @Override
    public boolean isBoolean(@NotNull String section) {
        return get(section) instanceof Boolean;
    }

    @Override
    public double getDouble(@NotNull String section) {
        return getDouble(section, 0);
    }

    @Override
    public double getDouble(@NotNull String section, double def) {
        Object obj = get(section);
        return obj instanceof Number ? ((Number) obj).doubleValue() : def;
    }

    @Override
    public boolean isDouble(@NotNull String section) {
        return get(section) instanceof Double;
    }

    @Override
    public long getLong(@NotNull String section) {
        return getLong(section, 0);
    }

    @Override
    public long getLong(@NotNull String section, long def) {
        Object obj = get(section);
        return obj instanceof Number ? ((Number) obj).longValue() : def;
    }

    @Override
    public boolean isLong(@NotNull String section) {
        return get(section) instanceof Long;
    }

    @Override
    public @Nullable <T> List<T> getList(@NotNull String section, @NotNull Class<T> type) {
        return getList(section, type, null);
    }

    @Override
    public @Nullable <T> List<T> getList(@NotNull String section, @NotNull Class<T> type, @Nullable List<T> def) {
        Object obj = get(section);
        if (!(obj instanceof List)) return def;
        List<?> list = (List<?>) obj;
        List<T> result = new ArrayList<>();
        for (Object o : list) {
            if (!type.isInstance(o)) continue;
            result.add(type.cast(o));
        }
        return result;
    }

    @Override
    public boolean isList(@NotNull String section) {
        return get(section) instanceof List;
    }

    @Override
    public @NotNull List<String> getStringList(@NotNull String section) {
        Object obj = get(section);
        if (!(obj instanceof List)) return new ArrayList<>();
        List<?> list = (List<?>) obj;
        List<String> result = new ArrayList<>();
        for (Object o : list) {
            result.add(o.toString());
        }
        return result;
    }

    @Override
    public @NotNull List<Integer> getIntegerList(@NotNull String section) {
        return getList(section, Integer.class, new ArrayList<>());
    }

    @Override
    public @NotNull List<Boolean> getBooleanList(@NotNull String section) {
        return getList(section, Boolean.class, new ArrayList<>());
    }

    @Override
    public @NotNull List<Double> getDoubleList(@NotNull String section) {
        return getList(section, Double.class, new ArrayList<>());
    }

    @Override
    public @NotNull List<Float> getFloatList(@NotNull String section) {
        return getList(section, Float.class, new ArrayList<>());
    }

    @Override
    public @NotNull List<Long> getLongList(@NotNull String section) {
        return getList(section, Long.class, new ArrayList<>());
    }

    @Override
    public @NotNull List<Byte> getByteList(@NotNull String section) {
        return getList(section, Byte.class, new ArrayList<>());
    }

    @Override
    public @NotNull List<Character> getCharacterList(@NotNull String section) {
        return getList(section, Character.class, new ArrayList<>());
    }

    @Override
    public @NotNull List<Short> getShortList(@NotNull String section) {
        return getList(section, Short.class, new ArrayList<>());
    }

    @Override
    public <T> @Nullable T getObject(@NotNull String section, @NotNull Class<T> type) {
        return getObject(section, type, null);
    }

    @Override
    public <T> @Nullable T getObject(@NotNull String section, @NotNull Class<T> type, @Nullable T def) {
        Object obj = get(section);
        if (obj == null) return def;
        return type.isInstance(obj) ? type.cast(obj) : def;
    }

    @Override
    public @Nullable CommentConfigurationSection getConfigurationSection(@NotNull String section) {
        Object obj = get(section);
        return obj instanceof CommentConfigurationSection ? (CommentConfigurationSection) obj : null;
    }

    @Override
    public boolean isConfigurationSection(@NotNull String section) {
        return get(section) instanceof CommentConfigurationSection;
    }
}
