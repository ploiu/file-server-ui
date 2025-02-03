package ploiu.search;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttributeTests {

    @ParameterizedTest(name = "{0} should be mapped to dateCreated")
    @ValueSource(strings = {"dAte", "date", "dateCreated", "createDate"})
    void testMapFieldForDateCreated(String input) {
        assertEquals("dateCreated", new Attribute(input, EqualityOperator.EQ, "").getField());
    }

    @ParameterizedTest(name = "{0} should be mapped to fileSize")
    @ValueSource(strings = {"sIze", "size", "length", "fileSize"})
    void testMapFieldForFileSize(String input) {
        assertEquals("fileSize", new Attribute(input, EqualityOperator.EQ, "").getField());
    }

    @ValueSource(strings = {"tYpe", "type", "fileType"})
    @ParameterizedTest(name = "{0} should be mapped to fileType")
    void testMapFieldForFileType(String input) {
        assertEquals("fileType", new Attribute(input, EqualityOperator.EQ, "").getField());
    }
}
