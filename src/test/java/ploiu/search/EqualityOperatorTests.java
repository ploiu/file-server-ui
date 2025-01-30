package ploiu.search;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EqualityOperatorTests {

    @ValueSource(strings = {"=", "==", "==="})
    @ParameterizedTest(name = "{0} should parse into EQ")
    void testParseEq(String op) {
        assertEquals(EqualityOperator.EQ, EqualityOperator.parse(op));
    }

    @ValueSource(strings = {"!=", "!==", "<>"})
    @ParameterizedTest(name = "{0} should parse into NEQ")
    void testParseNeq(String op) {
        assertEquals(EqualityOperator.NEQ, EqualityOperator.parse(op));
    }

    @Test
    @DisplayName("< should parse into LT")
    void testParseLt() {
        assertEquals(EqualityOperator.LT, EqualityOperator.parse("<"));
    }

    @Test
    @DisplayName("> should parse into GT")
    void testParseGt() {
        assertEquals(EqualityOperator.GT, EqualityOperator.parse(">"));
    }

    @Test
    @DisplayName("literally anything else should parse into UNKNOWN")
    void testParseUnknown() {
        assertEquals(EqualityOperator.UNKNOWN, EqualityOperator.parse("as;dlkfjas;dlfkjasd;lfkjas;ldfkjas;dlfkj"));
    }

}
