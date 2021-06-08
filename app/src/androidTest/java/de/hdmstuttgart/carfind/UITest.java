package de.hdmstuttgart.carfind;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.core.internal.deps.guava.base.Preconditions.checkNotNull;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;


/**
 * Instrumented test, which will execute on an Android device.
 * The test checks the functionality of the app by clearing the list, then creating some new cars and checking the list for these cars.
 */
@RunWith(AndroidJUnit4.class)
public class UITest {


    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule
            = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void useAppContext() {

        // Clear list
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.action_clear_list))
                .perform(click());


        // Create a new car
        onView(withId(R.id.fab))
                .perform(click());

        onView(withId(R.id.carModel))
                .perform(replaceText("Mini Cooper S"));

        onView(withId(R.id.licence_plate))
                .perform(replaceText("AB-C123"));

        onView(withId(R.id.level))
                .perform(replaceText("3"));

        onView(withId(R.id.spot))
                .perform(replaceText("22"));

        onView(withId(R.id.annotation))
                .perform(replaceText("This is a Car created by the UI test."), closeSoftKeyboard());

        onView(withId(R.id.save))
                .perform(click());


        // Create a second car
        onView(withId(R.id.fab))
                .perform(click());

        onView(withId(R.id.carModel))
                .perform(replaceText("VW ID.3"));

        onView(withId(R.id.licence_plate))
                .perform(replaceText("EF-G456"));

        onView(withId(R.id.level))
                .perform(replaceText("3"));

        onView(withId(R.id.spot))
                .perform(replaceText("23"));

        onView(withId(R.id.annotation))
                .perform(replaceText("This is a Car created by the UI test."), closeSoftKeyboard());

        onView(withId(R.id.save))
                .perform(click());


        // Check list for cars
        onView(withId(R.id.homeRecyclerView))
                .check(matches(atPosition(0, hasDescendant(withText("Mini Cooper S")))));

        onView(withId(R.id.homeRecyclerView))
                .check(matches(atPosition(1, hasDescendant(withText("VW ID.3")))));


        // Close application, reopen it and check again
        activityScenarioRule.getScenario().close();

        ActivityScenario.launch(MainActivity.class);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.homeRecyclerView))
                .check(matches(atPosition(0, hasDescendant(withText("Mini Cooper S")))));

        onView(withId(R.id.homeRecyclerView))
                .check(matches(atPosition(1, hasDescendant(withText("VW ID.3")))));


    }




    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
        checkNotNull(itemMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

}