local String1 = luajava.bindClass "java.lang.String"
print("String1: " .. String1)
local String2 = luajava.bindClass "java.lang.String"
print("String1 is String2: " .. tostring(String == String2))

local String = luajava.bindClass "java.lang.String"
print("String.valueOf: " .. String.valueOf(123))

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
local button = Button(activity)
local editor = EditText(activity)

button.setText("Run")
button.setOnClickListener(function()
    activity.newActivity("main2.lua", {editor.getText().toString()})
end)

editor.setText("print('hello world')")

layout.setOrientation(LinearLayout.VERTICAL)
layout.setFitsSystemWindows(true)
layout.addView(button)
layout.addView(editor)

activity.setContentView(layout)