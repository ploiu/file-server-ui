package ploiu.search;

import java.util.Collection;
import java.util.Optional;

public record Search(Optional<String> text, Collection<String> tags, Collection<Attribute> attributes) {
}
