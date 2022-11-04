package com.rafapps.simplenotes

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.core.widget.CompoundButtonCompat
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginRunnerConditionEvent
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.runner.*
import java.io.FileNotFoundException

abstract class TaskerActionActivity<TInput : Any,
        TOutput : Any,
        TActionRunner : TaskerPluginRunner<TInput, TOutput>,
        THelper : TaskerPluginConfigHelper<TInput, TOutput, TActionRunner>> :
    Activity(), TaskerPluginConfig<TInput> {

    private val taskerHelper by lazy { getNewHelper(this) }
    protected val titleText: EditText? by lazy { dialog.findViewById(R.id.et_title) }
    protected val noteText: EditText? by lazy { dialog.findViewById(R.id.et_note) }
    protected val checkBox: CheckBox? by lazy { dialog.findViewById(R.id.checkbox_append) }
    protected val checkBoxLayout: LinearLayout? by lazy { dialog.findViewById(R.id.checkbox_layout) }
    private val checkBoxText: TextView? by lazy { dialog.findViewById(R.id.checkbox_text) }
    private val layout: LinearLayout? by lazy { dialog.findViewById(R.id.layout_tasker_input) }
    private val buttonDone: Button? by lazy { dialog.findViewById(R.id.button_done) }
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(this)
            .setView(R.layout.activity_tasker_input)
            .setOnCancelListener { finish() }
            .create()
    }

    abstract fun getNewHelper(config: TaskerPluginConfig<TInput>): THelper

    override val context: Context
        get() = applicationContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
        setTheme(context)
        buttonDone?.setOnClickListener {
            dialog.dismiss()
            taskerHelper.finishForTasker()
        }
        taskerHelper.onCreate()
    }

    private fun setTheme(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val colourPrimary = preferences.getInt(
            HelperUtils.PREFERENCE_COLOUR_PRIMARY,
            ContextCompat.getColor(context, R.color.colorPrimary)
        )

        val colourFont =
            preferences.getInt(HelperUtils.PREFERENCE_COLOUR_FONT, Color.BLACK)
        val colourBackground =
            preferences.getInt(HelperUtils.PREFERENCE_COLOUR_BACKGROUND, Color.WHITE)

        titleText?.let {
            it.setTextColor(colourFont)
            it.setHintTextColor(ColorUtils.setAlphaComponent(colourFont, 120))
            it.backgroundTintList = ColorStateList.valueOf(colourPrimary)
        }

        noteText?.let {
            it.setTextColor(colourFont)
            it.setHintTextColor(ColorUtils.setAlphaComponent(colourFont, 120))
            it.backgroundTintList = ColorStateList.valueOf(colourPrimary)
        }

        checkBoxText?.setTextColor(colourFont)
        layout?.setBackgroundColor(colourBackground)
        buttonDone?.setBackgroundColor(colourPrimary)
        checkBox?.let {
            CompoundButtonCompat.setButtonTintList(it, ColorStateList.valueOf(colourPrimary))
        }
    }
}

class TaskerActionGetNoteActivity :
    TaskerActionActivity<NoteInput, NoteOutput, GetNoteActionRunner, GetNoteActionHelper>(),
    TaskerPluginConfig<NoteInput> {

    override val inputForTasker: TaskerInput<NoteInput>
        get() = TaskerInput(regular = NoteInput(noteTitle = (titleText?.text ?: "").toString()))

    override fun assignFromInput(input: TaskerInput<NoteInput>) {
        titleText?.let {
            val title = input.regular.noteTitle
            it.setText(title)
            it.setSelection(title.length)
        }
    }

    override fun getNewHelper(config: TaskerPluginConfig<NoteInput>) =
        GetNoteActionHelper(config)
}

class TaskerActionSetNoteActivity :
    TaskerActionActivity<SetNoteInput, NoteOutput, SetNoteActionRunner, SetNoteActionHelper>(),
    TaskerPluginConfig<SetNoteInput> {

    override val inputForTasker: TaskerInput<SetNoteInput>
        get() = TaskerInput(
            regular = SetNoteInput(
                noteTitle = (titleText?.text ?: "").toString(),
                noteContent = (noteText?.text ?: "").toString(),
                append = checkBox?.isChecked ?: true
            )
        )

    override fun assignFromInput(input: TaskerInput<SetNoteInput>) {
        titleText?.let {
            val title = input.regular.noteTitle
            it.setText(title)
            it.setSelection(title.length)
        }

        noteText?.let {
            val note = input.regular.noteContent
            it.setText(note)
            it.setSelection(note.length)
        }

        checkBox?.isChecked = input.regular.append
    }

    override fun getNewHelper(config: TaskerPluginConfig<SetNoteInput>) =
        SetNoteActionHelper(config)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkBoxLayout?.isVisible = true
        noteText?.isVisible = true
    }
}

class TaskerEventNoteUpdateActivity :
    TaskerActionActivity<NoteInput, NoteOutput, NoteUpdateActionRunner, NoteUpdateEventHelper>(),
    TaskerPluginConfig<NoteInput> {

    override val inputForTasker: TaskerInput<NoteInput>
        get() = TaskerInput(regular = NoteInput(noteTitle = (titleText?.text ?: "").toString()))

    override fun assignFromInput(input: TaskerInput<NoteInput>) {
        titleText?.let {
            val title = input.regular.noteTitle
            it.setText(title)
            it.setSelection(title.length)
        }
    }

    override fun getNewHelper(config: TaskerPluginConfig<NoteInput>) =
        NoteUpdateEventHelper(config)
}

class GetNoteActionHelper(config: TaskerPluginConfig<NoteInput>) :
    TaskerPluginConfigHelper<NoteInput, NoteOutput, GetNoteActionRunner>(config) {
    override val inputClass = NoteInput::class.java
    override val outputClass = NoteOutput::class.java
    override val runnerClass = GetNoteActionRunner::class.java
}

class SetNoteActionHelper(config: TaskerPluginConfig<SetNoteInput>) :
    TaskerPluginConfigHelper<SetNoteInput, NoteOutput, SetNoteActionRunner>(config) {
    override val inputClass = SetNoteInput::class.java
    override val outputClass = NoteOutput::class.java
    override val runnerClass = SetNoteActionRunner::class.java
}

class NoteUpdateEventHelper(config: TaskerPluginConfig<NoteInput>) :
    TaskerPluginConfigHelper<NoteInput, NoteOutput, NoteUpdateActionRunner>(config) {
    override val inputClass = NoteInput::class.java
    override val outputClass = NoteOutput::class.java
    override val runnerClass = NoteUpdateActionRunner::class.java
}

class GetNoteActionRunner : TaskerPluginRunnerAction<NoteInput, NoteOutput>() {
    override val notificationProperties
        get() = NotificationProperties(iconResId = R.mipmap.ic_launcher_foreground)

    override fun run(
        context: Context,
        input: TaskerInput<NoteInput>
    ): TaskerPluginResult<NoteOutput> {
        val title = input.regular.noteTitle

        if (!HelperUtils.fileExists(context, title))
            throw FileNotFoundException(context.getString(R.string.note_not_found, title))

        val output = NoteOutput(
            title = title,
            content = HelperUtils.readFile(context, title)
        )
        return TaskerPluginResultSucess(output)
    }
}

class SetNoteActionRunner : TaskerPluginRunnerAction<SetNoteInput, NoteOutput>() {
    override val notificationProperties
        get() = NotificationProperties(iconResId = R.mipmap.ic_launcher_foreground)

    override fun run(
        context: Context,
        input: TaskerInput<SetNoteInput>
    ): TaskerPluginResult<NoteOutput> {
        val title = input.regular.noteTitle
        val newText = input.regular.noteContent
        val append = input.regular.append

        if (!HelperUtils.fileExists(context, title))
            throw FileNotFoundException(context.getString(R.string.note_not_found, title))

        val currentContent = HelperUtils.readFile(context, title)
        val newContent = if (append) currentContent.plus(newText) else newText

        HelperUtils.writeFile(context, title, newContent)

        val output = NoteOutput(
            title = title,
            content = newContent
        )
        return TaskerPluginResultSucess(output)
    }
}

class NoteUpdateActionRunner :
    TaskerPluginRunnerConditionEvent<NoteInput, NoteOutput, NoteOutput>() {
    override val notificationProperties
        get() = NotificationProperties(iconResId = R.mipmap.ic_launcher_foreground)

    override fun getSatisfiedCondition(
        context: Context,
        input: TaskerInput<NoteInput>,
        update: NoteOutput?
    ): TaskerPluginResultCondition<NoteOutput> {
        if (update == null) return TaskerPluginResultConditionUnsatisfied()
        if (input.regular.noteTitle != update.title) return TaskerPluginResultConditionUnsatisfied()
        return TaskerPluginResultConditionSatisfied(context, update)
    }
}

const val VARIABLE_NAME_NOTE_TITLE = "notetitle"
const val VARIABLE_NAME_NOTE_CONTENT = "notecontent"
const val VARIABLE_NAME_NOTE_APPEND = "noteappend"

@SuppressLint("NonConstantResourceId")
@TaskerInputRoot
class NoteInput @JvmOverloads constructor(
    @field:TaskerInputField(
        key = VARIABLE_NAME_NOTE_TITLE,
        labelResId = R.string.note_title,
        descriptionResId = R.string.note_title
    ) var noteTitle: String = ""
)

@SuppressLint("NonConstantResourceId")
@TaskerInputRoot
class SetNoteInput @JvmOverloads constructor(
    @field:TaskerInputField(
        key = VARIABLE_NAME_NOTE_TITLE,
        labelResId = R.string.note_title,
        descriptionResId = R.string.note_title
    ) var noteTitle: String = "",

    @field:TaskerInputField(
        key = VARIABLE_NAME_NOTE_CONTENT,
        labelResId = R.string.note_content,
        descriptionResId = R.string.note_content_description
    ) var noteContent: String = "",

    @field:TaskerInputField(
        key = VARIABLE_NAME_NOTE_APPEND,
        labelResId = R.string.append,
        descriptionResId = R.string.append_text
    ) var append: Boolean = true
)

@SuppressLint("NonConstantResourceId")
@TaskerInputRoot
@TaskerOutputObject
class NoteOutput(
    @field:TaskerInputField(
        key = VARIABLE_NAME_NOTE_TITLE,
        labelResId = R.string.note_title,
        descriptionResId = R.string.note_title
    )
    @get:TaskerOutputVariable(
        name = VARIABLE_NAME_NOTE_TITLE,
        labelResId = R.string.note_title,
        htmlLabelResId = R.string.note_title_description
    ) val title: String = "",

    @field:TaskerInputField(
        key = VARIABLE_NAME_NOTE_CONTENT,
        labelResId = R.string.note_content,
        descriptionResId = R.string.note_content_description
    )
    @get:TaskerOutputVariable(
        name = VARIABLE_NAME_NOTE_CONTENT,
        labelResId = R.string.note_content,
        htmlLabelResId = R.string.note_content_description
    ) val content: String = ""
)
