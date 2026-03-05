package dev.saturn.hyperloop.modules;

import dev.saturn.hyperloop.Hyperloop;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;

public class HyperloopModule extends Module {
    public final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public static boolean teleporting = false;
    public static String botName = null;

    public final Setting<String> apiKey = sgGeneral.add(new StringSetting.Builder()
        .name("api-key")
        .description("Your API key to hyperloop.")
        .defaultValue("token")
        .build()
    );

    public final Setting<String> host = sgGeneral.add(new StringSetting.Builder()
        .name("api-host")
        .description("The host URL/IP to the API.")
        .defaultValue("http://localhost:3000")
        .build()
    );


    public HyperloopModule() {
        super(Hyperloop.CATEGORY, "hyperloop", "Customize the Saturn's hyperloop feature.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if(teleporting && mc.player != null && mc.getNetworkHandler() != null && !botName.isEmpty()) {
            boolean botOnline = mc.getNetworkHandler().getPlayerList()
                .stream()
                .map(PlayerListEntry::getProfile)
                .anyMatch(profile -> profile.name().equalsIgnoreCase(botName));

            if(botOnline) {
                mc.execute(() -> {
                    info("Teleporting to " + botName);
                    mc.getNetworkHandler().sendChatCommand("tpa " + botName);
                });
                teleporting = false;
                botName = "";
            }
        }
    }
}
