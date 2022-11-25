package raylcast.money;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;

public class MusicPlayer {
    private static final double JUKEBOX_RANGE_MULTIPLY = 16.0;
    private static final float JUKEBOX_VOLUME = 1.0f;

    public void playSound(Location location, String sound){
        location.getWorld().playSound(location, sound, SoundCategory.VOICE, JUKEBOX_VOLUME, 1.0f);
    }

    public void stopSound(Location location, String sound){
        var affectedPlayers = location.getWorld().getNearbyPlayers(location, JUKEBOX_VOLUME * JUKEBOX_RANGE_MULTIPLY);

        for(var player : affectedPlayers){
            player.stopSound(sound, SoundCategory.VOICE);
        }
    }

}
