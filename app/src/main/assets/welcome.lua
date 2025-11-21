local LinearLayout = luajava.bindClass("android.widget.LinearLayout")
local TextView = luajava.bindClass("android.widget.TextView")
local Button = luajava.bindClass("android.widget.Button")

local layout = LinearLayout(activity)
layout.orientation = LinearLayout.VERTICAL
layout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

local btn = Button(activity)
btn.text = "欢迎使用"
btn.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

layout.setFitsSystemWindows(true)
layout.addView(btn)

activity.setContentView(layout)