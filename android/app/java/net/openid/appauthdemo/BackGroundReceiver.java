package net.openid.appauthdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.openid.appauth.internal.Logger;

public class BackGroundReceiver extends BroadcastReceiver {


  private static final String TAG = "BackGroundReceiver";

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(TAG, "Received the Brodcast ");
    Logger.debug(" Received the Brodcast ",
      new String[] { "ethier broswer is null", "or selecting wrong browser"});
    Logger.info("Received the Brodcast   " );

    context.unregisterReceiver(this);

  }
}
