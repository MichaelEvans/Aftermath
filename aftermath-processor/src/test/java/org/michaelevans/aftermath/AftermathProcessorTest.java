package org.michaelevans.aftermath;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Ignore;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public final class AftermathProcessorTest {

    @Ignore
    @Test
    public void testProcessor() {
        JavaFileObject sampleActivity = JavaFileObjects.forSourceString("com.example.MainActivity",
                "package com.example;"
                        + "import org.michaelevans.aftermath.OnActivityResult;"
                        + "import org.michaelevans.aftermath.OnRequestPermissionResult;"
                        + "import android.content.Intent;"
                        + "public class MainActivity {"
                        + "static final int PICK_CONTACT_REQUEST = 1;"
                        + "static final int GET_ACCOUNTS_PERMISSION_REQUEST = 1;"
                        + "@OnActivityResult(PICK_CONTACT_REQUEST) public void onContactPicked("
                        + "int resultCode, Intent data) {}"
                        + "@OnRequestPermissionResult(GET_ACCOUNTS_PERMISSION_REQUEST) public void onPermissionGot("
                        + "String[] permissions, int[] grantResults) {}"
                        + "}"
        );

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("MainActivity$$Aftermath",
                Joiner.on('\n').join(
                        "package com.example;",
                        "",
                        "import android.content.Intent;",
                        "import java.lang.Override;",
                        "import org.michaelevans.aftermath.Aftermath;",
                        "",
                        "public class MainActivity$$Aftermath<T extends com.example.MainActivity>"
                                + " implements Aftermath.IAftermathDelegate<T> {",
                        "    @Override",
                        "    public void onActivityResult(final T target, final int requestCode,"
                                + " final int resultCode, final Intent data) {",
                        "        if(requestCode == 1) {",
                        "            target.onContactPicked(resultCode, data);",
                        "        }",
                        "    }",
                        "",
                        "    @Override",
                        "    public void onRequestPermissionsResult(final T target, final int requestCode,"
                                + " final String[] permissions, final int[] grantResults) {",
                        "        if (requestCode == 1) {",
                        "            target.onPermissionGot(permissions, grantResults);",
                        "        }",
                        "    }",
                        "}"));

        assert_().about(javaSource())
                .that(sampleActivity)
                .processedWith(new AftermathProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }


}
