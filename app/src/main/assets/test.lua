require "import"
import "android.widget.*"
import "android.app.*"

print("\nJava Object")
local alert = AlertDialog.Builder(activity)
    .setTitle("Title")
    .setMessage("Message")
    .setPositiveButton("OK", function(dialog, which)
        print("Java Object Callback Tested:")
        print("dialog:", dialog)
        print("which:", which)
    end)
    .show()

print("Java Object Tested:", alert)

print("Java ArrayList")
local ArrayList = luajava.bindClass "java.util.ArrayList"
local list = ArrayList()
list.add("1")
list.add("2")
print("ArrayList:", list)
print("ArrayList.length:", #list)

print("\nJava Array")
local int = luajava.bindClass "int"
local array = int{1, 2, 3}
print("array:", array)
print("array.length", #array)
print()