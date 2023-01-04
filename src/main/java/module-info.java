module file.server.ui.main {
    requires static lombok;

    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.logging;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.slf4j;

    opens ploiu.model to com.fasterxml.jackson.databind;
    exports ploiu.config;
}