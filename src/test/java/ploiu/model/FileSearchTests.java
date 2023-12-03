package ploiu.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileSearchTests {
    @ParameterizedTest(name = "Test that {0} is parses the query properly")
    @ValueSource(strings = {"test", "\ttest\t", " test ", "test +here'sATag @size>=1Mb", "+tag1 test +tag2"})
    void testFromInputParsesQuery(String query) {
        var search = FileSearch.fromInput(query);
        assertEquals("test", search.query());
    }

    @ParameterizedTest(name = "Test that {0} parses single tags properly")
    @ValueSource(strings = {"test +tag @size>=1Mb", "+tag\t"})
    void testFromInputParsesSingleTag(String query) {
        var search = FileSearch.fromInput(query);
        assertEquals(Set.of("tag"), search.tags());
    }

    @ParameterizedTest(name = "Test that {0} parses multiple tags properly")
    @ValueSource(strings = {"+tag1 +tag2", "what +tag1 +tag2", "+tag1 query +tag2", "+tag1+tag2"})
    void testFromInputParsesMultipleTags(String query) {
        var search = FileSearch.fromInput(query);
        assertEquals(Set.of("tag1", "tag2"), search.tags());
    }

    @Test
    @DisplayName("Test that passing the same tag multiple times deduplicates it")
    void testFromInputDeduplicatesTags() {
        var search = FileSearch.fromInput("+tag1 +tag1");
        assertEquals(Set.of("tag1"), search.tags());
    }

    @Test
    @DisplayName("Test that a file search without any values is empty")
    void testIsEmpty() {
        var search = new FileSearch();
        assertTrue(search.isEmpty());
    }

    @ParameterizedTest(name = "Test that a file search with anything in it is not empty")
    @ValueSource(strings = {"query", "+tag"})
    void testIsEmptyNotEmpty(String query) {
        var search = FileSearch.fromInput(query);
        assertFalse(search.isEmpty());
    }

    @Test
    @DisplayName("test that fileSearch properly creates a query string for query part")
    void testToQueryStringQuery() {
        var search = FileSearch.fromInput("test.txt");
        assertEquals("?search=test.txt", search.toQueryString());
    }

    @Test
    @DisplayName("test that fileSearch properly creates a query string for tag part")
    void testToQueryStringTags() {
        var search = FileSearch.fromInput("+tag1 +tag2");
        assertEquals("?tags=tag1&tags=tag2", search.toQueryString());
    }

    @Test
    @DisplayName("test that fileSearch properly creates full query string")
    void testToQueryStringFull() {
        var search = FileSearch.fromInput("query +tag1 +tag2");
        assertEquals("?search=query&tags=tag1&tags=tag2", search.toQueryString());
    }
}
