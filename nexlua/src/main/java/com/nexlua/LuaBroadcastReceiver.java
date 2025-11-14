package com.nexlua;

import android.content.*;

public class LuaBroadcastReceiver extends BroadcastReceiver
{

	private final OnReceiveListener onReceiveListener;
	
	public LuaBroadcastReceiver(OnReceiveListener onReceiveListener)
	{
		this.onReceiveListener =onReceiveListener;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		onReceiveListener.onReceive(context,intent);
	}
	
	public interface OnReceiveListener
	{
		void onReceive(android.content.Context context, android.content.Intent intent);
	}
}
