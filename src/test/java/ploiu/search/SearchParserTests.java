package ploiu.search;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchParserTests {

    @Test
    @DisplayName("parse handles regular search")
    void testParseHandlesRegularSearch() throws Exception {
        var input = "test";
        var expected = "test";
        var actual = SearchParser.parse(input).text().get();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("parse handles no search")
    void testParseHandlesNoSearch() throws Exception {
        var input = "@size > small";
        var expected = Optional.empty();
        var actual = SearchParser.parse(input).text();
        assertEquals(expected, actual);
    }


    @Test
    @DisplayName("parse strips extra spaces from search")
    void testParseStripsExtraSpacesFromSearch() throws Exception {
        var input = "\ttest      test ";
        var expected = "test test";
        var actual = SearchParser.parse(input).text().get();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("parse brings all regular search parts together when separated")
    void testParseGetsEntireSearch() throws Exception {
        var input = "test @size > small .gif";
        // there is a space after test and a space before .gif, so we get a space
        var expected = "test .gif";
        var actual = SearchParser.parse(input).text().get();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("parse properly parses single tag")
    void testParseSingleTag() throws Exception {
        var input = "+tag1";
        var expected = new ArrayList<>(List.of("tag1"));
        var actual = SearchParser.parse(input).tags();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("parse properly parses multiple tags")
    void testParseMultipleTags() throws Exception {
        var input = "+tag1 +tag2";
        var expected = new ArrayList<>(List.of("tag1", "tag2"));
        var actual = SearchParser.parse(input).tags();
        assertEquals(expected, actual);
    }

    @MethodSource("attributeArgs")
    @ParameterizedTest(name = "parse properly parses attribute {0} into {1}")
    void testParseAttributes(String searchString, List<Attribute> expected) {
        var actual = SearchParser.parse(searchString);
        assertEquals(expected, actual.attributes());
    }

    @MethodSource("fullSearchArgs")
    @ParameterizedTest(name = "parse properly parses full search {0} into {1}")
    void testParseFullThing(String input, Search expected) {
        var actual = SearchParser.parse(input);
        assertEquals(expected, actual);
    }

    static Stream<Arguments> attributeArgs() {
        return Stream.of(
                Arguments.of(
                        "@size > small", List.of(new Attribute("fileSize", EqualityOperator.GT, "small")),
                        "@date <> 2025-02-01 @size < Large", List.of(new Attribute("dateCreated", EqualityOperator.NEQ, "2025-02-01"), new Attribute("fileSize", EqualityOperator.LT, "Large"))
                )
        );
    }

    static Stream<Arguments> fullSearchArgs() {
        return Stream.of(
                Arguments.of("blah blah blah +tag1 @size > small +tag2", new Search(Optional.of("blah blah blah"), List.of("tag1", "tag2"), List.of(new Attribute("fileSize", EqualityOperator.GT, "small")))),
                Arguments.of("\nblah blah blah +tag1 @size>small  +tag2 blah", new Search(Optional.of("blah blah blah blah"), List.of("tag1", "tag2"), List.of(new Attribute("fileSize", EqualityOperator.GT, "small"))))
        );
    }
}
