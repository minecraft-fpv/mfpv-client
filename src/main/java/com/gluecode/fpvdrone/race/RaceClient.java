package com.gluecode.fpvdrone.race;

import com.gluecode.fpvdrone.Main;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.text.MessageFormat;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class RaceClient {
  // Build mode stuff:
  public static boolean isBuildMode = false;
  public static final ArrayList<SerialRaceGate> builtGates = new ArrayList<>();

  // These variable apply only to the current track the user is on according to userToTrack:
  public static HashMap<UUID, UUID> currentTrackId = new HashMap<>();
  public static final HashMap<UUID, Boolean> isRacingMode = new HashMap<>();
  public static final HashMap<UUID, Long> userStartTime = new HashMap<>();
  public static final HashMap<UUID, Integer> userBestTime = new HashMap<>();
  public static final HashMap<UUID, Integer> userGateIndex = new HashMap<>();

  // Users are added and removed as needed:
  public static final HashMap<UUID, UUID> userToTrack = new HashMap<>();
  public static final HashMap<UUID, HashMap<UUID, Boolean>> trackToUsers = new HashMap<>();

  // Values added will not be removed:
  public static final HashMap<UUID, SerialRaceTrack> loadedTracks = new HashMap<>();
  public static final HashMap<UUID, ArrayList<LapTime>> trackBestTimes = new HashMap<>();

  public static void handleLapStartPacket(long startTimeMillis, UUID uuid) {
    // use client's time instead of server time because client may have unsynced time.
    userStartTime.put(uuid, System.currentTimeMillis());
  }

  public static void handleLapBestPacket(
    int millis,
    UUID trackId,
    UUID userId
  ) {
    // Store all best times for the track, but also the current user's time in case the
    // current user isn't in the top list.
    userBestTime.put(userId, millis);

    if (trackBestTimes.get(trackId) == null) {
      trackBestTimes.put(trackId, new ArrayList<>());
    }
    ArrayList<LapTime> list = trackBestTimes.get(trackId);

    // only add the time if the UUID isn't in here already:
    boolean found = false;

    for (LapTime lapTime : list) {
      if (lapTime.userId.equals(userId)) {
        // Otherwise update the existing object
        lapTime.millis = millis;

        found = true;
        break;
      }
    }
    if (!found) {
      list.add(new LapTime(userId, millis));
    }

    // sort and only keep the top ten
    Collections.sort(list);
    if (list.size() > 10) {
      list.remove(10);
    }
  }

  public static void handleBuildModePacket(boolean value) {
    if (value != isBuildMode) {
      builtGates.clear();
    }
    isBuildMode = value;
  }

  public static void addGate(SerialRaceGate gate) {
    builtGates.add(gate);
  }

  public static void handleRaceModePacket(
    boolean value,
    UUID trackId,
    UUID userId
  ) {
    if (value) {
      handleStartRace(trackId, userId);
    } else {
      handleStopRacing(trackId, userId);
    }
  }

  public static void handleStartRace(UUID trackId, UUID userId) {
    currentTrackId.put(userId, trackId);
    isRacingMode.put(userId, true);
    userToTrack.put(userId, trackId);
    userGateIndex.put(userId, 0);

    if (trackToUsers.get(trackId) == null) {
      trackToUsers.put(trackId, new HashMap<>());
    }
    trackToUsers.get(trackId).put(userId, true);

    userStartTime.remove(userId);
    userBestTime.remove(userId);
  }

  public static void handleStopRacing(UUID trackId, UUID userId) {
    currentTrackId.remove(userId);
    isRacingMode.remove(userId);
    userToTrack.remove(userId);
    userGateIndex.remove(userId);

    if (trackToUsers.get(trackId) != null) {
      trackToUsers.get(trackId).remove(userId);
    }

    userStartTime.remove(userId);
    userBestTime.remove(userId);
  }

  public static void handleGateIndexPacket(
    int gateIndex,
    UUID trackId,
    UUID userId
  ) {
    userGateIndex.put(userId, gateIndex);
  }

  public static void loadTrack(SerialRaceTrack track) {
    loadedTracks.put(track.trackId, track);
  }

  public static boolean checkRacingMode(UUID userId) {
    Boolean isRacing = isRacingMode.get(userId);
    return isRacing != null && isRacing;
  }

  public static String formatTime(int millis) {
    int hours = millis / (1000 * 60 * 60);
    millis -= hours * (1000 * 60 * 60);
    int minutes = millis / (1000 * 60);
    millis -= minutes * (1000 * 60);
    int seconds = millis / 1000;
    millis -= seconds * 1000;
    int tens = millis / 10;
    String tensString = "" + tens;
    if (tensString.length() == 1) {
      tensString += "0";
    }
    if (hours > 0) {
      return MessageFormat.format(
        "{0}:{1}:{2}:{3}",
        hours,
        minutes,
        seconds,
        tensString
      );
    } else if (minutes > 0) {
      return MessageFormat.format(
        "{0}:{1}:{2}",
        minutes,
        seconds,
        tensString
      );
    } else {
      return MessageFormat.format("{0}:{1}", seconds, tensString);
    }
  }
}
