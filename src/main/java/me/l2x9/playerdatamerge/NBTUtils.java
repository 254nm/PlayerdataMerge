package me.l2x9.playerdatamerge;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class NBTUtils {

    private static Map<Integer, World> lut = new HashMap<Integer, World>() {{
        put(0, Bukkit.getWorld("world"));
        put(-1, Bukkit.getWorld("world_nether"));
        put(1, Bukkit.getWorld("world_the_end"));
    }};
    public static NBTTagCompound readData(File file, Logger logger) {
        try {
            FileInputStream fis = new FileInputStream(file);
            return NBTCompressedStreamTools.a(fis);
        } catch (Throwable t) {
            logger.severe(String.format("Failed to read playerdata due to %s. Please see the stacktrace below for more info", t.getClass().getName()));
            t.printStackTrace();
            return null;
        }
    }

    public static void writeNBT(File file, NBTTagCompound compound, Logger logger) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            DataOutputStream out = new DataOutputStream(fos);
            NBTCompressedStreamTools.a(compound, (OutputStream) out);
            out.flush();
            out.close();
            fos.close();
        } catch (Throwable t) {
            logger.severe(String.format("Failed to save playerdata due to %s. Please see the stacktrace below for more info", t.getClass().getName()));
        }
    }

    public static long getLastPlayed(File file, Logger logger) {
        NBTTagCompound comp = readData(file,logger);
        return comp.getCompound("bukkit").getLong("firstPlayed");
    }

    public static Location readPosition(NBTTagCompound playerData) {
        NBTTagList pos = playerData.getList("Pos", 6);
        return new Location(lut.getOrDefault(playerData.getInt("Dimension"), Bukkit.getWorlds().get(0)), pos.f(0), pos.f(1), pos.f(2));
    }

    public static NBTTagList doubleNbtList(double... doubles) {
        NBTTagList nbttaglist = new NBTTagList();
        for (double d : doubles) nbttaglist.add(new NBTTagDouble(d));
        return nbttaglist;
    }

    public static void writePosition(NBTTagCompound playerData, Location location) {
        UUID worldUID = location.getWorld().getUID();
        double x = location.getX(), y = location.getY(), z = location.getZ();
        playerData.setLong("WorldUUIDLeast", worldUID.getLeastSignificantBits());
        playerData.setLong("WorldUUIDMost", worldUID.getMostSignificantBits());
        playerData.set("Pos", doubleNbtList(x, y, z));
    }
}
