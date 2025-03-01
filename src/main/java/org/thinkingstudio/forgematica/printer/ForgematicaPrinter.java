package org.thinkingstudio.forgematica.printer;

import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.PrinterReference;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;

@Mod(value = PrinterReference.MOD_ID, dist = Dist.CLIENT)
public class ForgematicaPrinter {
    public ForgematicaPrinter() {
        if (FMLLoader.getDist().isClient()) {
            LitematicaMixinMod.onInitialize();
        }
    }
}
