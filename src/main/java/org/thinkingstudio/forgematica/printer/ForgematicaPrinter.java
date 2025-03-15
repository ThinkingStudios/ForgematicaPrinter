package org.thinkingstudio.forgematica.printer;

import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.PrinterReference;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import org.thinkingstudio.mafglib.util.NeoUtils;

@Mod(value = PrinterReference.MOD_ID)
public class ForgematicaPrinter {
    public ForgematicaPrinter(ModContainer modContainer) {
        if (FMLLoader.getDist().isClient()) {
            NeoUtils.getInstance().getClientModIgnoredServerOnly(modContainer);
            LitematicaMixinMod.onInitialize();
        }
    }
}
