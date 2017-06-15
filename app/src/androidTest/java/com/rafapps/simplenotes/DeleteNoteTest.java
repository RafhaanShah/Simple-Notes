package com.rafapps.simplenotes;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.filters.LargeTest;

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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DeleteNoteTest {

    @Rule
    public ActivityTestRule<NotesList> mActivityTestRule = new ActivityTestRule<>(NotesList.class);

    @Test
    public void deleteNoteTest() {
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.fab), isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction appCompatEditText = onView(
                withId(R.id.editText));
        appCompatEditText.perform(scrollTo(), replaceText("A note."), closeSoftKeyboard());

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.recyclerView),
                        withParent(allOf(withId(R.id.constraintLayout),
                                withParent(withId(R.id.coordinatorLayout)))),
                        isDisplayed()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction actionMenuItemView2 = onView(
                allOf(withId(R.id.deleteButton), withContentDescription("Delete"), isDisplayed()));
        actionMenuItemView2.perform(click());

        ViewInteraction appCompatButton = onView(
                allOf(withId(android.R.id.button1), withText("Yes")));
        appCompatButton.perform(scrollTo(), click());

        onView(withId(R.id.recyclerView)).check(matches(not(hasDescendant(withText("Note")))));

    }

    @Before
    public void deleteAllFiles() {
        File[] files = mActivityTestRule.getActivity().getFilesDir().listFiles();
        for (File file : files) {
            file.delete();
        }
    }

}
