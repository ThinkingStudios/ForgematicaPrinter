package me.aleksilassila.litematica.printer.config;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import me.aleksilassila.litematica.printer.PrinterReference;

import java.util.List;

public class Configs {
    private static final String GENERIC_KEY = PrinterReference.MOD_KEY + ".config.generic";

    // Configs settings
    public static final ConfigInteger PRINTING_INTERVAL = new ConfigInteger("printingInterval", 12, 1, 40, "Printing interval. Lower values mean faster printing speed.\nIf the printer creates \"ghost blocks\" or blocks are facing the wrong way, raise this value.");
    public static final ConfigDouble PRINTING_RANGE = new ConfigDouble("printingRange", 5, 2.5, 5, "Printing block place range\nLower values are recommended for servers.");
    public static final ConfigBoolean PRINT_MODE = new ConfigBoolean("printingMode", false, "Autobuild / print loaded selection.\nBe aware that some servers and anticheat plugins do not allow printing.");
    public static final ConfigBoolean PRINT_DEBUG = new ConfigBoolean("printingDebug", false, "Enables Debug logging for printing.");
    public static final ConfigBoolean REPLACE_FLUIDS_SOURCE_BLOCKS = new ConfigBoolean("replaceFluidSourceBlocks", true, "Whether or not fluid source blocks should be replaced by the printer.");
    public static final ConfigBoolean STRIP_LOGS = new ConfigBoolean("stripLogs", true, "Whether or not the printer should use normal logs if stripped\\nversions are not available and then strip them with an axe.");
    public static final ConfigBoolean INTERACT_BLOCKS = new ConfigBoolean("interactBlocks", true, "Whether or not the printer should set block states.");

    public static ImmutableList<IConfigBase> getConfigList() {
        List<IConfigBase> list = new java.util.ArrayList<>(fi.dy.masa.litematica.config.Configs.Generic.OPTIONS);
        list.add(PRINT_MODE);
        list.add(PRINT_DEBUG);
        list.add(PRINTING_INTERVAL);
        list.add(PRINTING_RANGE);
        list.add(REPLACE_FLUIDS_SOURCE_BLOCKS);
        list.add(STRIP_LOGS);
        list.add(INTERACT_BLOCKS);

        return ImmutableList.copyOf(list);
    }
}
