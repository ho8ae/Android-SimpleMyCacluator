package com.mop.a2023.p20205151.JINJJAREAL.real

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

class BatteryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            // 배터리 잔량 백분율 계산
            val batteryPct: Int = (level.toFloat() / scale.toFloat() * 100).toInt()

            // 이제 이 배터리 잔량 정보를 활용할 수 있습니다.
            // 예를 들어, 토스트 메시지로 출력하거나 다른 처리를 수행할 수 있습니다.
        }
    }
}
