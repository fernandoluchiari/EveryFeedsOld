package br.com.everyfeeds.receiver;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScheduleReceiver extends BroadcastReceiver {
	// restart service every 30 seconds
	private static final long REPEAT_TIME = 1000 * 60;// * 10;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("ScheduleEveryFeeds", "Iniciando broadcastReceiver");
		AlarmManager service = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, ServiceReceiver.class);
		final PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MILLISECOND, 1);
		service.setInexactRepeating(AlarmManager.RTC, cal.getTimeInMillis(),
				REPEAT_TIME, pending);
	}
	
	
}
