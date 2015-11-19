package org.michaelevans.aftermath;

import android.content.Intent;

/**
 * DO NOT USE. Exposed for generated classes' use.
 */
public interface IAftermathDelegate<T> {
    @SuppressWarnings("unused")
    void onActivityResult(final T target, int requestCode, int resultCode, Intent data);

    @SuppressWarnings("unused")
    void onRequestPermissionsResult(final T target, int requestCode, String[] permissions, int[] grantResults);
}
