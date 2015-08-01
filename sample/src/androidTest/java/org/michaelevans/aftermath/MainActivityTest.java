package org.michaelevans.aftermath;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.michaelevans.aftermath.sample.MainActivity;
import org.michaelevans.aftermath.sample.R;

import static android.app.Instrumentation.ActivityResult;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
public final class MainActivityTest {

    private static final String FAKE_VALUE = "123456789";
    private final Intent successIntent = new Intent();

    @Rule
    public IntentsTestRule<MainActivity> mActivityRule = new IntentsTestRule<>(MainActivity.class);

    @Before
    public void setUp() throws Exception {
        successIntent.putExtra("key", FAKE_VALUE);
    }

    @Test
    public void FirstActivityResultHandler() {
        intending(hasAction(equalTo(Intent.ACTION_PICK)))
                .respondWith(new ActivityResult(Activity.RESULT_OK, successIntent));

        onView(withText(R.string.get_contact_uri)).perform(click());

        onView(withId(R.id.contact_uri)).check(matches(withText(FAKE_VALUE)));
        onView(withId(R.id.photo_uri)).check(matches(withText(R.string.pick_a_photo)));
    }

    @Test
    public void FirstActivityResultHandler_Failure() {
        intending(hasAction(equalTo(Intent.ACTION_PICK)))
                .respondWith(new ActivityResult(Activity.RESULT_CANCELED, null));

        onView(withText(R.string.get_contact_uri)).perform(click());

        onView(withId(R.id.contact_uri)).check(matches(withText(R.string.an_error_has_occurred)));
        onView(withId(R.id.photo_uri)).check(matches(withText(R.string.pick_a_photo)));
    }

    @Test
    public void SecondActivityResultHandler() {
        intending(hasAction(equalTo(Intent.ACTION_GET_CONTENT)))
                .respondWith(new ActivityResult(Activity.RESULT_OK, successIntent));

        onView(withText(R.string.get_photo_uri)).perform(click());

        onView(withId(R.id.contact_uri)).check(matches(withText(R.string.pick_a_contact)));
        onView(withId(R.id.photo_uri)).check(matches(withText(FAKE_VALUE)));
    }

    @Test
    public void SecondActivityResultHandler_Failure() {
        intending(hasAction(equalTo(Intent.ACTION_GET_CONTENT)))
                .respondWith(new ActivityResult(Activity.RESULT_CANCELED, null));

        onView(withText(R.string.get_photo_uri)).perform(click());

        onView(withId(R.id.contact_uri)).check(matches(withText(R.string.pick_a_contact)));
        onView(withId(R.id.photo_uri)).check(matches(withText(R.string.an_error_has_occurred)));
    }
}
