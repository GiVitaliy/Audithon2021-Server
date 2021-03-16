package ru.audithon.common.helpers;

import com.google.common.collect.ListMultimap;

import java.util.*;

public class PrefixTree<T> {
    private HashMap<Character, PrefixTree<T>> children;
    private int level;
    private List<T> values;

    public void put(String key, T value) {
        Objects.requireNonNull(key, "key is null");

        PrefixTree<T> node = buildPathInternal(key);
        if (node.values == null) {
            node.values = new ArrayList<>();
        }
        node.values.add(value);
    }

    public List<T> get(String key) {
        Objects.requireNonNull(key, "key is null");

        PrefixTree<T> node = navigateInternal(key);

        if (node.level == key.length() && node.values != null) {
            return node.values;
        }

        return Collections.emptyList();
    }

    public boolean containsPrefix(String prefix) {
        Objects.requireNonNull(prefix, "prefix is null");

        PrefixTree<T> node = navigateInternal(prefix);

        return node.level == prefix.length();
    }

    public static <T> PrefixTree<T> fromListMultiMap(ListMultimap<String, T> multimap) {

        PrefixTree<T> root = new PrefixTree<>();

        multimap.asMap().keySet().forEach(key -> {
            multimap.get(key).forEach(val -> {
                root.put(key, val);
            });
        });

        return root;
    }

    private PrefixTree<T> navigateInternal(String key) {
        int level = 0;
        PrefixTree<T> current = this;
        while (level < key.length() && current.children != null && current.children.containsKey(key.charAt(level))) {
            current = current.children.get(key.charAt(level));
            level++;
        }
        return current;
    }

    private PrefixTree<T> buildPathInternal(String key) {
        PrefixTree<T> current = this;

        for (int level = 0; level < key.length(); level++) {
            if (current.children != null && current.children.containsKey(key.charAt(level))) {
                current = current.children.get(key.charAt(level));
            } else {
                if (current.children == null) {
                    current.children = new HashMap<>();
                }

                PrefixTree<T> newNode = new PrefixTree<>();
                newNode.level = level + 1;
                current.children.put(key.charAt(level), newNode);
                current = newNode;
            }
        }

        return current;
    }
}
