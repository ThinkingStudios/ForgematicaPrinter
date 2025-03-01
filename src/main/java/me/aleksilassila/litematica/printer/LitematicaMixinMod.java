package me.aleksilassila.litematica.printer;

import me.aleksilassila.litematica.printer.event.KeyCallbacks;
import net.minecraft.client.MinecraftClient;

public class LitematicaMixinMod {
    public static Printer printer;

    public static void onInitialize() {
        KeyCallbacks.init(MinecraftClient.getInstance());
        Printer.logger.info("{} initialized.", PrinterReference.MOD_STRING);
    }
}
