package ploiu.search;

public enum EqualityOperator {
    // string values are equivalent to the backend server
    LT("lt"),
    GT("gt"),
    EQ("eq"),
    NEQ("neq"),
    UNKNOWN("unknown");

    private final String value;

    EqualityOperator(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static EqualityOperator parse(String op) {
        return switch(op.trim()) {
            case "<" -> LT;
            case ">" -> GT;
            case "=", "==", "===" -> EQ;
            case "!=", "!==", "<>" -> NEQ;
            default -> UNKNOWN;
        };
    }
}
