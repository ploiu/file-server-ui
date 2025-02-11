package ploiu.search;

public enum TokenTypes {
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
    /**
     * portion of a tag name
     */
    TAG_NAME,
    SPACE,
    /**
     * normal-mode text for search title
     */
    NORMAL,
    /**
     * catch-all
     */
    UNKNOWN
}
