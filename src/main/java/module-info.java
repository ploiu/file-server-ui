module file.server.ui.main {
    requires static lombok;

    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.logging;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.slf4j;
    requires com.google.guice;

    opens ploiu.model to com.fasterxml.jackson.databind;
    opens ploiu.module to com.google.guice;

    exports ploiu.client;
    exports ploiu.config;
}