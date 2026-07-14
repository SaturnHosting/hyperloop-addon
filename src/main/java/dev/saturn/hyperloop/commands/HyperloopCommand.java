package dev.saturn.hyperloop.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.saturn.hyperloop.modules.HyperloopModule;
import dev.saturn.hyperloop.util.Utils;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class HyperloopCommand extends Command {
    public HyperloopCommand() {
        super("hyperloop", "Lists hyperloop homes (hover for details).", "hp");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
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

                    MutableComponent finalMessage = Component.literal("Hyperloops: ")
                        .withStyle(style -> style.withColor(ChatFormatting.DARK_PURPLE).withBold(true));

                    boolean first = true;

                    for (JsonElement e : homes) {
                        if (!e.isJsonObject()) continue;
                        JsonObject h = e.getAsJsonObject();

                        String home = h.get("home").getAsString();
                        String dimension = h.get("dimension").getAsString();
                        int x = h.get("x").getAsInt();
                        int z = h.get("z").getAsInt();

                        Component hover = Component.literal(
                            "Dimension: " + dimension +
                                "\nX: " + x +
                                "\nZ: " + z
                        );

                        Style hoverStyle = Style.EMPTY
                            .withColor(ChatFormatting.LIGHT_PURPLE)
                            .withBold(false)
                            .withHoverEvent(new HoverEvent.ShowText(hover));

                        if (!first) {
                            finalMessage.append(Component.literal(", "));
                        }
                        first = false;

                        finalMessage.append(Component.literal(home).setStyle(hoverStyle));
                    }

                    if (mc.player != null)
                        mc.execute(() -> mc.player.sendSystemMessage(finalMessage));
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
            literal("tp")
                .then(argument("loop", StringArgumentType.string()).suggests((context, suggestionsBuilder) -> {

                    return CompletableFuture.supplyAsync(() -> {
                        String json = Utils.fetchLoops();

                        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

                        if (root.has("error")) {
                            return suggestionsBuilder.build();
                        }

                        JsonArray homes = root.getAsJsonArray("homes");
                        if (homes == null || homes.isEmpty()) {
                            return suggestionsBuilder.build();
                        }

                        for (JsonElement e : homes) {
                            JsonObject h = e.getAsJsonObject();
                            suggestionsBuilder.suggest(h.get("home").getAsString());
                        }

                        return suggestionsBuilder.build();
                    });
                }).executes(context -> {
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
                            error("Error teleporting - invalid response. Is the API online/correct?");
                            ex.printStackTrace();
                        }
                    }).start();

                return SINGLE_SUCCESS;
            })
            )
        );

        builder.then(
            literal("dist")
                .then(argument("dim", StringArgumentType.word())
                    .then(argument("x", IntegerArgumentType.integer())
                        .then(argument("z", IntegerArgumentType.integer())
                            .executes(context -> {
                                if(!Modules.get().get(HyperloopModule.class).isActive()) {
                                    info("You need to enable the Hyperloop module!");
                                    return 0;
                                }

                                String dim = StringArgumentType.getString(context, "dim").toLowerCase();
                                int x = IntegerArgumentType.getInteger(context, "x");
                                int z = IntegerArgumentType.getInteger(context, "z");

                                new Thread(() -> {
                                    try {
                                        if (mc.player != null) {
                                            String json = Utils.fetchLoops();
                                            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                                            JsonArray homes = root.getAsJsonArray("homes");

                                            if (homes == null || homes.isEmpty()) {
                                                info("No homes found.");
                                                return;
                                            }

                                            JsonObject closestHome = null;
                                            double closestDist = Double.MAX_VALUE;

                                            for (JsonElement e : homes) {
                                                if (!e.isJsonObject()) continue;
                                                JsonObject h = e.getAsJsonObject();

                                                String hD = h.get("dimension").getAsString().toLowerCase();
                                                int hX = h.get("x").getAsInt();
                                                int hZ = h.get("z").getAsInt();

                                                double cX, cZ;

                                                switch (dim) {
                                                    case "overworld" -> {
                                                        if (hD.equals("nether")) {
                                                            cX = hX * 8.0;
                                                            cZ = hZ * 8.0;
                                                        } else if (hD.equals("overworld")) {
                                                            cX = hX;
                                                            cZ = hZ;
                                                        } else {
                                                            continue;
                                                        }
                                                    }
                                                    case "nether" -> {
                                                        if (hD.equals("overworld")) {
                                                            cX = hX / 8.0;
                                                            cZ = hZ / 8.0;
                                                        } else if (hD.equals("nether")) {
                                                            cX = hX;
                                                            cZ = hZ;
                                                        } else {
                                                            continue;
                                                        }
                                                    }
                                                    case "end" -> {
                                                        if (!hD.equals("end")) continue;
                                                        cX = hX;
                                                        cZ = hZ;
                                                    }
                                                    default -> {
                                                        continue;
                                                    }
                                                }

                                                double dX = cX - x;
                                                double dZ = cZ - z;
                                                double dist = Math.sqrt(dX * dX + dZ * dZ);

                                                if (dist < closestDist) {
                                                    closestDist = dist;
                                                    closestHome = h;
                                                }
                                            }

                                            Component message;
                                            if (closestHome == null) {
                                                message = Component.literal("No homes found in that dimension.");
                                            } else {
                                                Component homeText = Component.literal(closestHome.get("home").getAsString())
                                                    .setStyle(
                                                        Style.EMPTY
                                                            .withColor(ChatFormatting.LIGHT_PURPLE)
                                                            .withHoverEvent(
                                                                new HoverEvent.ShowText(
                                                                    Component.literal(
                                                                        "Dimension: " + closestHome.get("dimension").getAsString() +
                                                                            "\nX: " + closestHome.get("x").getAsInt() +
                                                                            "\nZ: " + closestHome.get("z").getAsInt()
                                                                    )
                                                                )
                                                            )
                                                    );

                                                message = Component.literal("Closest home is ")
                                                    .append(homeText)
                                                    .append(Component.literal(", it is " + (int)closestDist + " blocks away"));
                                            }

                                            mc.execute(() -> mc.player.sendSystemMessage(message));
                                        }
                                    } catch (Exception ex) {
                                        info("Error: " + ex.getMessage());
                                        ex.printStackTrace();
                                    }
                                }).start();

                                return SINGLE_SUCCESS;
                            })
                        )
                    )
                )
        );
    }

}
