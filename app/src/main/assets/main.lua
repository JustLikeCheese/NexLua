local LinearLayout = luajava.bindClass("android.widget.LinearLayout")
local Button = luajava.bindClass("android.widget.Button")
local EditText = luajava.bindClass("android.widget.EditText")
local Toast = luajava.bindClass("android.widget.Toast")
local AlertDialog = luajava.bindClass("android.app.AlertDialog")
local Object = luajava.bindClass("java.lang.Object")

local function toast(text)
    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
end

local function alert(text)
    AlertDialog.Builder(activity)
        .setTitle("提示")
        .setMessage(text)
        .setPositiveButton("OK", nil)
        .show()
end

local layout = LinearLayout(activity)
local toolbar = LinearLayout(activity)
local button1 = Button(activity)
local button2 = Button(activity)
local editor = EditText(activity)

button1.setText("Run")
button1.setOnClickListener(function()
    activity.newActivity("main2.lua", {editor.getText().toString()})
end)

button2.setText("Test")
button2.setOnClickListener(function()
    activity.newActivity("test.lua")
end)

editor.setText("print('hello world')")

toolbar.setOrientation(LinearLayout.HORIZONTAL)
toolbar.addView(button1)
toolbar.addView(button2)

layout.setOrientation(LinearLayout.VERTICAL)
layout.setFitsSystemWindows(true)
layout.addView(toolbar)
layout.addView(editor)

activity.setContentView(layout)

function onError(e)
    alert(e)
end