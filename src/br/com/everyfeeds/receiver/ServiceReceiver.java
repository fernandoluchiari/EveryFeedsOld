package br.com.everyfeeds.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import br.com.everyfeeds.service.MainService;

public class ServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		 Intent service = new Intent(context, MainService.class);
		    context.startService(service);
	}

}
