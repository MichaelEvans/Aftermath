package org.michaelevans.aftermath;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface OnActivityResult {
    int value();
}
