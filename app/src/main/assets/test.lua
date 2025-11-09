print("Java Class")
local AlertDialog = luajava.bindClass "android.app.AlertDialog"
print("AlertDialog: " .. AlertDialog)
local AlertDialog2 = luajava.bindClass "android.app.AlertDialog"
print("AlertDialog is AlertDialog2: " .. tostring(AlertDialog == AlertDialog2))

print("AlertDialog.BUTTON_NEUTRAL(-3):", tostring(AlertDialog.BUTTON_NEUTRAL))
print("AlertDialog.Builder:", AlertDialog.Builder)
