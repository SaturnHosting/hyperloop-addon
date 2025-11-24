package dev.saturn.hyperloop.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.saturn.hyperloop.modules.HyperloopModule;
import dev.saturn.hyperloop.util.Utils;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.command.CommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HyperloopCommand extends Command {
    public HyperloopCommand() {
        super("hyperloop", "Lists hyperloop homes (hover for details).", "hp");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        //list
        builder.then(literal("list").executes(context -> {
            new Thread(() -> {
                try {
                    String json = Utils.fetchLoops();

                    JsonObject root = JsonParser.parseString(json).getAsJsonObject();

                    if(root.has("error")) {
                        error("Error: " + root.get("error").getAsString());
                        return;
                    }

                    JsonArray homes = root.getAsJsonArray("homes");

                    if (homes == null || homes.isEmpty()) {
                        error("Error: No homes found.");
                        return;
                    }

                    MutableText finalMessage = Text.literal("Hyperloops: ")
                        .styled(style -> style.withColor(Formatting.DARK_PURPLE).withBold(true));

                    boolean first = true;

                    for (JsonElement e : homes) {
                        if (!e.isJsonObject()) continue;
                        JsonObject h = e.getAsJsonObject();

                        String home = h.get("home").getAsString();
                        String dimension = h.get("dimension").getAsString();
                        int x = h.get("x").getAsInt();
                        int z = h.get("z").getAsInt();

                        Text hover = Text.literal(
                            "Dimension: " + dimension +
                                "\nX: " + x +
                                "\nZ: " + z
                        );

                        Style hoverStyle = Style.EMPTY
                            .withColor(Formatting.LIGHT_PURPLE)
                            .withBold(false)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));

                        if (!first) {
                            finalMessage.append(Text.literal(", "));
                        }
                        first = false;

                        finalMessage.append(Text.literal(home).setStyle(hoverStyle));
                    }

                    if (mc.player != null)
                        mc.player.sendMessage(finalMessage, false);
                    else
                        error("Error: can't send message to player, god knows why...");

                } catch (Exception ex) {
                    error("Error fetching JSON. Is the API online/correct? ");
                    ex.printStackTrace();
                }
            }).start();

            return SINGLE_SUCCESS;
        }));

        builder.then(
            literal("tp").then(argument("loop", StringArgumentType.string()).executes(context -> {
                if(!Modules.get().get(HyperloopModule.class).isActive()) {
                    info("You need to enable the Hyperloop module!");
                    return 0;
                }
                    new Thread(() -> {
                        try {
                            String home = StringArgumentType.getString(context, "loop");

                            if (mc.player != null) {
                                String teleportResult = Utils.teleport(home, mc.player.getName().getString());

                                JsonElement element = JsonParser.parseString(teleportResult);
                                if (element.isJsonObject()) {
                                    JsonObject obj = element.getAsJsonObject();
                                    if (obj.has("bot")) {
                                        String botName = obj.get("bot").getAsString();
                                        info("Starting hyperloop towards " + home + " using " +  botName);
                                        HyperloopModule.teleporting = true;
                                        HyperloopModule.botName = botName;

                                    } else if(obj.has("error")) {
                                        String error = obj.get("error").getAsString();
                                        error("Error: " + error);
                                    }
                                    else {
                                        error("Error: No 'bot' property found in JSON.");
                                    }
                                } else {
                                    error("Error: Teleport result is not a JSON object: " + teleportResult);
                                }
                            } else {
                                error("Error: No player found to teleport.");
                            }
                        } catch (Exception ex) {
                            error("Error teleporting: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }).start();

                return SINGLE_SUCCESS;
            })
            )
        );
    }
}
