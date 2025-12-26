package com.odtheking.odin.features.impl.nether

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.skyblock.Supply
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.math.*

object PearlWaypoints : Module(
    name = "Pearl Waypoints",
    description = "Renders waypoints for pearls in Kuudra."
) {
    private val dynamicWaypoints by BooleanSetting("Dynamic Waypoints", false, desc = "Renders waypoints dynamically based on your position.")
    private val dynamicWaypointsColor by ColorSetting("Dynamic Color", Colors.MINECRAFT_DARK_PURPLE, true, desc = "Color of the dynamic waypoints.").withDependency { dynamicWaypoints }
    private val presetWaypoints by BooleanSetting("Preset Waypoints", true, desc = "Renders preset waypoints for pearls.")
    private val hideFarWaypoints by BooleanSetting("Hide Far Waypoints", true, desc = "Hides the waypoints that are not the closest to you.").withDependency { presetWaypoints }

    init {
        on<RenderEvent.Last> {
            if (!KuudraUtils.inKuudra || KuudraUtils.phase != 1) return@on

            var closest = true
            getOrderedLineups(mc.player?.blockPosition() ?: return@on).forEach { (lineup, color) ->
                lineup.startPos.forEach {
                    if (presetWaypoints) context.drawWireFrameBox(AABB(it), color)
                }

                lineup.lineups.forEach lineupLoop@{ blockPos ->
                    if ((NoPre.missing.equalsOneOf(Supply.None, Supply.Square) ||
                                (lineup.supply != Supply.Square || enumToLineup[NoPre.missing] == blockPos)) && (!hideFarWaypoints || closest)) {
                        if (presetWaypoints) context.drawFilledBox(AABB(blockPos), color)
                        if (dynamicWaypoints) {
                            val destinationSupply = if (lineup.supply == Supply.Square) NoPre.missing else lineup.supply
                            calculatePearl(destinationSupply.dropOffSpot)?.let {
                                context.drawFilledBox(AABB.ofSize(it, 0.12, 0.12, 0.12), dynamicWaypointsColor)
                            }
                            context.drawWireFrameBox(AABB(BlockPos(lineup.supply.dropOffSpot.above())), dynamicWaypointsColor)
                        }
                    }
                }
                closest = false
            }
        }
    }

    private val enumToLineup = hashMapOf(
        Supply.xCannon to BlockPos(-59, 106, -59),
        Supply.X to BlockPos(-58, 127, -148),
        Supply.Shop to BlockPos(-146, 107, -60),
        Supply.Triangle to BlockPos(-149, 104, -70),
        Supply.Equals to BlockPos(-168, 124, -118),
        Supply.Slash to BlockPos(-65, 109, -162)
    )

    private fun getOrderedLineups(pos: BlockPos): SortedMap<Lineup, Color> {
        return pearlLineups.toSortedMap(
            compareBy { key ->
                key.startPos.minOfOrNull { it.distSqr(pos) } ?: Double.MAX_VALUE
            }
        )
    }

    private const val DEG_TO_RAD = PI / 180
    private const val RAD_TO_DEG = 180 / PI
    private const val E_VEL = 1.67
    private const val E_VEL_SQ = E_VEL * E_VEL
    private const val GRAV = 0.05

    // Made by Aidanmao
    private fun calculatePearl(targetPos: BlockPos): Vec3? {
        val (posX, posY, posZ) = mc.player?.renderPos ?: return null

        val offX = targetPos.x - posX
        val offZ = targetPos.z - posZ
        val offHor = hypot(offX, offZ)

        val discrim = E_VEL_SQ - GRAV * (((GRAV * offHor * offHor) / (2 * E_VEL_SQ)) - (targetPos.y - posY + 1.62))
        if (discrim < 0) return null

        val sqrtDiscrim = sqrt(discrim)
        val atanFactor = GRAV * offHor
        val angle1 = (atan((E_VEL_SQ + sqrtDiscrim) / atanFactor)) * RAD_TO_DEG
        val angle2 = (atan((E_VEL_SQ - sqrtDiscrim) / atanFactor)) * RAD_TO_DEG

        val angle = when {
            angle1 >= 45.0 -> angle1
            angle2 >= 45.0 -> angle2
            else -> return null
        }

        val dragAng = when {
            offHor < 10 -> 1.0
            offHor in 28.0..<40.0 -> 1.033 + ((offHor - 28) / 12.0) * (-0.033)
            offHor < 28 -> 1.026 + ((offHor - 10) / 18.0) * (-0.017)
            offHor in 36.0..45.0 -> 0.982
            else -> 1.0 + ((offHor - 40) / 15.0) * (-0.12)
        }

        val radP = -(angle * dragAng) * DEG_TO_RAD
        val radY = -atan2(offX, offZ)
        val cosRadP = cos(radP)

        return Vec3(posX - (cosRadP * sin(radY)) * 10, posY + (-sin(radP)) * 10, posZ + (cosRadP * cos(radY)) * 10)
    }

    private val pearlLineups: Map<Lineup, Color> = mapOf(
        // Shop
        Lineup(
            supply = Supply.Shop,
            startPos = setOf(BlockPos(-71, 79, -135), BlockPos(-86, 78, -129)),
            lineups = setOf(BlockPos(-146, 107, -60), BlockPos(-147, 111, -69))
        ) to Colors.MINECRAFT_RED,
        // Triangle
        Lineup(
            supply = Supply.Triangle,
            startPos = setOf(BlockPos(-68, 77, -123)),
            lineups = setOf(BlockPos(-149, 104, -70))
        ) to Colors.MINECRAFT_LIGHT_PURPLE,
        // X
        Lineup(
            supply = Supply.X,
            startPos = setOf(BlockPos(-135, 77, -139)),
            lineups = setOf(BlockPos(-59, 115, -71))
        ) to Colors.MINECRAFT_YELLOW,
        Lineup(
            supply = Supply.xCannon,
            startPos = setOf(BlockPos(-131, 79, -114)),
            lineups = setOf(BlockPos(-59, 106, -59), BlockPos(-51, 108, -67), BlockPos(-39, 93, -76))
        ) to Colors.WHITE,
        // Square
        Lineup(
            supply = Supply.Square,
            startPos = setOf(BlockPos(-141, 78, -91)),
            lineups = setOf(
                BlockPos(-59, 106, -59), // cannon
                BlockPos(-58, 127, -148), // X
                BlockPos(-146, 107, -60), // shop
                BlockPos(-149, 104, -70), // triangle
                BlockPos(-168, 124, -118), // equals
                BlockPos(-65, 109, -162) // slash
            )
        ) to Colors.MINECRAFT_AQUA,
        // equals
        Lineup(
            supply = Supply.Equals,
            startPos = setOf(BlockPos(-66, 76, -88)),
            lineups = setOf(BlockPos(-168, 124, -118))
        ) to Colors.MINECRAFT_GREEN,
        // slash
        Lineup(
            supply = Supply.Slash,
            startPos = setOf(BlockPos(-115, 77, -69)),
            lineups = setOf(BlockPos(-65, 109, -162))
        ) to Colors.MINECRAFT_BLUE
    )

    private data class Lineup(val supply: Supply, val startPos: Set<BlockPos>, val lineups: Set<BlockPos>)
}