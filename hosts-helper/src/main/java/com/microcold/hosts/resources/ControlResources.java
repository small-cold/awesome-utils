package com.microcold.hosts.resources;

import java.util.ResourceBundle;

/*
 * Created by MicroCold on 2017/9/14.
 */
public class ControlResources {
    // Translatable properties
    private static final String BASE_NAME = "resources/lang";

    // Non-translateable properties
    private static final String NT_BASE_NAME = "resources/lang-nt";

    // Do not cache the bundle here. It is cached by the ResourceBundle
    // class and may be updated if the default locale changes.

    private ControlResources() {
        // no-op
    }

    /*
     * Look up a string in the properties file corresponding to the
     * default locale (i.e. the application's locale). If not found, the
     * search then falls back to the base controls.properties file,
     * containing the default string (usually English).
     */
    public static String getString(String key) {
        return ResourceBundle.getBundle(BASE_NAME).getString(key);
    }

    /*
     * Look up a non-translatable string in the properties file
     * corresponding to the default locale (i.e. the application's
     * locale). If not found, the search then falls back to the base
     * controls-nt.properties file, containing the default string.
     *
     * Note that property values may be set in locale-specific files,
     * e.g. when a property value is defined for a country rather than
     * a language. However, there are no such files included with
     * JavaFX 8, but may be added to the classpath by developers or
     * users.
     */
    public static String getNonTranslatableString(String key) {
        return ResourceBundle.getBundle(NT_BASE_NAME).getString(key);
    }
}
