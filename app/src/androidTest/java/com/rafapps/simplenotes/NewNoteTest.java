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
import static org.hamcrest.Matchers.containsString;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class NewNoteTest {

    @Rule
    public ActivityTestRule<NotesListActivity> mActivityTestRule = new ActivityTestRule<>(NotesListActivity.class);

    @Test
    public void newNoteTest() {
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.fab), isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction appCompatEditText = onView(
                withId(R.id.editText));
        appCompatEditText.perform(scrollTo(), replaceText("This is a new note."), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                withId(R.id.title));
        appCompatEditText2.perform(scrollTo(), replaceText("New Note"), closeSoftKeyboard());

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        onView(withId(R.id.recyclerView)).check(matches(hasDescendant(withText("New Note"))));

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.recyclerView),
                        withParent(allOf(withId(R.id.constraintLayout),
                                withParent(withId(R.id.coordinatorLayout)))),
                        isDisplayed()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));


        onView(withId(R.id.title)).check(matches(withText("New Note")));
        onView(withId(R.id.editText)).check(matches(withText(containsString("This is a new note."))));

    }

    @Before
    public void deleteAllFiles() {
        File[] files = mActivityTestRule.getActivity().getFilesDir().listFiles();
        for (File file : files) {
            file.delete();
        }
    }

}
