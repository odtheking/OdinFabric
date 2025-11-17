package com.odtheking.odin.utils.network.hypixelapi

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.odtheking.odin.utils.capitalizeWords
import com.odtheking.odin.utils.magicalPower
import com.odtheking.odin.utils.startsWithOneOf
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.jvm.optionals.getOrNull
import kotlin.math.floor

//todo all the missing data. right now its about enough for what i currently need
object HypixelData {

    data class PlayerInfo(
        val profileData: ProfilesData,
        val uuid: String,
        val name: String,
    ) {
        fun profileOrSelected(profileName: String? = null): Profiles? =
            profileData.profiles.find { it.cuteName.equals(profileName, true) } ?: profileData.profiles.find { it.selected }

        inline val memberData get() = profileData.profiles.find { it.selected }?.members?.get(uuid)

        @Transient val profileList: List<Pair<String, String>> = profileData.profiles.map { it.cuteName to it.gameMode }

        companion object {
            val dummyPlayer = PlayerInfo(
                uuid = "BAH",
                name = "???",
                profileData = ProfilesData()
            )
        }
    }

    data class ProfilesData(
        val error: String? = null,
        val cause: String? = null,
        @SerializedName("profiles")
        private val profileList: List<Profiles>? = emptyList(), // for some reason this gets sent as null instead of empty sometimes. kinda weird.
    ) {
        val profiles get() = profileList.orEmpty()

        @Transient
        val failed: String? = when {
            error != null -> error
            cause != null -> cause
            else -> null
        }
    }

    data class Profiles(
        @SerializedName("profile_id")
        val profileId: String,
        @SerializedName("community_upgrades")
        val communityUpgrades: CommunityUpgrades = CommunityUpgrades(),
        val members: Map<String, MemberData>,
        @SerializedName("game_mode")
        val gameMode: String = "normal",
        val banking: BankingData = BankingData(),
        @SerializedName("cute_name")
        val cuteName: String,
        val selected: Boolean,
    )

    val mpRegex = Regex("§7§4☠ §cRequires §5.+§c.")

    data class MemberData(
        val rift: RiftData = RiftData(),
        @SerializedName("player_data")
        val playerData: PlayerData = PlayerData(),
        val events: EventsData = EventsData(),
        @SerializedName("garden_player_data")
        val gardenPlayerData: GardenPlayerData = GardenPlayerData(),
        @SerializedName("accessory_bag_storage")
        val accessoryBagStorage: AccessoryBagStorage = AccessoryBagStorage(),
        val leveling: LevelingData = LevelingData(),
        @SerializedName("item_data")
        val miscItemData: MiscItemData = MiscItemData(),
        @SerializedName("jacobs_contest")
        val jacobsContest: JacobsContestData = JacobsContestData(),
        val currencies: CurrencyData = CurrencyData(),
        val dungeons: DungeonsData = DungeonsData(),
        @SerializedName("glacite_player_data")
        val glacitePlayerData: GlacitePlayerData = GlacitePlayerData(),
        val profile: ProfileData = ProfileData(),
        @SerializedName("pets_data")
        val pets: PetsData = PetsData(),
        @SerializedName("player_id")
        val playerId: String,
        @SerializedName("nether_island_player_data")
        val crimsonIsle: CrimsonIsle = CrimsonIsle(),
        @SerializedName("player_stats")
        val playerStats: PlayerStats = PlayerStats(),
        val slayer: Slayers = Slayers(),
        val inventory: Inventory = Inventory(),
        val collection: Map<String, Long> = mapOf()
    ) {
        val magicalPower get() = inventory.bagContents["talisman_bag"]?.itemStacks?.mapNotNull {
            if (it == null || it.lore.any { item -> mpRegex.matches(item) }) return@mapNotNull null
            val mp = it.magicalPower + (if (it.id == "ABICASE") floor(crimsonIsle.abiphone.activeContacts.size / 2f).toInt() else 0)
            val itemId = it.id.takeUnless { it.startsWithOneOf("PARTY_HAT", "BALLOON_HAT") } ?: "PARTY_HAT"
            itemId to mp
        }?.groupBy { it.first }?.mapValues { entry ->
            entry.value.maxBy { it.second }
        }?.values?.fold(0) { acc, pair ->
            acc + pair.second
        }?.let { it + if (rift.access.consumedPrism) 11 else 0 } ?: 0

        @Transient val tunings = accessoryBagStorage.tuning.currentTunings.map { "${it.key.replace("_", " ").capitalizeWords()}§7: ${it.value}" }

        @Transient val inventoryApi = inventory.eChestContents.itemStacks.isNotEmpty()

        @Transient val allItems = (inventory.invContents.itemStacks + inventory.eChestContents.itemStacks + inventory.backpackContents.flatMap { it.value.itemStacks })

        @Transient val assumedMagicalPower = magicalPower.takeUnless { it == 0 } ?: (accessoryBagStorage.tuning.currentTunings.values.sum() * 10)
    }

    data class Slayers(
        @SerializedName("slayer_bosses")
        val bosses: Map<String, SlayerData> = emptyMap()
    )

    data class SlayerData(
        @SerializedName("claimed_levels")
        val claimed: Map<String, Boolean> = emptyMap(),
        val xp: Long = 0,
        //todo all this nonsense
    )

    data class PlayerStats(
        val kills: Map<String, Float> = emptyMap(),
        val deaths: Map<String, Float> = emptyMap(),
    ) {
        val bloodMobKills get() =
            ((kills["watcher_summon_undead"] ?: 0f) + (kills["master_watcher_summon_undead"] ?: 0f)).toInt()
    }

    data class CrimsonIsle(
        val abiphone: Abiphone = Abiphone(),
    )

    data class Abiphone(
        @SerializedName("active_contacts")
        val activeContacts: List<String> = emptyList(),
    )

    data class RiftData(
        @SerializedName("village_plaza")
        val villagePlaza: VillagePlaza = VillagePlaza(),
        @SerializedName("wither_cage")
        val witherCage: WitherCage = WitherCage(),
        @SerializedName("black_lagoon")
        val blackLagoon: BlackLagoon = BlackLagoon(),
        @SerializedName("dead_cats")
        val deadCats: DeadCatsData = DeadCatsData(),
        @SerializedName("wizard_tower")
        val wizardTower: WizardTower = WizardTower(),
        val enigma: EnigmaData = EnigmaData(),
        val gallery: GalleryData = GalleryData(),
        //todo the rest of this
        val access: RiftAccess = RiftAccess(),
    )

    data class RiftAccess(
        @SerializedName("last_free")
        val lastFree: Long = 0,
        @SerializedName("consumed_prism")
        val consumedPrism: Boolean = false
    )

    data class GalleryData(
        @SerializedName("elise_step")
        val eliseStep: Int = 0,
        @SerializedName("secured_trophies")
        val securedTrophies: List<SecuredTrophy> = emptyList(),
        @SerializedName("sent_trophy_dialogues")
        val sentDialogues: List<String> = emptyList(),
    )

    data class SecuredTrophy(
        val type: String,
        val timestamp: Long,
        val visits: Int,
    )

    data class EnigmaData(
        @SerializedName("bought_cloak")
        val boughtCloak: Boolean = false,
        @SerializedName("found_souls")
        val foundSouls: List<String> = emptyList(),
        @SerializedName("claimed_bonus_index")
        val claimedBonusIndex: Int = 0,
    )

    data class WizardTower(
        @SerializedName("wizard_quest_step")
        val wizardQuestStep: Int = 0,
        @SerializedName("crumbs_laid_out")
        val crumbsLaidOut: Int = 0,
    )

    data class DeadCatsData(
        @SerializedName("found_cats")
        val foundCats: List<String> = emptyList(),
        @SerializedName("talked_to_jacquelle")
        val talkedJacquelle: Boolean = false,
        @SerializedName("picked_up_detector")
        val pickedUpDetector: Boolean = false,
        @SerializedName("unlocked_pet")
        val unlocked: Boolean = false,
        val montezuma: Pet = Pet()
    )

    data class BlackLagoon(
        @SerializedName("talked_to_edwin")
        val talkedToEdwin: Boolean = false,
        @SerializedName("received_science_paper")
        val receivedSciencePaper: Boolean = false,
        @SerializedName("delivered_science_paper")
        val deliveredSciencePaper: Boolean = false,
        @SerializedName("completed_step")
        val completedStep: Int = 0,
    )

    data class WitherCage(
        @SerializedName("killed_eyes")
        val killedEyes: List<String> = emptyList(),
    )

    data class BarryCenter(
        @SerializedName("first_talked_to_barry")
        val talkedToBarry: Boolean = false,
        val convinced: List<String> = emptyList(),
        @SerializedName("received_reward")
        val receivedReward: Boolean = false,
    )

    data class VillagePlaza(
        val murder: VillageMurder = VillageMurder(),
        @SerializedName("barry_center")
        val barryCenter: BarryCenter = BarryCenter(),
        val cowboy: VillageCowboy = VillageCowboy(),
        @SerializedName("barter_bank")
        val barterBank: JsonElement? = null, //todo
        val lonely: VillageLonely = VillageLonely(),
        val seraphine: VillageSeraphine = VillageSeraphine(),
        @SerializedName("got_scammed")
        val scammed: Boolean = false,
    )

    data class VillageSeraphine(
        @SerializedName("step_index")
        val stepIndex: Int = 0,
    )

    data class VillageLonely(
        @SerializedName("seconds_sitting")
        val secondsSitting: Int = 0,
    )

    data class VillageCowboy(
        val stage: Int = 0,
        @SerializedName("hay_eaten")
        val hayEaten: Long = 0,
        @SerializedName("rabbit_name")
        val rabbitName: String? = null
    )

    data class VillageMurder(
        @SerializedName("step_index")
        val stepIndex: Int = 0,
        @SerializedName("room_clues")
        val roomClues: List<String> = emptyList(),
    )

    data class PetsData(
        //todo other useless things here
        val pets: List<Pet> = emptyList()
    ) {
        @Transient
        val activePet = pets.find { it.active }
    }

    data class Pet(
        val uuid: String? = null,
        val uniqueId: String? = null,
        val type: String = "",
        val exp: Double = 0.0,
        val active: Boolean = false,
        val tier: String = "",
        val heldItem: String? = null,
        val candyUsed: Int = 0,
        val skin: String? = null,
    )

    data class ProfileData(
        @SerializedName("first_join")
        val firstJoin: Long = 0,
        @SerializedName("personal_bank_upgrade")
        val personalBankUpgrade: Int = 0,
        @SerializedName("bank_account")
        val bankAccount: Double = 0.0,
        @SerializedName("cookie_buff_active")
        val activeCookie: Boolean = false,
    )

    data class GlacitePlayerData(
        @SerializedName("corpses_looted")
        val corpsesLooted: Map<String, Int> = emptyMap(),
        @SerializedName("mineshafts_entered")
        val mineshaftsEntered: Int = 0,
    )

    data class DungeonsData(
        @SerializedName("dungeon_types")
        val dungeonTypes: DungeonTypes = DungeonTypes(),
        @SerializedName("player_classes")
        val classes: Map<String, ClassData> = emptyMap(),
        @SerializedName("dungeon_journal")
        val dungeonJournal: DungeonJournal = DungeonJournal(),
        @SerializedName("dungeons_blah_blah")
        val dungeonYapping: List<String> = emptyList(),
        @SerializedName("selected_dungeon_class")
        val selectedClass: String? = null,
        @SerializedName("daily_runs")
        val dailyRuns: DailyRunData = DailyRunData(),
        //todo val treasures: TreasureData,
        @SerializedName("dungeon_hub_race_settings")
        val dungeonRaceSettings: DungeonRaceSettings = DungeonRaceSettings(),
        @SerializedName("last_dungeon_run")
        val lastDungeonRun: String? = null,
        val secrets: Long = 0,
    )

    data class DungeonTypes(
        val catacombs: DungeonTypeData = DungeonTypeData(),
        @SerializedName("master_catacombs")
        val mastermode: DungeonTypeData = DungeonTypeData(),
    )

    data class DungeonRaceSettings(
        @SerializedName("selected_race")
        val selected: String? = null,
        @SerializedName("selected_setting")
        val setting: String? = null,
        val runback: Boolean = false
    )

    data class DailyRunData(
        @SerializedName("current_day_stamp")
        val currentDayStamp: Long? = null,
        @SerializedName("completed_runs_count")
        val completedRunsCount: Long = 0,
    )

    data class DungeonJournal(
        @SerializedName("unlocked_journals")
        val unlockedJournals: List<String> = emptyList()
    )

    data class ClassData(
        val experience: Double = 0.0
    )

    data class DungeonTypeData(
        @SerializedName("times_played")
        val timesPlayed: Map<String, Double>? = null,
        val experience: Double = 0.0,
        @SerializedName("tier_completions")
        val tierComps: Map<String, Float> = emptyMap(),
        @SerializedName("milestone_completions")
        val milestoneComps: Map<String, Float> = emptyMap(),
        @SerializedName("fastest_time")
        val fastestTimes: Map<String, Float> = emptyMap(),
        @SerializedName("best_runs")
        val bestRuns: Map<String, List<BestRun>> = emptyMap(),
        @SerializedName("best_score")
        val bestScore: Map<String, Float> = emptyMap(),
        @SerializedName("mobs_killed")
        val mobsKilled: Map<String, Float> = emptyMap(),
        @SerializedName("most_mobs_killed")
        val mostMobsKilled: Map<String, Float> = emptyMap(),
        @SerializedName("most_damage_berserk")
        val mostDamageBers: Map<String, Double> = emptyMap(),
        @SerializedName("most_healing")
        val mostHealing: Map<String, Double> = emptyMap(),
        @SerializedName("watcher_kills")
        val watcherKills: Map<String, Float> = emptyMap(),
        @SerializedName("highest_tier_completed")
        val highestTierComp: Int = 0,
        @SerializedName("most_damage_tank")
        val mostDamageTank: Map<String, Double> = emptyMap(),
        @SerializedName("most_damage_healer")
        val mostDamageHealer: Map<String, Double> = emptyMap(),
        @SerializedName("fastest_time_s")
        val fastestTimeS: Map<String, Double> = emptyMap(),
        @SerializedName("most_damage_mage")
        val mostDamageMage: Map<String, Double> = emptyMap(),
        @SerializedName("fastest_time_s_plus")
        val fastestTimeSPlus: Map<String, Double> = emptyMap(),
        @SerializedName("most_damage_Archer")
        val mostDamageArcher: Map<String, Double> = emptyMap(),
    ) {
//        @Transient val total =
    }

    data class BestRun(
        val timestamp: Long? = null,
        @SerializedName("score_exploration")
        val explorationScore: Int = 0,
        @SerializedName("score_speed")
        val speedScore: Int = 0,
        @SerializedName("score_skill")
        val skillScore: Int = 0,
        @SerializedName("score_bonus")
        val bonusScore: Int = 0,
        @SerializedName("dungeon_class")
        val dungeonClass: String? = null,
        val teammates: List<String> = emptyList(),
        @SerializedName("elapsed_time")
        val elapsedTime: Long = 0,
        @SerializedName("damage_dealt")
        val damageDealt: Double = 0.0,
        val deaths: Int = 0,
        @SerializedName("mobs_killed")
        val mobsKilled: Int = 0,
        @SerializedName("secrets_found")
        val secretsFound: Int = 0,
        @SerializedName("damage_mitigated")
        val damageMitigated: Double = 0.0,
        @SerializedName("ally_healing")
        val allyHealing: Double = 0.0,
    )

    data class CurrencyData(
        @SerializedName("coin_purse")
        val coins: Double = 0.0,
        @SerializedName("motes_purse")
        val motes: Double = 0.0,
        val essence: Map<String, EssenceData> = emptyMap(),
    )

    data class EssenceData(
        val current: Int = 0
    )

    data class JacobsContestData(
        @SerializedName("medals_inv")
        val medalsInv: Map<String, Int> = emptyMap(),
        //val perks: Map<String, Int> = emptyMap(), //todo cancer
        val contests: Map<String, ContestData> = emptyMap(),
        val talked: Boolean = false,
        @SerializedName("unique_brackets")
        val uniqueBrackets: Map<String, List<String>> = emptyMap(),
        val migration: Boolean = false,
        @SerializedName("personal_bests")
        val personalBests: Map<String, Long> = emptyMap(),
    )

    data class ContestData(
        val collected: Long = 0,
        @SerializedName("claimed_rewards")
        val claimed: Boolean = false,
        @SerializedName("claimed_position")
        val position: Long = 0,
        @SerializedName("claimed_participants")
        val participants: Long = 0,
    )

    data class MiscItemData(
        val soulflow: Long = 0,
        @SerializedName("favorite_arrow")
        val favoriteArrow: String? = null,
    )

    data class LevelingData(
        val experience: Long = 0,
        val completions: Map<String, Int> = emptyMap(),
        val completed: List<String> = emptyList(),
        @SerializedName("migrated_completions")
        val migratedComps: Boolean = false,
        @SerializedName("completed_tasks")
        val completedTasks: List<String> = emptyList(),
        @SerializedName("category_expanded")
        val expanded: Boolean = false,
        @SerializedName("last_viewed_tasks")
        val lastViewedTasks: List<String> = emptyList(),
        @SerializedName("highest_pet_score")
        val highestPetScore: Int = 0,
        @SerializedName("mining_fiesta_ores_mined")
        val fiestaOres: Long = 0,
        val migrated: Boolean = false,
        @SerializedName("migrated_completions_2")
        val migratedCompletions2: Boolean = false,
        @SerializedName("fishing_festival_sharks_killed")
        val sharksKilled: Long = 0,
        @SerializedName("claimed_talisman")
        val talisman: Boolean = false,
        @SerializedName("bop_bonus")
        val bopBonus: String = "",
        @SerializedName("emblem_unlocks")
        val emblemUnlocks: List<String> = emptyList(),
        @SerializedName("task_sort")
        val taskSort: String? = null,
        @SerializedName("selected_symbol")
        val selectedSymbol: String? = null,
    )

    data class AccessoryBagStorage(
        val tuning: TuningData = TuningData(),
        @SerializedName("selected_power")
        val selectedPower: String? = null,
        @SerializedName("unlocked_powers")
        val unlockedPowers: List<String> = emptyList(),
        @SerializedName("bag_upgrades_purchased")
        val bagUpgrades: Int = 0,
        @SerializedName("highest_magical_power")
        val highestMP: Long = 0,
    )

    data class TuningData(
        @SerializedName("slot_0")
        val currentTunings: Map<String, Int> = emptyMap(),
        val highestUnlockedSlot: Int = 0,
    )

    data class GardenPlayerData(
        val copper: Int = 0,
    )

    data class EventsData(
        val easter: EasterEvent = EasterEvent()
    )

    data class EasterEvent(
        //todo this nonsense //val rabbits: Map<String, Int> = emptyMap()
        @SerializedName("time_tower")
        val timeTower: TimeTowerData = TimeTowerData(),
        val employees: Map<String, Int> = emptyMap(),
        @SerializedName("total_chocolate")
        val totalChocolate: Long = 0,
        @SerializedName("last_viewed_chocolate_factory")
        val lastViewed: Long? = null,
        @SerializedName("rabbit_barn_capacity_level")
        val barnCapacity: Int = 0,
        val shop: EasterShopData = EasterShopData(),
        @SerializedName("chocolate_level")
        val chocolateLevel: Int = 0,
        val chocolate: Long = 0,
        @SerializedName("chocolate_since_prestige")
        val chocolateSincePrestige: Long = 0,
        @SerializedName("click_upgrades")
        val clickUpgrades: Int = 0,
        @SerializedName("chocolate_multiplier_upgrades")
        val chocolateMultiplierUpgrades: Int = 0,
        @SerializedName("rabbit_rarity_upgrades")
        val rabbitRarityUpgrades: Int = 0,
    )

    data class EasterShopData(
        val year: Int? = null,
        val rabbits: List<String> = emptyList(),
        @SerializedName("rabbits_purchases")
        val rabbitsPurchases: List<String> = emptyList(),
        @SerializedName("chocolate_spent")
        val chocolateSpent: Long = 0,
    )

    data class TimeTowerData(
        val charges: Int = 0,
        @SerializedName("activation_time")
        val activationTime: Long? = null,
        val level: Int = 0,
        @SerializedName("last_charge_time")
        val lastChargeTime: Long? = null,
    )

    data class PlayerData(
        @SerializedName("visited_zones")
        val visitedZones: List<String> = emptyList(),
        @SerializedName("last_death")
        val lastDeath: Long? = null,
        val perks: Map<String, Int> = emptyMap(), //todo this maybe? seems annoying. probably need a custom basic deserializer
        @SerializedName("achievement_spawned_island_types")
        val achievementSpawnedIslandTypes: List<String> = emptyList(),
        @SerializedName("active_effects")
        val activeEffects: List<ActiveEffect> = emptyList(),
        @SerializedName("paused_effects")
        val pausedEffect: List<ActiveEffect> = emptyList(),
        @SerializedName("temp_stat_buffs")
        val tempStatBuffs: List<TempStatBuff> = emptyList(),
        @SerializedName("death_count")
        val deathCount: Long = 0,
        @SerializedName("disabled_potion_effects")
        val disabledPotionEffects: List<String> = emptyList(),
        @SerializedName("visited_modes")
        val vistedModes: List<String> = emptyList(),
        @SerializedName("unlocked_coll_tiers")
        val unLockedCollTiers: List<String> = emptyList(),
        @SerializedName("crafted_generators")
        val craftedGenerators: List<String> = emptyList(),
        @SerializedName("fishing_treasure_caught")
        val fishingTreasureCaught: Long = 0,
        val experience: Map<String, Double> = emptyMap(),
        @SerializedName("fastest_target_practice")
        val fastestTargetPractice: Double? = null,
    )

    data class Experience( //maybe useful but accessing as a list is better for most situations.
        @SerializedName("SKILL_FISHING")
        val fishing: Double = 0.0,
        @SerializedName("SKILL_ALCHEMY")
        val alchemy: Double = 0.0,
        @SerializedName("SKILL_RUNECRAFTING")
        val runeCrafting: Double = 0.0,
        @SerializedName("SKILL_MINING")
        val mining: Double = 0.0,
        @SerializedName("SKILL_FARMING")
        val farming: Double = 0.0,
        @SerializedName("SKILL_ENCHANTING")
        val enchanting: Double = 0.0,
        @SerializedName("SKILL_TAMING")
        val taming: Double = 0.0,
        @SerializedName("SKILL_FORAGING")
        val foraging: Double = 0.0,
        @SerializedName("SKILL_SOCIAL")
        val social: Double = 0.0,
        @SerializedName("SKILL_CARPENTRY")
        val carpentry: Double = 0.0,
        @SerializedName("SKILL_COMBAT")
        val combat: Double = 0.0,
    )

    data class TempStatBuff(
        val stat: Int? = null,
        @SerializedName("stat_id")
        val statId: String? = null,
        val key: String,
        val amount: Int,
        @SerializedName("expire_at")
        val expireAt: Long,
    )

    data class ActiveEffect(
        val effect: String,
        val level: Int,
        val modifiers: List<EffectModifier> = emptyList(),
        @SerializedName("ticks_remaining")
        val ticksRemaining: Long,
        val infinite: Boolean
    )

    data class EffectModifier(
        val key: String,
        val amp: Int,
    )

    data class Inventory(
        @SerializedName("inv_contents")
        val invContents: InventoryContents = InventoryContents(),
        @SerializedName("ender_chest_contents")
        val eChestContents: InventoryContents = InventoryContents(),
        @SerializedName("backpack_icons")
        val backpackIcons: Map<String, InventoryContents> = emptyMap(),
        @SerializedName("bag_contents")
        val bagContents: Map<String, InventoryContents> = emptyMap(),
        @SerializedName("inv_armor")
        val invArmor: InventoryContents = InventoryContents(),
        @SerializedName("equipment_contents")
        val equipment: InventoryContents = InventoryContents(),
        @SerializedName("wardrobe_equipped_slot")
        val wardrobeEquipped: Int? = null,
        @SerializedName("backpack_contents")
        val backpackContents: Map<String, InventoryContents> = emptyMap(),
        @SerializedName("sacks_counts")
        val sacks: Map<String, Long> = emptyMap(),
        @SerializedName("personal_vault_contents")
        val personalVault: InventoryContents = InventoryContents(),
        @SerializedName("wardrobe_contents")
        val wardrobeContents: InventoryContents = InventoryContents()
    )

    data class BankingData(
        val balance: Double = 0.0,
        val transactions: List<BankTransactions> = emptyList()
    )

    data class BankTransactions(
        val amount: Double,
        val timestamp: Long,
        val action: String,
        @SerializedName("initiator_name")
        val initiator: String,
    )

    data class InventoryContents(
        val type: Int? = null,
        val data: String = ""
    ) {
        @OptIn(ExperimentalEncodingApi::class)
        val itemStacks: List<ItemData?> get() = with(data) {
            if (isEmpty()) return emptyList()
            val nbtCompound = NbtIo.readCompressed(Base64.decode(this).inputStream(), NbtAccounter.unlimitedHeap())
            val itemNBTList = nbtCompound.getList("i").getOrNull() ?: return emptyList()
            itemNBTList.indices.map { i ->
                val compound = itemNBTList.getCompound(i).getOrNull()?.takeIf { it.size() > 0 } ?: return@map null
                val tag = compound.get("tag")?.asCompound()?.get() ?: return@map null
                val id = tag.get("ExtraAttributes")?.asCompound()?.get()?.get("id")?.asString()?.get() ?: ""
                val display = tag.get("display")?.asCompound()?.get() ?: return@map null
                val name = display.get("Name")?.asString()?.get() ?: ""
                val lore = display.get("Lore")?.asList()?.get()?.mapNotNull { it.asString().getOrNull() } ?: emptyList()
                ItemData(name, id, lore)
            }
        }
    }

    data class ItemData(
        val name: String,
        val id: String,
        val lore: List<String>,
    )

    data class CommunityUpgrades(
        @SerializedName("upgrade_states")
        val upgradeStates: List<CommunityUpgrade> = emptyList(),
    )

    data class CommunityUpgrade(
        val upgrade: String,
        val tier: Int,
        @SerializedName("started_ms")
        val startedMs: Long,
        @SerializedName("started_by")
        val startedBy: String,
        @SerializedName("claimed_by")
        val claimedBy: String,
    )
}