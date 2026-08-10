package com.example.uos_lms.feature.teacher.presentation.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.common.millisToDisplay
import com.example.uos_lms.core.domain.model.QuizType
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.LoadingButton
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun CreateQuizScreen(
    onBack: () -> Unit,
    viewModel: CreateQuizViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.created.collect { onBack() }
    }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Create Quiz / Exam",
                onBack = onBack,
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.TEACHER).toList()),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                QuizType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = uiState.type == type,
                        onClick = { viewModel.onTypeChange(type) },
                        shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = QuizType.entries.size,
                        ),
                    ) {
                        Text(type.name)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            AppTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = "Title",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "Description",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppTextField(
                value = uiState.timeLimitText,
                onValueChange = viewModel::onTimeLimitChange,
                label = "Time Limit (minutes)",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(uiState.dueDateMillis?.let { "Due: ${millisToDisplay(it)}" } ?: "Pick Due Date")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Questions", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            uiState.questions.forEachIndexed { index, question ->
                QuestionEditorCard(
                    index = index,
                    question = question,
                    canRemove = uiState.questions.size > 1,
                    onTextChange = { viewModel.onQuestionTextChange(index, it) },
                    onOptionChange = { optionIndex, value -> viewModel.onOptionChange(index, optionIndex, value) },
                    onCorrectOptionChange = { optionIndex -> viewModel.onCorrectOptionChange(index, optionIndex) },
                    onMarksChange = { viewModel.onMarksChange(index, it) },
                    onRemove = { viewModel.removeQuestion(index) },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedButton(onClick = viewModel::addQuestion, modifier = Modifier.fillMaxWidth()) {
                Text("Add Question")
            }

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(24.dp))
            LoadingButton(
                text = "Save",
                isLoading = uiState.isSaving,
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.dueDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDueDateChange(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun QuestionEditorCard(
    index: Int,
    question: QuestionDraft,
    canRemove: Boolean,
    onTextChange: (String) -> Unit,
    onOptionChange: (Int, String) -> Unit,
    onCorrectOptionChange: (Int) -> Unit,
    onMarksChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Question ${index + 1}", style = MaterialTheme.typography.titleSmall)
                if (canRemove) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove question")
                    }
                }
            }
            AppTextField(
                value = question.text,
                onValueChange = onTextChange,
                label = "Question text",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            question.options.forEachIndexed { optionIndex, option ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = question.correctOptionIndex == optionIndex,
                        onClick = { onCorrectOptionChange(optionIndex) },
                    )
                    AppTextField(
                        value = option,
                        onValueChange = { onOptionChange(optionIndex, it) },
                        label = "Option ${optionIndex + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            AppTextField(
                value = question.marksText,
                onValueChange = onMarksChange,
                label = "Marks",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
}
