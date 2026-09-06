package me.aleksilassila.litematica.printer.implementation;

import me.aleksilassila.litematica.printer.BlockHelper;
import net.minecraft.block.AbstractButtonBlock;

public class BlockHelperImpl extends BlockHelper {
    static {
        interactiveBlocks.add(AbstractButtonBlock.class);
    }
}
