package com.rafapps.simplenotes;

import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SearchTest {

    @Rule
    public ActivityTestRule<NotesListActivity> mActivityTestRule = new ActivityTestRule<>(NotesListActivity.class);

    @Test
    public void searchTest() {
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.fab), isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction appCompatEditText = onView(
                withId(R.id.title));
        appCompatEditText.perform(scrollTo(), replaceText("Shopping"), closeSoftKeyboard());

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.search_button), withContentDescription("Search"),
                        withParent(allOf(withId(R.id.search_bar),
                                withParent(withId(R.id.searchButton)))),
                        isDisplayed()));
        appCompatImageView.perform(click());

        ViewInteraction searchAutoComplete = onView(
                allOf(withId(R.id.search_src_text),
                        withParent(allOf(withId(R.id.search_plate),
                                withParent(withId(R.id.search_edit_frame)))),
                        isDisplayed()));
        searchAutoComplete.perform(replaceText("shop"), closeSoftKeyboard());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.recyclerView),
                        withParent(allOf(withId(R.id.constraintLayout),
                                withParent(withId(R.id.coordinatorLayout)))),
                        isDisplayed()));

        onView(withId(R.id.recyclerView)).check(matches(hasDescendant(withText("Shopping"))));

        recyclerView.perform(actionOnItemAtPosition(0, click()));

        onView(withId(R.id.title)).check(matches(withText("Shopping")));
    }

    @Before
    public void deleteAllFiles() {
        File[] files = mActivityTestRule.getActivity().getFilesDir().listFiles();
        for (File file : files) {
            file.delete();
        }
    }

}
