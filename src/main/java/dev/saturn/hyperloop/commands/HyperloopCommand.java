package dev.saturn.hyperloop.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.saturn.hyperloop.util.Utils;
import meteordevelopment.meteorclient.commands.Command;
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
        builder.then(literal("list").executes(context -> {
            new Thread(() -> {
                try {
                    String json = Utils.fetchLoops();

                    JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                    JsonArray homes = root.getAsJsonArray("homes");

                    if (homes == null || homes.isEmpty()) {
                        info("No homes found.");
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
                        info("can't send message to player, god knows why...");

                } catch (Exception ex) {
                    info("error fetching: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }).start();

            return SINGLE_SUCCESS;
        }));
    }
}
