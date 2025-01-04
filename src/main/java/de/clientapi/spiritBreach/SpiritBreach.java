package de.clientapi.spiritBreach;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

public class SpiritBreach extends CoreAbility implements AddonAbility {

    private long cooldown;
    private double maxDistance;
    private boolean activated;

    public SpiritBreach(Player player) {
        super(player);

        if (!bPlayer.canBendIgnoreBinds(this) || !hasSpirit()) {
            remove();
            return;
        }

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
        this.cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.ClientAPI.Spirit.SpiritBreach.Cooldown", 15000);
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
            performBreach();
        }
    }

    private void performBreach() {
        Location targetLocation;
        Location playerLoc = player.getLocation();

        if (player.getTargetBlockExact((int) maxDistance) != null) {
            targetLocation = player.getTargetBlockExact((int) maxDistance).getLocation();
            targetLocation.add(0, 1, 0);
        } else {
            Vector direction = player.getEyeLocation().getDirection().normalize();
            targetLocation = playerLoc.clone().add(direction.multiply(maxDistance));
        }
        ParticleEffect.PORTAL.display(playerLoc, 10, 0.5, 0.5, 0.5);
        ParticleEffect.PORTAL.display(targetLocation, 10, 0.5, 0.5, 0.5);

        player.teleport(targetLocation, TeleportCause.PLUGIN);
        remove();
    }

    @Override
    public boolean isSneakAbility() {
        return true;
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
    public long getCooldown() {
        return cooldown;
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
        ConfigManager.getConfig().addDefault("ExtraAbilities.ClientAPI.Spirit.SpiritBreach.Cooldown", 15000);
        ConfigManager.getConfig().addDefault("ExtraAbilities.ClientAPI.Spirit.SpiritBreach.MaxDistance", 15.0);
        ConfigManager.defaultConfig.save();

        SpiritBreachListener listener = new SpiritBreachListener((SpiritBreach) ProjectKorra.plugin.getServer().getPluginManager().getPlugin("SpiritBreach"));
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);
    }

    @Override
    public void stop() {
        remove();
    }

    @Override
    public String getAuthor() {
        return "ClientAPI";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getDescription() {
        return "Allows the user to teleport up to 15 blocks in any direction and travel between dimensions.";
    }

    @Override
    public String getInstructions() {
        return "Look at a location within range and activate the ability.";
    }

    @Override
    public Element getElement() {
        return Element.getElement("Spirit");
    }
}