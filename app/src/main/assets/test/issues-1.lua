local Button = luajava.bindClass 'android.widget.Button'
btn = Button(activity)
btn.setText('Click me')
btn.setOnClickListener(function()
    activity.showToast('Clicked')
end)
activity.setContentView(btn)