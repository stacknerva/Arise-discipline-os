import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()

target = """@Composable
fun AddRoutineDialog(template: RoutineTemplateEntity?, onDismiss: () -> Unit, onSave: (String, String, String, Int, Int) -> Unit) {
    var title by remember { mutableStateOf(template?.title ?: "") }
    var start by remember { mutableStateOf(template?.startTimeStr ?: "") }
    var end by remember { mutableStateOf(template?.endTimeStr ?: "") }
    var offsetStr by remember { mutableStateOf(template?.notificationOffsetMins?.toString() ?: "5") }
    var orderStr by remember { mutableStateOf(template?.orderIndex?.toString() ?: "0") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (template == null) "ADD ROUTINE" else "EDIT ROUTINE") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start Time (HH:mm)") })
                OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End Time (HH:mm)") })"""
                
replacement = """import com.example.utils.parseTo24Hour

@Composable
fun AddRoutineDialog(template: RoutineTemplateEntity?, onDismiss: () -> Unit, onSave: (String, String, String, Int, Int) -> Unit) {
    var title by remember { mutableStateOf(template?.title ?: "") }
    var start by remember { mutableStateOf(if (template != null) formatTimeAmPm(template.startTimeStr) else "") }
    var end by remember { mutableStateOf(if (template != null) formatTimeAmPm(template.endTimeStr) else "") }
    var offsetStr by remember { mutableStateOf(template?.notificationOffsetMins?.toString() ?: "5") }
    var orderStr by remember { mutableStateOf(template?.orderIndex?.toString() ?: "0") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (template == null) "ADD ROUTINE" else "EDIT ROUTINE") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start Time (e.g. 2:00 PM)") })
                OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End Time (e.g. 4:30 PM)") })"""

if target in content:
    content = content.replace(target, replacement)
    
    # Need to change the save logic too
    save_target = """        confirmButton = { TextButton(onClick = { 
            val offset = offsetStr.toIntOrNull() ?: 5
            val order = orderStr.toIntOrNull() ?: 0
            onSave(title, start, end, offset, order) 
        }) { Text("SAVE") } },"""
    save_replace = """        confirmButton = { TextButton(onClick = { 
            val offset = offsetStr.toIntOrNull() ?: 5
            val order = orderStr.toIntOrNull() ?: 0
            onSave(title, parseTo24Hour(start), parseTo24Hour(end), offset, order) 
        }) { Text("SAVE") } },"""
    
    content = content.replace(save_target, save_replace)
    
    with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Not found")
