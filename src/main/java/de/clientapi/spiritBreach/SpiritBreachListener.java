package de.clientapi.spiritBreach;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SpiritBreachListener implements Listener {

    private final SpiritBreach plugin;

    public SpiritBreachListener(SpiritBreach plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
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

        if (bPlayer.canBend(CoreAbility.getAbility(SpiritBreach.class))) {
            new SpiritBreach(player, false);
        }
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

        if (bPlayer.canBend(CoreAbility.getAbility(SpiritBreach.class))) {
            new SpiritBreach(player, true);
        }
    }
}