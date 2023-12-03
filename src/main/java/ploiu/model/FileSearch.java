package ploiu.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Accessors(fluent = true)
public final class FileSearch {
    private final String query;
    private final Set<String> tags;

    FileSearch() {
        this.query = "";
        this.tags = new HashSet<>();
    }

    private FileSearch(String query, Set<String> tags) {
        this.query = query;
        this.tags = tags;
    }

    public static FileSearch fromInput(String input) {
        if (input == null) {
            return new FileSearch();
        }
        var cleaned = input
                .trim()
                // dedup spaces
                .replaceAll(" +", " ")
                // dedup +
                .replaceAll("\\++", "+")
                // split tags from each other (e.g. +tag1+tag2 -> +tag1 +tag2)
                .replaceAll("(?<!\\W)\\+", " +");
        if (cleaned.isBlank()) {
            return new FileSearch();
        }
        var split = cleaned.split(" ");
        var tags = Arrays.stream(split)
                .filter(it -> it.startsWith("+"))
                .map(it -> it.substring(1))
                .collect(Collectors.toSet());
        // get rid of anything that starts with + or @ (@ isn't used yet but will be for attributes)
        var query = cleaned.replaceAll("[+@].*?( |$)", "").trim();
        return new FileSearch(query, tags);
    }

    public boolean isEmpty() {
        return query.isBlank() && tags.isEmpty();
    }

    public String toQueryString() {
        var builder = new StringBuilder("?");
        if (!query.isBlank()) {
            builder.append("search=")
                    .append(query)
                    .append("&");
        }
        for (var tag : tags) {
            builder.append("tags=")
                    .append(tag)
                    .append("&");
        }
        return builder.toString().replaceAll("&$", "");
    }
}
