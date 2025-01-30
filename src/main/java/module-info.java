// open is required to let the application access everything in src/main/resources
open module file.server.ui.main {

    requires static lombok;

    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires java.logging;
    requires org.slf4j;
    requires com.google.guice;
    requires java.desktop;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.media;
    requires org.jetbrains.annotations;
    // TODO remove these next 2 once we're off of apache http client
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires io.reactivex.rxjava3;
    requires org.pdfsam.rxjavafx;
    requires retrofit2;
    requires retrofit2.adapter.rxjava3;
    requires retrofit2.converter.jackson;
    requires okhttp3;
    // needed for okhttp3 which is needed for retrofit
    requires kotlin.stdlib;

    exports ploiu.ui to javafx.fxml, javafx.graphics;
    exports ploiu.event to javafx.fxml, javafx.graphics;
    exports ploiu to javafx.graphics;

    exports ploiu.client;
    exports ploiu.config;
    exports ploiu.exception;
    exports ploiu.model;
    exports ploiu.search to javafx.graphics;
}
