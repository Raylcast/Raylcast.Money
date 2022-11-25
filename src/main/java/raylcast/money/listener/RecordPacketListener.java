package raylcast.money.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import raylcast.money.Money;
import raylcast.money.RecordRecord;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class RecordPacketListener extends PacketAdapter {
    private static final Map<Material, Sound> SOUND_MAP = new HashMap<>() {{
        put(Material.MUSIC_DISC_11, Sound.MUSIC_DISC_11);
        put(Material.MUSIC_DISC_13, Sound.MUSIC_DISC_13);
        put(Material.MUSIC_DISC_BLOCKS, Sound.MUSIC_DISC_BLOCKS);
        put(Material.MUSIC_DISC_CAT, Sound.MUSIC_DISC_CAT);
        put(Material.MUSIC_DISC_CHIRP, Sound.MUSIC_DISC_CHIRP);
        put(Material.MUSIC_DISC_FAR, Sound.MUSIC_DISC_FAR);
        put(Material.MUSIC_DISC_MALL, Sound.MUSIC_DISC_MALL);
        put(Material.MUSIC_DISC_MELLOHI, Sound.MUSIC_DISC_MELLOHI);
        put(Material.MUSIC_DISC_STAL, Sound.MUSIC_DISC_STAL);
        put(Material.MUSIC_DISC_STRAD, Sound.MUSIC_DISC_STRAD);
        put(Material.MUSIC_DISC_WAIT, Sound.MUSIC_DISC_WAIT);
        put(Material.MUSIC_DISC_WARD, Sound.MUSIC_DISC_WARD);
        put(Material.MUSIC_DISC_PIGSTEP, Sound.MUSIC_DISC_PIGSTEP);
    }};

    public RecordPacketListener(Plugin plugin, ListenerPriority priority) {
        super(plugin, priority, PacketType.Play.Server.WORLD_EVENT);
    }

    @Override
    public void onPacketSending(PacketEvent e){
        final PacketContainer packet = e.getPacket();
        final StructureModifier<BlockPosition> position = packet.getBlockPositionModifier();
        final BlockPosition blockPosition = position.getValues().get(0);
        final Player player = e.getPlayer();
        final World world = player.getWorld();
        final Location location = new Location(world, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
        final Block block = world.getBlockAt(location);
        final BlockState blockState = block.getState();
        final Integer data = packet.getIntegers().read(1);

        if (!(blockState instanceof Jukebox jukebox) || data.equals(0)) {
            return;
        }

        final ItemStack disc = jukebox.getRecord();

        if (!disc.hasItemMeta()){
            return;
        }

        var meta = disc.getItemMeta();

        if (!meta.hasCustomModelData()){
            return;
        }

        int customModelData = meta.getCustomModelData();

        var record = Money.SoundMap.get(customModelData);

        if (record == null){
            return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            player.stopSound(SOUND_MAP.get(disc.getType()), SoundCategory.RECORDS);
            sendNowPlayingMessage(player, record);
        }, 0);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            player.stopSound(SOUND_MAP.get(disc.getType()), SoundCategory.RECORDS);
        }, 3);
    }

    private void sendNowPlayingMessage(Player player, RecordRecord record){
        final BaseComponent[] baseComponents =  new ComponentBuilder()
                .append("Now playing: ").color(ChatColor.GOLD)
                .append(record.SongName).color(ChatColor.GREEN)
                .create();
        player.sendActionBar(baseComponents);
    }
}
