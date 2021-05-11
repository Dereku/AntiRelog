package ru.leymooo.antirelog.listeners;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import net.ess3.api.events.teleport.PreTeleportEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.leymooo.antirelog.config.Settings;
import ru.leymooo.antirelog.manager.PvPManager;

import java.util.UUID;

public class EssentialsTeleportListener implements Listener {

    private final PvPManager pvpManager;
    private final Settings settings;
    private final PluginCommand essentialsTpacceptCommand;

    public EssentialsTeleportListener(PvPManager pvpManager, Settings settings) {
        this.pvpManager = pvpManager;
        this.settings = settings;
        this.essentialsTpacceptCommand = Bukkit.getPluginCommand("tpaccept");
    }

    @EventHandler
    public void onPreTeleport(PreTeleportEvent event) {
        if (settings.isDisableTeleportsInPvp() && pvpManager.isInPvP(event.getTeleportee().getBase())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (this.essentialsTpacceptCommand == null) {
            return;
        }
        final String[] split = event.getMessage().toLowerCase().split(" ");
        if (this.essentialsTpacceptCommand.getAliases().contains(split[0]) || this.essentialsTpacceptCommand.getLabel().equals(split[0])) {
            final Essentials plugin = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            if (plugin == null) {
                return; // NPE Check. Without ClassCastException check, ofc. :genius:
            }
            final User userTpacceptIssuer = plugin.getUser(event.getPlayer());
            final UUID teleportRequesterUUID = userTpacceptIssuer.getTeleportRequest();
            final Player requester = Bukkit.getServer().getPlayer(teleportRequesterUUID);
            if (settings.isDisableTeleportsInPvp() && pvpManager.isInPvP(requester)) {
                userTpacceptIssuer.requestTeleport(null, false);
            }
        }
    }
}
