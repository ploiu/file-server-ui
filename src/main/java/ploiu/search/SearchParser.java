package ploiu.search;


/**
 * Parses a string input by the user into a search object that represents the type of search the backend can understand
 */
public final class SearchParser {
    private static final String OP_GEX = "[=<>!]";

    public static Search parse(String search) {
        var split = split(search);
        throw new UnsupportedOperationException("unimplemented");
    }

    enum TokenTypes {
        /**
         * start of an attribute portion ("@")
         */
        ATTRIBUTE_START,
        /**
         * name of an attribute portion
         */
        ATTRIBUTE_NAME,
        /**
         * fragment or whole part of attribute operator see {@link EqualityOperator}
         */
        ATTRIBUTE_OP,
        /**
         * fragment or whole part of the value of an attribute see {@link Attribute}
         */
        ATTRIBUTE_VALUE,
        /**
         * start of a tag portion ("+")
         */
        TAG_START,
        /** portion of a tag name */
        TAG_NAME,
        SPACE,
        /**
         * normal-mode text for search title
         */
        NORMAL,
        /**
         * catch-all
         */
        UNKNOWN;
    }

    /**
     * the current mode when parsing chars into tokens
     */
    enum Modes {
        FILE_NAME,
        ATTRIBUTE_NAME,
        ATTRIBUTE_OP,
        ATTRIBUTE_VALUE,
        TAG_NAME,
        /**
         * the default mode, set whenever we encounter a space and are either in "FILE_NAME", "ATTRIBUTE_VALUE", or "TAG_NAME"
         */
        UNSET;
    }

    record Token(char value, TokenTypes type) {
    }

    record TokenAndMode(Token token, Modes mode) {
    }

    /**
     * parses the passed search string into a collection of tokens to aid in parsing
     * the search into something that can be translated into what the server requires for searching
     *
     * @param search
     * @return a Token array. This array will have the same length as the input string in the same order of chars as the input string
     */
    static Token[] split(String search) {
        var tokens = new Token[search.length()];
        // stripping extra spaces will let us more easily tokenize and parse later
        var chars = search.trim().replaceAll(" +", " ").toCharArray();
        var mode = Modes.UNSET;
        TokenAndMode tokenAndMode = null;
        for (int i = 0; i < chars.length; i++) {
            var c = chars[i];
            tokenAndMode = switch (mode) {
                // unset can only ever lead into file name, attribute start, tag start, or more unset
                case Modes.UNSET -> handleUnset(c);
                // attribute name can only ever lead into attribute name or attribute op
                case Modes.ATTRIBUTE_NAME -> handleAttributeName(c);
                // file name can only ever lead to file name or unset
                case Modes.FILE_NAME -> handleFileName(c);
                // attribute op can only ever lead to attribute op or attribute value
                case Modes.ATTRIBUTE_OP -> handleAttributeOp(c);
                // attribute value can only lead to attribute value or unknown
                case Modes.ATTRIBUTE_VALUE -> handleAttributeValue(c);
                // tag name can only lead to tag name or unknown
                case Modes.TAG_NAME -> handleTagName(c);
            };
            tokens[i] = tokenAndMode.token();
            mode = tokenAndMode.mode();
        }
        return tokens;
    }

    static TokenAndMode handleUnset(char c) {
        return switch (c) {
            case '+' -> new TokenAndMode(new Token(c, TokenTypes.TAG_START), Modes.TAG_NAME);
            case '@' -> new TokenAndMode(new Token(c, TokenTypes.ATTRIBUTE_START), Modes.ATTRIBUTE_NAME);
            case ' ' -> new TokenAndMode(new Token(c, TokenTypes.SPACE), Modes.UNSET);
            default -> new TokenAndMode(new Token(c, TokenTypes.NORMAL), Modes.FILE_NAME);
        };
    }

    static TokenAndMode handleAttributeName(char c) {
        if (c == ' ') {
            return new TokenAndMode(new Token(c, TokenTypes.SPACE), Modes.ATTRIBUTE_OP);
        } else if (String.valueOf(c).matches(OP_GEX)) {
            return new TokenAndMode(new Token(c, TokenTypes.ATTRIBUTE_OP), Modes.ATTRIBUTE_OP);
        } else if (String.valueOf(c).matches("[a-zA-Z]")) {
            return new TokenAndMode(new Token(c, TokenTypes.ATTRIBUTE_NAME), Modes.ATTRIBUTE_NAME);
        } else {
            return new TokenAndMode(new Token(c, TokenTypes.UNKNOWN), Modes.ATTRIBUTE_NAME);
        }
    }

    static TokenAndMode handleFileName(char c) {
        if (c == ' ') {
            return new TokenAndMode(new Token(c, TokenTypes.SPACE), Modes.UNSET);
        } else {
            return new TokenAndMode(new Token(c, TokenTypes.NORMAL), Modes.FILE_NAME);
        }
    }

    static TokenAndMode handleAttributeOp(char c) {
        if (c == ' ') {
            return new TokenAndMode(new Token(c, TokenTypes.SPACE), Modes.ATTRIBUTE_VALUE);
        } else if (String.valueOf(c).matches(OP_GEX)) {
            return new TokenAndMode(new Token(c, TokenTypes.ATTRIBUTE_OP), Modes.ATTRIBUTE_OP);
        } else {
            return new TokenAndMode(new Token(c, TokenTypes.ATTRIBUTE_VALUE), Modes.ATTRIBUTE_VALUE);
        }
    }

    static TokenAndMode handleAttributeValue(char c) {
        if (c == ' ') {
            return new TokenAndMode(new Token(c, TokenTypes.SPACE), Modes.UNSET);
        } else {
            return new TokenAndMode(new Token(c, TokenTypes.ATTRIBUTE_VALUE), Modes.ATTRIBUTE_VALUE);
        }
    }

    static TokenAndMode handleTagName(char c) {
        if (c == ' ') {
            return new TokenAndMode(new Token(c, TokenTypes.SPACE), Modes.UNSET);
        } else {
            return new TokenAndMode(new Token(c, TokenTypes.TAG_NAME), Modes.TAG_NAME);
        }
    }
}
