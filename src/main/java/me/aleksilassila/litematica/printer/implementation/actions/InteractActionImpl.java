package me.aleksilassila.litematica.printer.implementation.actions;

import me.aleksilassila.litematica.printer.Printer;
import me.aleksilassila.litematica.printer.actions.InteractAction;
import me.aleksilassila.litematica.printer.implementation.PrinterPlacementContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public class InteractActionImpl extends InteractAction {
    public InteractActionImpl(PrinterPlacementContext context) {
        super(context);
    }

    @Override
    protected void interact(MinecraftClient client, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (client.interactionManager == null)
            return;

        ItemStack requiredStack = context.getStack();
        if (requiredStack.isEmpty())
            return;

        ItemStack heldStack = player.getMainHandStack();

        // Abort if player isn’t holding the correct block
        if (heldStack.getItem() != requiredStack.getItem()) {
            return; // do nothing, printer won’t place
        }
        Printer.printDebug("held: {}, req: {}", heldStack.getItem(), requiredStack.getItem());

        // Hand is main hand because player is holding it
        hand = Hand.MAIN_HAND;

        // Call full Easy Place helper
        PrinterEasyPlaceHelper.doPrinterEasyPlace(client, context, hand);
    }
}
