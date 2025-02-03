package ploiu.search;

import java.util.Collection;

public record Search(String text, Collection<String> tags, Collection<Attribute> attributes) {
}
