package ru.basher.configuration;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface CommentConfigurationSection {

    @NotNull Map<String, Object> getMap();

    boolean contains(@NotNull String section);

    default boolean contains(@NotNull String section, boolean ignoreDefault) {
        return contains(section);
    }

    @NotNull String getCurrentPath();

    @NotNull String getName();

    @NotNull CommentConfigurationSection getRoot();

    @NotNull CommentConfigurationSection getParent();

    @Nullable Object get(@NotNull String section);

    @Nullable Object get(@NotNull String section, @Nullable Object def);

    void set(@NotNull String section, @NotNull Object value);

    void remove(@NotNull String section);

    @NotNull CommentConfigurationSection createSection(@NotNull String section);

    @Nullable String getString(@NotNull String section);

    @Contract("_, !null -> !null")
    @Nullable String getString(@NotNull String section, @Nullable String def);

    boolean isString(@NotNull String section);

    int getInt(@NotNull String section);

    int getInt(@NotNull String section, int def);

    boolean isInt(@NotNull String section);

    boolean getBoolean(@NotNull String section);

    boolean getBoolean(@NotNull String section, boolean def);

    boolean isBoolean(@NotNull String section);

    double getDouble(@NotNull String section);

    double getDouble(@NotNull String section, double def);

    boolean isDouble(@NotNull String section);

    long getLong(@NotNull String section);

    long getLong(@NotNull String section, long def);

    boolean isLong(@NotNull String section);

    @Nullable <T> List<T> getList(@NotNull String section, @NotNull Class<T> type);

    @Contract("_, _, !null -> !null")
    @Nullable <T> List<T> getList(@NotNull String section, @NotNull Class<T> type, @Nullable List<T> def);

    boolean isList(@NotNull String section);

    @NotNull List<String> getStringList(@NotNull String section);

    @NotNull List<Integer> getIntegerList(@NotNull String section);

    @NotNull List<Boolean> getBooleanList(@NotNull String section);

    @NotNull List<Double> getDoubleList(@NotNull String section);

    @NotNull List<Float> getFloatList(@NotNull String section);

    @NotNull List<Long> getLongList(@NotNull String section);

    @NotNull List<Byte> getByteList(@NotNull String section);

    @NotNull List<Character> getCharacterList(@NotNull String section);

    @NotNull List<Short> getShortList(@NotNull String section);

    <T> @Nullable T getObject(@NotNull String section, @NotNull Class<T> type);

    @Contract("_, _, !null -> !null")
    <T> @Nullable T getObject(@NotNull String section, @NotNull Class<T> type, @Nullable T def);


    @Nullable CommentConfigurationSection getConfigurationSection(@NotNull String section);

    boolean isConfigurationSection(@NotNull String section);

}
