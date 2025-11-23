package dev.saturn.hyperloop.modules;

import dev.saturn.hyperloop.Hyperloop;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class HyperloopModule extends Module {
    public final SettingGroup sgGeneral = this.settings.getDefaultGroup();

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



}
