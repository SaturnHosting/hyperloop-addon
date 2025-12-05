package dev.saturn.hyperloop;

import dev.saturn.hyperloop.commands.HyperloopCommand;
import dev.saturn.hyperloop.modules.HyperloopModule;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class Hyperloop extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Hyperloop");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Hyperloop");

        // Modules
        Modules.get().add(new HyperloopModule());

        // Commands
        Commands.add(new HyperloopCommand());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "dev.saturn.hyperloop";
    }
}
