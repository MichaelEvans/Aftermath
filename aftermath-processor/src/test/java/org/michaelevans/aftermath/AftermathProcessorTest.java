package org.michaelevans.aftermath;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public final class AftermathProcessorTest {

    @Test
    public void testProcessor() {
        JavaFileObject sampleActivity = JavaFileObjects.forSourceString("com.example.MainActivity",
                "package com.example;"
                        + "import org.michaelevans.aftermath.OnActivityResult;"
                        + "import android.content.Intent;"
                        + "public class MainActivity {"
                        + "static final int PICK_CONTACT_REQUEST = 1;"
                        + "@OnActivityResult(PICK_CONTACT_REQUEST) public void onContactPicked("
                        + "int resultCode, Intent data) {}"
                        + "}"
        );

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("MainActivity$$Aftermath",
                Joiner.on('\n').join(
                        "package com.example;",
                        "",
                        "import android.content.Intent;",
                        "",
                        "public final class MainActivity$$Aftermath {",
                        "    public static void onActivityResult("
                                + "final com.example.MainActivity target, final int requestCode,"
                                + " final int resultCode, final Intent data) {",
                        "        if(requestCode == 1) {",
                        "            target.onContactPicked(resultCode, data);",
                        "        }",
                        "    }",
                        "}"));

        JavaFileObject expectedAftermath =
                JavaFileObjects.forSourceString("Aftermath",
                                                "package org.michaelevans.aftermath;\n"
                                                        + "\n"
                                                        + "import android.content.Intent;\n"
                                                        + "import com.example.MainActivity;\n"
                                                        + "import "
                                                        + "com.example.MainActivity$$Aftermath;\n"
                                                        + "import java.lang.Object;\n"
                                                        + "\n"
                                                        + "public final class Aftermath {\n"
                                                        + "  public static void onActivityResult("
                                                        + "final Object target, "
                                                        + "final int requestCode, "
                                                        + "final int resultCode, "
                                                        + "final Intent data) {\n"
                                                        + "    if(target instanceof MainActivity) {"
                                                        + "\n"
                                                        + "      MainActivity$$Aftermath"
                                                        + ".onActivityResult((MainActivity) target, "
                                                        + "requestCode, resultCode, data);\n"
                                                        + "    }\n"
                                                        + "  }\n"
                                                        +
                                                        "}");

        assert_().about(javaSource())
                .that(sampleActivity)
                .processedWith(new AftermathProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource, expectedAftermath);
    }
}
