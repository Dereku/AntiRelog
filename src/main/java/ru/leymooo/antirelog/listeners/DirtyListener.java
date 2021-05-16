package ru.leymooo.antirelog.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.leymooo.antirelog.Antirelog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirtyListener implements Listener {
    private final Pattern pattern = Pattern.compile("(kick|ban|tempban|ipban)\\s(-s\\s)?([a-z0-9_.]{3,16})(.*)?");
    private final Antirelog plugin;

    public DirtyListener(Antirelog antirelog) {
        this.plugin = antirelog;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        //TODO: Использовать PluginCommand?

        if (!this.plugin.getSettings().isDisablePunishmentDuringPvp()) {
            return;
        }
        final String lowerCaseCommand = event.getMessage().toLowerCase().replace("/", "");
        final Matcher matcher = this.pattern.matcher(lowerCaseCommand);
        if (!matcher.matches()) {
            return;
        }

        final Player player = this.plugin.getServer().getPlayer(matcher.group(3));
        if (player == null) {
            return;
        }
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission("antirelog.punishmentbypass")) {
            if (this.plugin.getPvpManager().isInPvP(player)) {
                this.plugin.getPvpManager().stopPvP(player);
            }
            return;
        }

        if (!this.plugin.getPvpManager().isInPvP(player)) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', this.plugin.getSettings().getPunishmentFailedMessage()));
        final String broadcastMessage = ChatColor.translateAlternateColorCodes(
                '&', this.plugin.getSettings().getPunishmentFailedBroadcast()
        );
        Bukkit.broadcast(
                broadcastMessage.replace("%player%", event.getPlayer().getName())
                .replace("%punished%", matcher.group(3))
                .replace("%command%", matcher.group(1)),
                "antirelog.failpunishment.report"
        );
    }
}
