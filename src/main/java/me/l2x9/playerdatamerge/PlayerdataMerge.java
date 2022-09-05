package me.l2x9.playerdatamerge;

import net.minecraft.server.v1_12_R1.EnumGamemode;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class PlayerdataMerge extends JavaPlugin {

    @Override
    public void onEnable() {
        File toMergeDir = new File(".", "merge");
        File playerDataDir = new File("world", "playerdata");
        File backupDir = new File("PlayerData-Backup");
        backupDir.mkdirs();
        if (toMergeDir.exists() && toMergeDir.isDirectory()) {
            for (File mergingFile : toMergeDir.listFiles((dir, name) -> name.endsWith(".dat"))) {
                try {
                    NBTTagCompound playerData = NBTUtils.readData(mergingFile, getLogger());
                    if (playerData == null) {
                        log("Failed to preform tasks on %s", mergingFile.getName());
                        continue;
                    }
                    NBTTagCompound bukkit = playerData.getCompound("bukkit");
                    String name = bukkit.getString("lastKnownName");
                    Location location = NBTUtils.readPosition(playerData);
                    location.setY(location.getWorld().getHighestBlockYAt(location));
                    NBTUtils.writePosition(playerData, location);
                    playerData.setBoolean("Invulnerable", false);
                    playerData.setInt("playerGameType", EnumGamemode.SURVIVAL.getId());
                    NBTUtils.writeNBT(mergingFile, playerData, getLogger());
                    File playerdataFile = new File(playerDataDir, mergingFile.getName());
                    if (playerdataFile.exists()) {
                        File backup = new File(backupDir, playerdataFile.getName());
                        if (!backup.exists()) {
                            Files.move(playerdataFile.toPath(), backup.toPath());
                            log("Created backup for %s(%s)", name, playerdataFile.getName().replace(".dat", ""));
                        }
                    }
                    Files.copy(mergingFile.toPath(), Paths.get("world", "playerdata", playerdataFile.getName()));
                    log("Copied playerdata for %s(%s)", name, playerdataFile.getName().replace(".dat", ""));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } else {
            log("The directory %s does not exist", toMergeDir.getAbsolutePath());
            System.exit(0);
        }
    }

    private void log(String format, Object... args) {
        getLogger().info(ChatColor.translateAlternateColorCodes('&', String.format(format, args)));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
