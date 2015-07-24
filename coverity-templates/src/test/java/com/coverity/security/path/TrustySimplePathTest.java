package com.coverity.security.path;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TrustySimplePathTest {

    @Test
    public void testBasicUsage() {
        TrustySimplePath path = new TrustySimplePath("a/b/c");
        assertEquals(path.sub("d").getPath(), localizePath("a/b/c/d"));
        assertEquals(path.sub("d", "e").getPath(), localizePath("a/b/c/d/e"));
        assertEquals(path.path("d/e").getPath(), localizePath("a/b/c/d/e"));

        boolean exception = false;
        try {
            path.sub("..");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            path.sub("a/b");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            path.sub("a/../../b");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }


    private static String localizePath(String s) {
        return new File(s).getPath();
    }
}
