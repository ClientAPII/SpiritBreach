package de.clientapi.spiritBreach;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpiritBreach extends CoreAbility implements AddonAbility {

    private long cooldown;
    private long dimensionalCooldown;
    private double maxDistance;
    private boolean activated;
    private boolean isDimensionalTravel;
    private Permission perm;

    public SpiritBreach(Player player, boolean isDimensionalTravel) {
        super(player);

        if (!bPlayer.canBendIgnoreBinds(this) || !hasSpirit()) {
            remove();
            return;
        }

        this.isDimensionalTravel = isDimensionalTravel;
        setFields();
        start();
        bPlayer.addCooldown(this);
    }

    private boolean hasSpirit() {
        Element element = Element.getElement("Spirit");
        if (element == null) {
            return false;
        }
        return bPlayer.hasElement(element);
    }

    private void setFields() {
        this.cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.ClientAPI.Spirit.SpiritBreach.Cooldown", 15) * 1000;
        this.dimensionalCooldown = ConfigManager.getConfig().getLong("ExtraAbilities.ClientAPI.Spirit.SpiritBreach.DimensionalCooldown", 900) * 1000;
        this.maxDistance = ConfigManager.getConfig().getDouble("ExtraAbilities.ClientAPI.Spirit.SpiritBreach.MaxDistance", 15.0);
        this.activated = false;
    }

    @Override
    public void progress() {
        if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
            remove();
            return;
        }

        if (!activated) {
            activated = true;
            if (isDimensionalTravel) {
                performDimensionalBreach();
            } else {
                performShortBreach();
            }
        }
    }

    private void performShortBreach() {
        Location targetLocation;
        Location playerLoc = player.getLocation();

        if (player.getTargetBlockExact((int) maxDistance) != null) {
            targetLocation = player.getTargetBlockExact((int) maxDistance).getLocation();
            targetLocation.add(0.5, 1, 0.5);
        } else {
            Vector direction = player.getEyeLocation().getDirection().normalize();
            targetLocation = playerLoc.clone().add(direction.multiply(maxDistance));
        }

        ParticleEffect.PORTAL.display(playerLoc, 10, 0.5, 0.5, 0.5);
        ParticleEffect.PORTAL.display(targetLocation, 10, 0.5, 0.5, 0.5);

        player.teleport(targetLocation, TeleportCause.PLUGIN);
        remove();
    }

    private void performDimensionalBreach() {

        World currentWorld = player.getWorld();
        List<Environment> possibleDestinations = new ArrayList<>();


        if (!currentWorld.getEnvironment().equals(Environment.NORMAL))
            possibleDestinations.add(Environment.NORMAL);
        if (!currentWorld.getEnvironment().equals(Environment.NETHER))
            possibleDestinations.add(Environment.NETHER);
        if (!currentWorld.getEnvironment().equals(Environment.THE_END))
            possibleDestinations.add(Environment.THE_END);


        Random rand = new Random();
        Environment targetEnv = possibleDestinations.get(rand.nextInt(possibleDestinations.size()));


        World targetWorld = null;
        for (World world : player.getServer().getWorlds()) {
            if (world.getEnvironment() == targetEnv) {
                targetWorld = world;
                break;
            }
        }

        if (targetWorld == null) {
           // player.sendMessage("No valid dimension found to travel to!");
            remove();
            return;
        }


        Location targetLoc = findSafeLocation(targetWorld);
        if (targetLoc == null) {
           // player.sendMessage("No safe location found in target dimension!");
            remove();
            return;
        }


        ParticleEffect.PORTAL.display(player.getLocation(), 20, 0.5, 0.5, 0.5);
        ParticleEffect.PORTAL.display(targetLoc, 20, 0.5, 0.5, 0.5);

        player.teleport(targetLoc, TeleportCause.PLUGIN);
        remove();
    }

    private Location findSafeLocation(World world) {
        Random rand = new Random();
        int attempts = 0;
        int maxAttempts = 100;


        int searchRange = switch (world.getEnvironment()) {
            case NETHER -> 2500;
            case THE_END -> 4500;
            default -> 2500;
        };


        int minY, maxY;
        switch (world.getEnvironment()) {
            case NETHER -> {
                minY = 31;
                maxY = 100;
            }
            case THE_END -> {
                minY = 45;
                maxY = 80;
            }
            default -> {
                minY = 63;
                maxY = 150;
            }
        }

        while (attempts < maxAttempts) {
            int x = rand.nextInt(searchRange) - (searchRange / 2);
            int z = rand.nextInt(searchRange) - (searchRange / 2);

            for (int y = maxY; y >= minY; y--) {
                Location loc = new Location(world, x, y, z);

                if (isSafeLocation(loc)) {

                    loc.add(0.5, 1, 0.5);
                    loc.setYaw(player.getLocation().getYaw());
                    loc.setPitch(player.getLocation().getPitch());
                    return loc;
                }
            }
            attempts++;
        }


        Location spawn = world.getSpawnLocation();
        if (isSafeLocation(spawn)) {
            spawn.add(0.5, 1, 0.5);
            spawn.setYaw(player.getLocation().getYaw());
            spawn.setPitch(player.getLocation().getPitch());
            return spawn;
        }

        return null;
    }

    private boolean isSafeLocation(Location location) {
        Block block = location.getBlock();
        Block ground = block.getRelative(BlockFace.DOWN);
        Block head = block.getRelative(BlockFace.UP);

        // List of valid ground block types
        Material groundType = ground.getType();
        boolean isValidGround = switch (location.getWorld().getEnvironment()) {
            case NETHER -> groundType == Material.NETHERRACK ||
                    groundType == Material.SOUL_SAND ||
                    groundType == Material.SOUL_SOIL;
            case THE_END -> groundType == Material.END_STONE;
            case NORMAL -> groundType == Material.GRASS_BLOCK ||
                    groundType == Material.STONE ||
                    groundType == Material.SAND;
            default -> false;
        };

        return isValidGround &&
                block.getType() == Material.AIR &&
                head.getType() == Material.AIR &&
                !block.isLiquid() &&
                !head.isLiquid();
    }

    @Override
    public long getCooldown() {
        return isDimensionalTravel ? dimensionalCooldown : cooldown;
    }

    @Override
    public boolean isSneakAbility() {
        return isDimensionalTravel;
    }

    @Override
    public boolean isHarmlessAbility() {
        return true;
    }

    @Override
    public boolean isIgniteAbility() {
        return false;
    }

    @Override
    public boolean isExplosiveAbility() {
        return false;
    }

    @Override
    public String getName() {
        return "SpiritBreach";
    }

    @Override
    public Location getLocation() {
        return player.getLocation();
    }

    @Override
    public void load() {
        ConfigManager.getConfig().addDefault("ExtraAbilities.ClientAPI.Spirit.SpiritBreach.Cooldown", 15); // 15 seconds
        ConfigManager.getConfig().addDefault("ExtraAbilities.ClientAPI.Spirit.SpiritBreach.DimensionalCooldown", 900); // 15 minutes in seconds
        ConfigManager.getConfig().addDefault("ExtraAbilities.ClientAPI.Spirit.SpiritBreach.MaxDistance", 15.0);
        ConfigManager.defaultConfig.save();

        perm = new Permission("bending.ability.SpiritBreach");
        perm.setDefault(PermissionDefault.TRUE);
        ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);

        SpiritBreachListener listener = new SpiritBreachListener((SpiritBreach) ProjectKorra.plugin.getServer().getPluginManager().getPlugin("SpiritBreach"));
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);
    }

    @Override
    public void stop() {
        ProjectKorra.plugin.getServer().getPluginManager().removePermission(perm);
        remove();
    }

    @Override
    public String getAuthor() {
        return "ClientAPII";
    }

    @Override
    public String getVersion() {
        return "1.1.1";
    }

    @Override
    public String getDescription() {
        return "Allows the user to teleport up to 15 blocks in any direction with left-click (15s cooldown) and travel between dimensions with sneak (15min cooldown).";
    }

    @Override
    public String getInstructions() {
        return "Left-click to teleport in the direction you're looking, or sneak to travel to a random dimension.";
    }

    @Override
    public Element getElement() {
        return Element.getElement("Spirit");
    }
}