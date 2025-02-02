package ploiu.search;


import java.util.ArrayList;
import java.util.Optional;

/**
 * Parses a string input by the user into a search object that represents the type of search the backend can understand
 */
public final class SearchParser {
    private static final String OP_GEX = "[=<>!]";

    /**
     * parses the passed String into a {@link Search} object to be passed to the backend server
     *
     * @param search
     * @return
     */
    public static Search parse(String search) {
        search = search.trim().replaceAll(" {2,}", " ");
        var tokens = tokenize(search);
        // searchText is always just gonna be a concatenation of normal text so it's ok to have 1 dedicated to it
        var searchText = new StringBuilder();
        var tags = new ArrayList<String>();
        var attributes = new ArrayList<Attribute>();
        for (int index = 0; index < tokens.length; ) {
            var current = tokens[index];
            if (current.type() == TokenTypes.NORMAL) {
                index = handleNormalTokens(tokens, index, searchText);
            } else if (current.type() == TokenTypes.TAG_START) {
                var builder = new StringBuilder();
                index = handleTagTokens(tokens, index, builder);
                tags.add(builder.toString());
            } else if (current.type() == TokenTypes.ATTRIBUTE_START) {
                var builder = Attribute.builder();
                index = handleAttributeTokens(tokens, index, builder);
                attributes.add(builder.build());
            } else {
                // unknown, skip
                index++;
            }
        }
        var cleanedText = searchText.toString().trim().replaceAll(" +", " ");
        return new Search(cleanedText.isEmpty() ? Optional.empty() : Optional.of(cleanedText), tags, attributes);
    }

    /**
     * splits the text up into different token types for syntax highlighting and to aid searching
     *
     * @param search
     * @return a Token array. This array will have the same length as the input string in the same order of chars as the input string
     */
    public static Token[] tokenize(String search) {
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

    /**
     * parses out the text in {@code tokens} as a normal search, starting from {@code start} index. The passed StringBuilder is populated with the text
     *
     * @param tokens  tokens pulled from {@link #tokenize(String)}
     * @param start   the start index to search through
     * @param builder this will have the contents of the tokens for normal text
     * @return the new index to iterate from in tokens
     */
    static int handleNormalTokens(Token[] tokens, int start, StringBuilder builder) {
        Token current;
        while (start < tokens.length && ((current = tokens[start]).type() == TokenTypes.NORMAL || current.type() == TokenTypes.SPACE)) {
            builder.append(current.value());
            start++;
        }
        return start;
    }

    /**
     * parses out the text in {@code tokens} into a tag, starting from {@code start} + 1 (because the first index could be a `+`, which isn't actually part of the tag)
     *
     * @param tokens  the tokens to parse through
     * @param start   the index of the tag start (`+` character)
     * @param builder will be populated with the tag name
     * @return the index to continue iterating from
     */
    static int handleTagTokens(Token[] tokens, int start, StringBuilder builder) {
        // first char is +, so skip it
        start++;
        Token current;
        while (start < tokens.length && (current = tokens[start++]).type() == TokenTypes.TAG_NAME) {
            builder.append(current.value());
        }
        return start;
    }

    static int handleAttributeTokens(Token[] tokens, int start, Attribute.AttributeBuilder builder) {
        // first char is @ which we don't need, so skip it
        start++;
        Token current;
        var nameBuilder = new StringBuilder();
        var opBuilder = new StringBuilder();
        var valueBuilder = new StringBuilder();
        while (start < tokens.length && (current = tokens[start]).type() == TokenTypes.ATTRIBUTE_NAME) {
            nameBuilder.append(current.value());
            start++;
        }
        // there could be spaces in between the name and operator, so we need to skip those
        while (start < tokens.length && (tokens[start]).type() != TokenTypes.ATTRIBUTE_OP) {
            start++;
        }
        while (start < tokens.length && (current = tokens[start]).type() == TokenTypes.ATTRIBUTE_OP) {
            opBuilder.append(current.value());
            start++;
        }
        // there could be spaces in between the operator and value, so we need to skip those
        while (start < tokens.length && (tokens[start]).type() != TokenTypes.ATTRIBUTE_VALUE) {
            start++;
        }
        while (start < tokens.length && (current = tokens[start]).type() == TokenTypes.ATTRIBUTE_VALUE) {
            valueBuilder.append(current.value());
            start++;
        }
        builder
                .field(nameBuilder.toString())
                .op(EqualityOperator.parse(opBuilder.toString()))
                .value(valueBuilder.toString());
        return start;
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

    record TokenAndMode(Token token, Modes mode) {
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
