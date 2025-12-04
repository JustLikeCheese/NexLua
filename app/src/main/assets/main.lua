require "import"
import "android.widget.*"
import "android.app.AlertDialog"
import "java.lang.Object"

local function toast(text)
    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
end

local function alert(title, text)
    local dialog = AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(text)
            .setPositiveButton("OK", null)
            .create()
    dialog.show()
    local androidId = luajava.bindClass("android.R$id")
    messageView = dialog.findViewById(androidId.message)
    if messageView ~= nil then
        messageView.setTextIsSelectable(true)
    end
end

local layout = LinearLayout(activity)
local toolbar = LinearLayout(activity)
local button1 = Button(activity)
local button2 = Button(activity)
local editor = EditText(activity)

button1.setText("Run")
button1.setOnClickListener(function()
    activity.newActivity("main2", {editor.getText().toString()})
end)

button2.setText("Test")
button2.setOnClickListener(function()
    activity.newActivity("test")
end)

editor.setText("print('hello world')")

toolbar.setOrientation(LinearLayout.HORIZONTAL)
toolbar.addView(button1)
toolbar.addView(button2)

layout.setOrientation(LinearLayout.VERTICAL)
layout.setFitsSystemWindows(true)
layout.addView(toolbar)
layout.addView(editor)

activity.setHomeAsUpEnabled(false)
activity.setContentView(layout)

function onError(exception, type, message)
    alert(type, message);
    return true;
end