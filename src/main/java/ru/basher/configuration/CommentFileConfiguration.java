package ru.basher.configuration;

import com.google.common.base.Charsets;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class CommentFileConfiguration extends CommentMemorySection {

    private final LoaderOptions loaderOptions = new LoaderOptions();

    private final Yaml yaml;
    private final Map<String, List<String>> comments = new HashMap<>();

    public CommentFileConfiguration() {
        super(null, "");
        loaderOptions.setProcessComments(true);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setProcessComments(true);
        dumperOptions.setIndent(2);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);

        Representer representer = new Representer(dumperOptions);
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        SafeConstructor constructor = new SafeConstructor(loaderOptions);

        yaml = new Yaml(constructor, representer, dumperOptions, loaderOptions);
    }

    public void load(@NotNull File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            load(new InputStreamReader(fis, Charsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void load(@NotNull Reader reader) {
        comments.clear();
        Composer composer = new Composer(
                new ParserImpl(new StreamReader(reader), loaderOptions),
                new Resolver(),
                loaderOptions
        );
        Node node = composer.getSingleNode();
        if (node instanceof MappingNode) {
            readMapping((MappingNode) node, "", this);
        }
    }

    private void readMapping(@NotNull MappingNode node, @NotNull String path, @NotNull CommentConfigurationSection target) {
        for (NodeTuple tuple : node.getValue()) {
            ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();

            String key = keyNode.getValue();
            Node valueNode = tuple.getValueNode();

            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (keyNode.getBlockComments() != null) {
                List<String> comments = new ArrayList<>();
                for (CommentLine c : keyNode.getBlockComments()) {
                    comments.add(c.getValue());
                }
                this.comments.put(fullPath, comments);
            }

            if (valueNode instanceof MappingNode) {
                CommentConfigurationSection section = target.createSection(key);
                readMapping((MappingNode) valueNode, fullPath, section);
            } else if (valueNode instanceof SequenceNode) {
                List<Object> list = new ArrayList<>();
                readSequence((SequenceNode) valueNode, list);
                target.set(key, list);
            } else if (valueNode instanceof ScalarNode) {
                target.set(key, parseScalar((ScalarNode) valueNode));
            }
        }
    }

    private void readSequence(@NotNull SequenceNode node, @NotNull List<Object> target) {
        for (Node element : node.getValue()) {
            if (element instanceof SequenceNode) {
                List<Object> list = new ArrayList<>();
                readSequence((SequenceNode) element, list);
                target.add(list);
            } else if (element instanceof ScalarNode) {
                target.add(parseScalar((ScalarNode) element));
            }
        }
    }

    private @NotNull Object parseScalar(@NotNull ScalarNode node) {
        Tag tag = node.getTag();
        String value = node.getValue();

        if (Tag.INT.equals(tag)) {
            return Integer.parseInt(value);
        }
        if (Tag.FLOAT.equals(tag)) {
            return Double.parseDouble(value);
        }
        if (Tag.BOOL.equals(tag)) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }




    public void save(@NotNull File file) {
        try {
            String data = saveToString();

            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), Charsets.UTF_8)) {
                writer.write(data);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull String saveToString() {
        MappingNode root = writeMapping(this, "");
        StringWriter writer = new StringWriter();

        yaml.serialize(root, writer);
        String serialized = writer.toString();

        if (serialized.equalsIgnoreCase("{}\n")) serialized = "";
        return serialized;
    }

    private MappingNode writeMapping(@NotNull CommentConfigurationSection source, @NotNull String path) {
        List<NodeTuple> tuples = new ArrayList<>();

        for (Map.Entry<String, Object> entry : source.getMap().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            String fullPath = path.isEmpty() ? key : path + "." + key;

            ScalarNode keyNode = new ScalarNode(
                    Tag.STR, key, null, null, DumperOptions.ScalarStyle.PLAIN
            );

            List<String> keyComments = comments.get(fullPath);
            if (keyComments != null) {
                List<CommentLine> block = new ArrayList<>();
                for (String c : keyComments) {
                    block.add(new CommentLine(null, null, c, CommentType.BLOCK));
                }
                keyNode.setBlockComments(block);
            }

            Node valueNode;
            if (value instanceof CommentConfigurationSection) {
                valueNode = writeMapping((CommentConfigurationSection) value, fullPath);
            } else if (value instanceof List<?>) {
                List<Node> nodes = new ArrayList<>();
                for (Object element : (List<?>) value) {
                    nodes.add(writeValueNode(element));
                }
                valueNode = new SequenceNode(Tag.SEQ, nodes, DumperOptions.FlowStyle.BLOCK);
            } else {
                valueNode = writeValueNode(value);
            }

            tuples.add(new NodeTuple(keyNode, valueNode));
        }

        return new MappingNode(Tag.MAP, tuples, DumperOptions.FlowStyle.BLOCK);
    }

    private @NotNull Node writeValueNode(@NotNull Object value) {
        if (value instanceof List<?>) {
            List<Node> nodes = new ArrayList<>();
            for (Object element : (List<?>) value) {
                nodes.add(writeValueNode(element));
            }
            return new SequenceNode(Tag.SEQ, nodes, DumperOptions.FlowStyle.BLOCK);
        }

        Tag tag;
        DumperOptions.ScalarStyle style;
        if (value instanceof Integer || value instanceof Long) {
            tag = Tag.INT;
            style = DumperOptions.ScalarStyle.PLAIN;

        } else if (value instanceof Double || value instanceof Float) {
            tag = Tag.FLOAT;
            style = DumperOptions.ScalarStyle.PLAIN;

        } else if (value instanceof Boolean) {
            tag = Tag.BOOL;
            style = DumperOptions.ScalarStyle.PLAIN;

        } else {
            tag = Tag.STR;
            style = DumperOptions.ScalarStyle.SINGLE_QUOTED;
        }

        return new ScalarNode(
                tag,
                String.valueOf(value),
                null,
                null,
                style
        );
    }

}
