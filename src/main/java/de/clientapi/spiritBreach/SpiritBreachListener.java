package de.clientapi.spiritBreach;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SpiritBreachListener implements Listener {

    private final SpiritBreach plugin;

    public SpiritBreachListener(SpiritBreach plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) {
            return;
        }

        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

        if (bPlayer == null) {
            return;
        }

        if (!bPlayer.getBoundAbilityName().equalsIgnoreCase("SpiritBreach")) {
            return;
        }

        if (bPlayer.canBend(CoreAbility.getAbility(de.clientapi.spiritBreach.SpiritBreach.class))) {
            new de.clientapi.spiritBreach.SpiritBreach(player);
        }
    }
}