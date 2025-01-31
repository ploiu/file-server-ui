package ploiu.search;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public final class Attribute {
    private final String field;
    private final EqualityOperator op;
    private final String value;

    @Override
    public String toString() {
        return field + "." + op.toString() + ";" + value;
    }
}
