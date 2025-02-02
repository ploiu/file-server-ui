package ploiu.search;

public enum EqualityOperator {
    // string values are equivalent to the backend server
    LT,
    GT,
    EQ,
    NEQ,
    UNKNOWN;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

    public static EqualityOperator parse(String op) {
        return switch (op.trim()) {
            case "<" -> LT;
            case ">" -> GT;
            case "=", "==", "===" -> EQ;
            case "!=", "!==", "<>" -> NEQ;
            default -> UNKNOWN;
        };
    }
}
