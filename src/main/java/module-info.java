module file.server.ui.main {
    requires static lombok;

    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires java.logging;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.slf4j;
    requires com.google.guice;
    requires java.desktop;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.media;
    requires org.jetbrains.annotations;

    opens ploiu.model to com.fasterxml.jackson.databind;
    opens ploiu.module to com.google.guice;
    opens ploiu.ui to javafx.fxml;
    opens ploiu to javafx.graphics;

    exports ploiu.ui to javafx.fxml, javafx.graphics;
    exports ploiu to javafx.graphics;

    exports ploiu.client;
    exports ploiu.config;
    exports ploiu.exception;
    exports ploiu.model;
}
