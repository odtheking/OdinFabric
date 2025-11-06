package com.odtheking.odin.features.impl.nether

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.skyblock.Supply
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.math.*

object PearlWaypoints : Module(
    name = "Pearl Waypoints",
    description = "Renders waypoints for pearls in Kuudra."
) {
    private val dynamicWaypoints by BooleanSetting("Dynamic Waypoints", true, desc = "Renders waypoints dynamically based on your position.")
    private val dynamicWaypointsColor by ColorSetting("Dynamic Color", Colors.MINECRAFT_DARK_PURPLE.withAlpha(0.5f), true, desc = "Color of the dynamic waypoints.").withDependency { dynamicWaypoints }
    private val presetWaypoints by BooleanSetting("Preset Waypoints", true, desc = "Renders preset waypoints for pearls.")
    private val hideFarWaypoints by BooleanSetting("Hide Far Waypoints", true, desc = "Hides the waypoints that are not the closest to you.").withDependency { presetWaypoints }

    init {
        on<RenderEvent.Last> {
            if (!KuudraUtils.inKuudra || KuudraUtils.phase != 1) return@on

            var closest = true
            getOrderedLineups(mc.player?.blockPos ?: return@on).forEach { (lineup, color) ->
                lineup.startPos.forEach {
                    if (presetWaypoints) drawWireFrameBox(
                        Box(it),
                        color.withAlpha(if (!closest && hideFarWaypoints) 0.25f else 1f),
                        if (!closest && hideFarWaypoints) 4f else 6f
                    )
                }

                lineup.lineups.forEach lineupLoop@{ blockPos ->
                    if ((NoPre.missing.equalsOneOf(Supply.None, Supply.Square) ||
                                (lineup.supply != Supply.Square || enumToLineup[NoPre.missing] == blockPos)) && (!hideFarWaypoints || closest)) {
                        if (presetWaypoints) drawFilledBox(Box(blockPos), color)
                        if (dynamicWaypoints) {
                            val destinationSupply = if (lineup.supply == Supply.Square) NoPre.missing else lineup.supply
                            calculatePearl(destinationSupply.dropOffSpot)?.let {
                                drawWireFrameBox(Box.of(it, 0.12, 0.12, 0.12), dynamicWaypointsColor, 2f)
                            }
                            drawWireFrameBox(Box(BlockPos(lineup.supply.dropOffSpot.up())), dynamicWaypointsColor)
                        }
                    }
                }
                closest = false
            }
        }
    }

    private val enumToLineup = hashMapOf(
        Supply.xCannon to BlockPos(-110, 155, -106),
        Supply.X to BlockPos(-46, 120, -150),
        Supply.Shop to BlockPos(-46, 135, -139),
        Supply.Triangle to BlockPos(-37, 139, -125),
        Supply.Equals to BlockPos(-28, 128, -112),
        Supply.Slash to BlockPos(-106, 157, -99)
    )

    private fun getOrderedLineups(pos: BlockPos): SortedMap<Lineup, Color> {
        return pearlLineups.toSortedMap(
            compareBy { key ->
                key.startPos.minOfOrNull { it.getSquaredDistance(pos) } ?: Double.MAX_VALUE
            }
        )
    }

    private const val DEG_TO_RAD = PI / 180
    private const val RAD_TO_DEG = 180 / PI
    private const val E_VEL = 1.67
    private const val E_VEL_SQ = E_VEL * E_VEL
    private const val GRAV = 0.05

    // Made by Aidanmao
    private fun calculatePearl(targetPos: BlockPos): Vec3d? {
        val posX = mc.player?.renderX ?: return null
        val posY = mc.player?.renderY ?: return null
        val posZ = mc.player?.renderZ ?: return null

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

        return Vec3d(posX - (cosRadP * sin(radY)) * 10, posY + (-sin(radP)) * 10, posZ + (cosRadP * cos(radY)) * 10)
    }

    private val pearlLineups: Map<Lineup, Color> = mapOf(
        // Shop
        Lineup(
            supply = Supply.Shop,
            startPos = setOf(BlockPos(-71, 79, -135), BlockPos(-86, 78, -129)),
            lineups = setOf(BlockPos(-97, 157, -114))
        ) to Colors.MINECRAFT_RED,
        // Triangle
        Lineup(
            supply = Supply.Triangle,
            startPos = setOf(BlockPos(-68, 77, -123)),
            lineups = setOf(BlockPos(-96, 161, -105))
        ) to Colors.MINECRAFT_LIGHT_PURPLE,
        // X
        Lineup(
            supply = Supply.X,
            startPos = setOf(BlockPos(-135, 77, -139)),
            lineups = setOf(BlockPos(-102, 160, -110))
        ) to Colors.MINECRAFT_YELLOW,
        Lineup(
            supply = Supply.xCannon,
            startPos = setOf(BlockPos(-131, 79, -114)),
            lineups = setOf(BlockPos(-112, 155, -107))
        ) to Colors.WHITE,
        // Square
        Lineup(
            supply = Supply.Square,
            startPos = setOf(BlockPos(-141, 78, -91)),
            lineups = setOf(
                BlockPos(-110, 155, -106), // cannon
                BlockPos(-46, 120, -150), // X
                BlockPos(-46, 135, -139), // shop
                BlockPos(-37, 139, -125), // triangle
                BlockPos(-28, 128, -112), // equals
                BlockPos(-106, 157, -99) // slash
            )
        ) to Colors.MINECRAFT_AQUA,
        // equals
        Lineup(
            supply = Supply.Equals,
            startPos = setOf(BlockPos(-66, 76, -88)),
            lineups = setOf(BlockPos(-101, 160, -100))
        ) to Colors.MINECRAFT_GREEN,
        // slash
        Lineup(
            supply = Supply.Slash,
            startPos = setOf(BlockPos(-114, 77, -69)),
            lineups = setOf(BlockPos(-106, 157, -99), BlockPos(-138, 145, -88))
        ) to Colors.MINECRAFT_BLUE
    )

    private data class Lineup(val supply: Supply, val startPos: Set<BlockPos>, val lineups: Set<BlockPos>)
}