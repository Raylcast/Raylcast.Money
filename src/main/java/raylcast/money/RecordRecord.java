package raylcast.money;

import java.util.UUID;

public class RecordRecord {
    public String SoundName;
    public UUID OwnerId;
    public String SongName;

    public RecordRecord(String soundName, UUID ownerId, String songName){
        SoundName = soundName;
        OwnerId = ownerId;
        SongName = songName;
    }
}
