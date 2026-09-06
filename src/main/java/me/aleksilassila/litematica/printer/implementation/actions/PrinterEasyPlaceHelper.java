package me.aleksilassila.litematica.printer.implementation.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import fi.dy.masa.litematica.Litematica;
import fi.dy.masa.litematica.util.EasyPlaceProtocol;
import fi.dy.masa.litematica.util.EntityUtils;
import me.aleksilassila.litematica.printer.implementation.PrinterPlacementContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import fi.dy.masa.litematica.util.PlacementHandler;

public class PrinterEasyPlaceHelper {

  public static ActionResult doPrinterEasyPlace(MinecraftClient mc, PrinterPlacementContext ctx, Hand hand) {
    if (mc == null || mc.interactionManager == null || ctx == null)
      return ActionResult.FAIL;

    BlockPos pos = ctx.hitResult.getBlockPos();
    ClientWorld world = mc.world;
    BlockState stateClient = world.getBlockState(pos);
    ItemStack stack = ctx.getStack();

    if (stack.isEmpty())
      return ActionResult.FAIL;

    if (hand == null)
      hand = EntityUtils.getUsedHandForItem(mc.player, stack);
    if (hand == null)
      return ActionResult.FAIL;

    // Easy Place: original target position
    Vec3d hitPos = ctx.hitResult.getPos();
    Direction side = ctx.hitResult.getSide();

    // Determine protocol version
    EasyPlaceProtocol protocol = PlacementHandler.getEffectiveProtocolVersion();

    if (protocol == EasyPlaceProtocol.V3) {
      hitPos = applyPlacementProtocolV3(pos, stateClient, hitPos);
    } else if (protocol == EasyPlaceProtocol.V2) {
      hitPos = applyCarpetProtocolHitVec(pos, stateClient, hitPos);
    } else if (protocol == EasyPlaceProtocol.SLAB_ONLY) {
      hitPos = applyBlockSlabProtocol(pos, stateClient, hitPos);
    }

    // Perform block placement
    BlockHitResult hitResult = new BlockHitResult(hitPos, side, pos, false);
    mc.interactionManager.interactBlock(mc.player, world, hand, hitResult);
    mc.interactionManager.interactItem(mc.player, world, hand);

    // Double slab handling
    if (stateClient.getBlock() instanceof SlabBlock slab &&
        slab.getDefaultState().get(SlabBlock.TYPE) == SlabType.DOUBLE) {

      stateClient = world.getBlockState(pos);
      if (stateClient.getBlock() instanceof SlabBlock &&
          stateClient.get(SlabBlock.TYPE) != SlabType.DOUBLE) {
        hitResult = new BlockHitResult(hitPos, side, pos, false);
        mc.interactionManager.interactBlock(mc.player, world, hand, hitResult);
      }
    }

    return ActionResult.SUCCESS;
  }

  // ----------------------
  // Implement these helpers exactly like Easy Place:
  // ----------------------

  public static <T extends Comparable<T>> Vec3d applyPlacementProtocolV3(BlockPos pos, BlockState state,
      Vec3d hitVecIn) {
    Collection<Property<?>> props = state.getBlock().getStateManager().getProperties();

    if (props.isEmpty()) {
      return hitVecIn;
    }

    double relX = hitVecIn.x - pos.getX();
    int protocolValue = 0;
    int shiftAmount = 1;
    int propCount = 0;

    @Nullable
    DirectionProperty property = fi.dy.masa.malilib.util.BlockUtils.getFirstDirectionProperty(state);

    // DirectionProperty - allow all except: VERTICAL_DIRECTION (PointedDripstone)
    if (property != null && property != Properties.VERTICAL_DIRECTION) {
      Direction direction = state.get(property);
      protocolValue |= direction.getId() << shiftAmount;
      shiftAmount += 3;
      ++propCount;
    }

    List<Property<?>> propList = new ArrayList<>(props);
    propList.sort(Comparator.comparing(Property::getName));

    try {
      for (Property<?> p : propList) {
        if ((p instanceof DirectionProperty) == false &&
            PlacementHandler.WHITELISTED_PROPERTIES.contains(p)) {
          @SuppressWarnings("unchecked")
          Property<T> prop = (Property<T>) p;
          List<T> list = new ArrayList<>(prop.getValues());
          list.sort(Comparable::compareTo);

          int requiredBits = MathHelper.floorLog2(MathHelper.smallestEncompassingPowerOfTwo(list.size()));
          int valueIndex = list.indexOf(state.get(prop));

          if (valueIndex != -1) {
            // System.out.printf("requesting: %s = %s, index: %d\n", prop.getName(),
            // state.get(prop), valueIndex);
            protocolValue |= (valueIndex << shiftAmount);
            shiftAmount += requiredBits;
            ++propCount;
          }
        }
      }
    } catch (Exception e) {
      Litematica.logger.warn("Exception trying to request placement protocol value", e);
    }

    if (propCount > 0) {
      double x = pos.getX() + relX + 2 + protocolValue;
      // System.out.printf("request prot value 0x%08X\n", protocolValue + 2);
      return new Vec3d(x, hitVecIn.y, hitVecIn.z);
    }

    return hitVecIn;
  }

  public static Vec3d applyCarpetProtocolHitVec(BlockPos pos, BlockState state, Vec3d hitVecIn) {
    double x = hitVecIn.x;
    double y = hitVecIn.y;
    double z = hitVecIn.z;
    Block block = state.getBlock();
    Direction facing = fi.dy.masa.malilib.util.BlockUtils.getFirstPropertyFacingValue(state);
    final int propertyIncrement = 16;
    boolean hasData = false;
    int protocolValue = 0;

    if (facing != null) {
      protocolValue = facing.getId();
      hasData = true; // without this down rotation would not be detected >_>
    } else if (state.contains(Properties.AXIS)) {
      Direction.Axis axis = state.get(Properties.AXIS);
      protocolValue = axis.ordinal();
      hasData = true; // without this id 0 would not be detected >_>
    }

    if (block instanceof RepeaterBlock) {
      protocolValue += state.get(RepeaterBlock.DELAY) * propertyIncrement;
    } else if (block instanceof ComparatorBlock && state.get(ComparatorBlock.MODE) == ComparatorMode.SUBTRACT) {
      protocolValue += propertyIncrement;
    } else if (state.contains(Properties.BLOCK_HALF) && state.get(Properties.BLOCK_HALF) == BlockHalf.TOP) {
      protocolValue += propertyIncrement;
    } else if (state.contains(Properties.SLAB_TYPE) && state.get(Properties.SLAB_TYPE) == SlabType.TOP) {
      protocolValue += propertyIncrement;
    }

    y = applySlabOrStairHitVecY(y, pos, state);

    if (protocolValue != 0 || hasData) {
      x += (protocolValue * 2) + 2;
    }

    return new Vec3d(x, y, z);
  }

  private static Vec3d applyBlockSlabProtocol(BlockPos pos, BlockState state, Vec3d hitVecIn) {
    double newY = applySlabOrStairHitVecY(hitVecIn.y, pos, state);
    return newY != hitVecIn.y ? new Vec3d(hitVecIn.x, newY, hitVecIn.z) : hitVecIn;
  }

  private static double applySlabOrStairHitVecY(double origY, BlockPos pos, BlockState state) {
    double y = origY;

    if (state.contains(Properties.SLAB_TYPE)) {
      y = pos.getY();

      if (state.get(Properties.SLAB_TYPE) == SlabType.TOP) {
        y += 0.99;
      }
    } else if (state.contains(Properties.BLOCK_HALF)) {
      y = pos.getY();

      if (state.get(Properties.BLOCK_HALF) == BlockHalf.TOP) {
        y += 0.99;
      }
    }

    return y;
  }

}
