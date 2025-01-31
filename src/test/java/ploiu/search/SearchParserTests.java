package ploiu.search;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

class SearchParserTests {

  @Test
  @DisplayName("parse handles regular search")
  void testParseHandlesRegularSearch() throws Exception {
    fail();
  }

  @Test
  @DisplayName("parse strips extra spaces from search")
  void testParseStripsExtraSpacesFromSearch() throws Exception {
    fail();
  }

  @Test
  @DisplayName("parse brings all regular search parts together when separated")
  void testParseGetsEntireSearch() throws Exception {
    fail();
  }

  @Test
  @DisplayName("parse properly parses single tag")
  void testParseSingleTag() throws Exception {
    fail();
  }

  @Test
  @DisplayName("parse properly parses multiple tags")
  void testParseMultipleTags() throws Exception {
    fail();
  }

  @MethodSource("attributeArgs")
  @ParameterizedTest(name = "parse properly parses attribute {0} into {1}")
  void testParseAttributes(String searchString, List<Attribute> expected) {
    fail();
  }

  static Stream<Arguments> attributeArgs() {
    return Stream.of(
        Arguments.of("@size > small", new Attribute("size", EqualityOperator.GT, "small"))
    );
  }

}
