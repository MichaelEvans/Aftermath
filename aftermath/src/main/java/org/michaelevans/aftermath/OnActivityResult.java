package org.michaelevans.aftermath;

import android.app.Activity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface OnActivityResult {
    /**
     * @return The requestCode that this method should respond to.
     */
    int value();

    /**
     * @return The resultCode that this method should respond to. Defaults to RESULT_OK.
     */
    int resultCode() default Activity.RESULT_OK;
}
