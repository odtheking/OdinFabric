package com.odtheking.odin.utils

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.core.onReceive
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket
import net.minecraft.util.Util
import kotlin.math.min

object ServerUtils {
    private var prevTime = 0L
    var averageTps = 20f
        private set

    var currentPing: Int = 0
        private set

    var averagePing: Int = 0
        private set

    init {
        onReceive<WorldTimeUpdateS2CPacket> {
            if (prevTime != 0L) {
                averageTps = (20000f / (System.currentTimeMillis() - prevTime + 1)).coerceIn(0f, 20f)
            }

            prevTime = System.currentTimeMillis()
        }

        onReceive<PingResultS2CPacket> {
            currentPing = (Util.getMeasuringTimeMs() - startTime).toInt().coerceAtLeast(0)

            val pingLog = mc.debugHud.pingLog

            val sampleSize = min(pingLog.length, 5)

            if (sampleSize == 0) {
                averagePing = currentPing
                return@onReceive
            }

            var total = 0L
            for (i in 0 until sampleSize) {
                total += pingLog.get(i)
            }

            averagePing = (total / sampleSize).toInt()
        }
    }
}