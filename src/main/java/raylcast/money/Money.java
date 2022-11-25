package raylcast.money;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Jukebox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import raylcast.money.listener.RecordPacketListener;

import java.util.*;

public final class Money extends JavaPlugin implements Listener {

    private final FileConfiguration Config;
    private final MusicPlayer Player;

    public static final Map<Integer, RecordRecord> SoundMap = new HashMap<>() {{
        put(44892444, new RecordRecord("minecraft:raylcast.music.gravitacion", UUID.fromString("8b76d61d-80e6-4483-9ff8-bad1cdd161a7"), "Gravitacion"));
    }};
    public static final Map<UUID, Integer> PlayerSoundMap = new HashMap<>();

    public static final Map<UUID, ItemStack> HeadCache = new HashMap<>();
    private final List<Material> Discs = new ArrayList<>() {{
        add(Material.MUSIC_DISC_11);
        add(Material.MUSIC_DISC_13);
        add(Material.MUSIC_DISC_WAIT);
        add(Material.MUSIC_DISC_PIGSTEP);
        add(Material.MUSIC_DISC_OTHERSIDE);
        add(Material.MUSIC_DISC_BLOCKS);
        add(Material.MUSIC_DISC_CHIRP);
        add(Material.MUSIC_DISC_CAT);
        add(Material.MUSIC_DISC_FAR);
        add(Material.MUSIC_DISC_MALL);
        add(Material.MUSIC_DISC_MELLOHI);
        add(Material.MUSIC_DISC_WARD);
        add(Material.MUSIC_DISC_STAL);
        add(Material.MUSIC_DISC_STRAD);
    }};

    public Money(){
        Config = getConfig();
        java.util.Random random = new Random();
        Player = new MusicPlayer();
    }

    @Override
    public void onLoad(){
        super.onLoad();
        Config.addDefault("diamondMoneyChance", 0.5);
        Config.addDefault("debrisMoneyChance", 1.0);
        Config.options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        com.comphenix.protocol.ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(new RecordPacketListener(this, ListenerPriority.NORMAL));

        Bukkit.addRecipe(getPennyRecipe());
        Bukkit.addRecipe(getDollarRecipe());

        SoundMap.forEach((cmd, r) -> {
            PlayerSoundMap.put(r.OwnerId, cmd);
        });

        var rr1 = new RecordRecord("minecraft:raylcast.clans.ascend", UUID.fromString("8b76d61d-80e6-4483-9ff8-bad1cdd161a7"), "Heart of Courage");
        SoundMap.put(44892445, rr1);
        Bukkit.addRecipe(getRecordItemRecipe(44892445, rr1.SongName, new ItemStack(Material.DRAGON_HEAD)));
    }

    @Override
    public void onDisable() {
        saveConfig();
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        e.getPlayer().setResourcePack("https://cloud.playwo.de/s/EWXYwXkH8JHLAqC/download/mp.zip", "68b45967f5bd3ab0f026b092220cb59668c8f63a");
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent e){
        var player = e.getPlayer();
        var display = e.getAdvancement().getDisplay();

        if (display == null || display.isHidden()){
            return;
        }
        if (display.doesAnnounceToChat()){
            player.getInventory().addItem(getDollarItem(2));
            return;
        }

        player.getInventory().addItem(getPennyItem(6));
    }
    @EventHandler
    public void onPlayerRecipeDiscover(PlayerRecipeDiscoverEvent e){
        e.getPlayer().getInventory().addItem(getPennyItem(1));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        var killer = e.getPlayer().getKiller();

        if (killer == null){
            return;
        }
        e.getDrops().add(getPlayerHead(e.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        var block = e.getBlock();
        var player = e.getPlayer();

        if (block.getState() instanceof Jukebox jukebox){
            stopPlayingSounds(jukebox);
            return;
        }

        if (!e.isDropItems()){
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE){
            return;
        }

        var mainItem = player.getInventory().getItemInMainHand();
        if (mainItem.containsEnchantment(Enchantment.SILK_TOUCH)){
            return;
        }

        if (block.getType() != Material.DIAMOND_ORE && block.getType() != Material.DEEPSLATE_DIAMOND_ORE) {
            return;
        }

        int totalMined = getConfig().getInt("Players." + player.getUniqueId().toString() + ".TotalMined");

        int pennyDropCount = Math.max(10 - totalMined / 32, 1);

        if (pennyDropCount >= 9) {
            player.getWorld().dropItemNaturally(block.getLocation(), getDollarItem(1));
            pennyDropCount -= 9;
        }

        if (pennyDropCount > 0){
            player.getWorld().dropItemNaturally(block.getLocation(), getPennyItem(pennyDropCount));
        }

        getConfig().set("Players." + player.getUniqueId().toString() + ".TotalMined", totalMined + 1);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }

        var block = e.getClickedBlock();

        if (block == null || block.getType() != Material.JUKEBOX){
            return;
        }

        var state = block.getState();
        if (!(state instanceof Jukebox jukebox)){
            return;
        }

        var record = jukebox.getRecord();

        if (record.getType() != Material.AIR){
            stopPlayingSounds(jukebox);
            return;
        }

        var usedItem = e.getItem();
        if (usedItem == null || !usedItem.hasItemMeta()){
            return;
        }
        var meta = usedItem.getItemMeta();
        if (!meta.hasCustomModelData()){
            return;
        }
        int customModelData = meta.getCustomModelData();

        var r = SoundMap.get(customModelData);

        if (r == null){
            return;
        }

        Player.playSound(jukebox.getLocation(), r.SoundName);
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e){
        var contents = Arrays.stream(e.getInventory().getContents()).filter(c -> c != null && c.getType() != Material.AIR).toList();
        if(contents.size() != 2){
            return;
        }
        if (!Discs.contains(contents.get(0).getType()) && !Discs.contains(contents.get(1).getType())){
            return;
        }
        if (contents.get(0).getType() != Material.PLAYER_HEAD && contents.get(1).getType() != Material.PLAYER_HEAD){
            return;
        }

        SkullMeta meta;

        if (contents.get(0).getType() == Material.PLAYER_HEAD){
            meta = (SkullMeta) contents.get(0).getItemMeta();
        } else{
            meta = (SkullMeta) contents.get(1).getItemMeta();
        }

        var owningPlayer = meta.getOwningPlayer();
        if (owningPlayer == null){
            return;
        }

        Integer cmd = PlayerSoundMap.get(owningPlayer.getUniqueId());
        if (cmd == null){
            return;
        }

        var rr = SoundMap.get(cmd);
        if (rr == null){
            return;
        }

        var disc = makeRecordItem(cmd, rr);
        e.getInventory().setResult(disc);
        e.getRecipe().getResult().set
    }

    private void stopPlayingSounds(Jukebox jukebox){
        var record = jukebox.getRecord();

        if (record.getType() == Material.AIR){
            return;
        }

        if (!record.hasItemMeta()){
            return;
        }

        var meta = record.getItemMeta();

        if (!meta.hasCustomModelData()){
            return;
        }

        int customModelData = meta.getCustomModelData();

        var r = SoundMap.get(customModelData);

        if (r == null){
            return;
        }

        Player.stopSound(jukebox.getLocation(), r.SoundName);
    }

    private ItemStack getDollarItem(int amount){
        var money = new ItemStack(Material.PUFFERFISH, amount);
        var meta = money.getItemMeta();

        meta.setCustomModelData(34892394);

        var lore = new ArrayList<Component>();

        lore.add(
            Component.text("The main currency in the shores", NamedTextColor.DARK_GREEN)
        );
        lore.add(
            Component.text("NOT EDIBLE!", NamedTextColor.DARK_RED)
        );

        var displayName = Component.text("Dollar $$$");

        meta.lore(lore);
        meta.displayName(displayName);

        money.setItemMeta(meta);
        return money;
    }

    private ItemStack getPennyItem(int amount){
        var money = new ItemStack(Material.GOLD_NUGGET, amount);
        var meta = money.getItemMeta();

        meta.setCustomModelData(44892394);

        var lore = new ArrayList<Component>();

        lore.add(
            Component.text("The main sub currency in the shores", NamedTextColor.DARK_GREEN)
        );

        var displayName = Component.text("Penny $$$");

        meta.lore(lore);
        meta.displayName(displayName);

        money.setItemMeta(meta);
        return money;
    }

    private ItemStack getPlayerHead(UUID uuid){
        var head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta)head.getItemMeta();

        var player = Bukkit.getPlayer(uuid);
        var profile = Bukkit.createProfile(uuid);

        if (player == null) {
            if(profile.complete(true, true)){
                getLogger().warning("API Load");
                for (ProfileProperty property : profile.getProperties()) {
                    getLogger().warning(property.getSignature());
                }
            }else{
                getLogger().warning("API LOAD FAIL");
            }
            profile = Bukkit.createProfile(uuid);
        }


        if(profile.completeFromCache( true)){
            getLogger().warning("Cache Load");
            for (ProfileProperty property : profile.getProperties()) {
                getLogger().warning(property.getSignature());
            }
        } else {
            getLogger().warning("CACHE LOAD FAIL");
        }

        meta.setPlayerProfile(profile);
        head.setItemMeta(meta);
        return head;
    }
    private ItemStack makeRecordItem(int customModelData, RecordRecord r){
        return makeRecordItem(customModelData, r.SongName);
    }
    private ItemStack makeRecordItem(int customModelData, String songName){
        var disc = new ItemStack(Material.MUSIC_DISC_PIGSTEP);
        var meta = disc.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.setCustomModelData(customModelData);
        meta.setDisplayName(songName);
        disc.setItemMeta(meta);
        return disc;
    }

    private ShapelessRecipe getDollarRecipe(){
        var key = new NamespacedKey(this, "money.dollar");
        var recipe = new ShapelessRecipe(key, getDollarItem(1));

        recipe.addIngredient(9, getPennyItem(1));
        return recipe;
    }
    private ShapelessRecipe getPennyRecipe(){
        var key = new NamespacedKey(this, "money.penny");
        var recipe = new ShapelessRecipe(key, getPennyItem(9));

        recipe.addIngredient(1, getDollarItem(1));
        return recipe;
    }
    private ShapelessRecipe getRecordRecipe(int customModelData, RecordRecord r){
        var head = getPlayerHead(r.OwnerId);
        return getRecordItemRecipe(customModelData, r.SongName, head);
    }
    private ShapelessRecipe getRecordItemRecipe(int customModelData, String songName, ItemStack item){
        var key = new NamespacedKey(this, "money." + songName.replace(' ', '_').toLowerCase());
        var recipe = new ShapelessRecipe(key, makeRecordItem(customModelData, songName));

        recipe.addIngredient(1, item);
        recipe.addIngredient(new RecipeChoice.MaterialChoice(Discs));
        return recipe;
    }
}
