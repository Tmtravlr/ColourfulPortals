package com.tmtravlr.colourfulportalsmod;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;

public class ItemBucketColourfulWater
  extends Item
{
  private boolean isEnchanted;
  private boolean isMixed;
  private boolean isFull;
  private static final boolean debug = false;
  
  public ItemBucketColourfulWater(boolean setIsEnchanted, boolean setIsMixed, boolean setIsFull)
  {
    this.isEnchanted = setIsEnchanted;
    this.isMixed = setIsMixed;
    this.isFull = setIsFull;
    this.maxStackSize = (this.isFull ? 1 : 16);
    setCreativeTab(ColourfulPortalsMod.cpTab);
  }


  @SideOnly(Side.CLIENT)
  public boolean hasEffect(ItemStack par1ItemStack, int pass)
  {
    return this.isEnchanted;
  }
  
  public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean notSure)
  {
    if (!this.isMixed)
    {
      list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("item.bucketColourfulWaterUnmixed.info.1"));
      list.add(EnumChatFormatting.ITALIC + String.valueOf(this.isEnchanted ? ColourfulPortalsMod.xpLevelRemixingCost : ColourfulPortalsMod.xpLevelMixingCost) + StatCollector.translateToLocal("item.bucketColourfulWaterUnmixed.info.2"));
    }
    if ((this.isMixed) && (!this.isEnchanted) && (!this.isFull)) {
      list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("item.bucketColourfulWaterFirst.info.1"));
    }
    if (this.isEnchanted) {
      list.add(StatCollector.translateToLocal("item.bucketColourfulWater.info.enchant"));
    }
  }
  
  @SideOnly(Side.CLIENT)
  public void registerIcons(IIconRegister iconRegister)
  {
    if (!this.isFull) {
      this.itemIcon = iconRegister.registerIcon("colourfulportalsmod:bucketColourfulWaterFirst");
    } else if (!this.isMixed) {
      this.itemIcon = iconRegister.registerIcon("colourfulportalsmod:bucketColourfulWaterUnmixed");
    } else {
      this.itemIcon = iconRegister.registerIcon("colourfulportalsmod:bucketColourfulWater");
    }
  }
  
  public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
  {
    if ((this.isMixed) && (this.isEnchanted))
    {
      boolean flag = !this.isFull;
      MovingObjectPosition movingobjectposition = getMovingObjectPositionFromPlayer(world, player, flag);
      if (movingobjectposition == null) {
        return itemStack;
      }
      FillBucketEvent event = new FillBucketEvent(player, itemStack, world, movingobjectposition);
      if (MinecraftForge.EVENT_BUS.post(event)) {
        return itemStack;
      }
      if (event.getResult() == Event.Result.ALLOW)
      {
        if (player.capabilities.isCreativeMode) {
          return itemStack;
        }
        if (--itemStack.stackSize <= 0) {
          return event.result;
        }
        if (!player.inventory.addItemStackToInventory(event.result)) {
          player.dropPlayerItemWithRandomChoice(event.result, false);
        }
        return itemStack;
      }
      if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
      {
        int i = movingobjectposition.blockX;
        int j = movingobjectposition.blockY;
        int k = movingobjectposition.blockZ;
        if (!world.canMineBlock(player, i, j, k)) {
          return itemStack;
        }
        if (flag)
        {
          if (!player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStack)) {
            return itemStack;
          }
          Block block = world.getBlock(i, j, k);
          int l = world.getBlockMetadata(i, j, k);
          if (Block.getIdFromBlock(block) == Block.getIdFromBlock(ColourfulPortalsMod.colourfulWater))
          {
            world.setBlockToAir(i, j, k);
            if (player.capabilities.isCreativeMode) {
              return itemStack;
            }
            if (--itemStack.stackSize <= 0) {
              return new ItemStack(ColourfulPortalsMod.bucketColourfulWater);
            }
            if (!player.inventory.addItemStackToInventory(new ItemStack(ColourfulPortalsMod.bucketColourfulWater))) {
              player.dropPlayerItemWithRandomChoice(new ItemStack(ColourfulPortalsMod.bucketColourfulWater, 1, 0), false);
            }
            return itemStack;
          }
        }
        else
        {
          if (!this.isFull) {
            return new ItemStack(ColourfulPortalsMod.bucketColourfulWaterEmpty);
          }
          if (movingobjectposition.sideHit == 0) {
            j--;
          }
          if (movingobjectposition.sideHit == 1) {
            j++;
          }
          if (movingobjectposition.sideHit == 2) {
            k--;
          }
          if (movingobjectposition.sideHit == 3) {
            k++;
          }
          if (movingobjectposition.sideHit == 4) {
            i--;
          }
          if (movingobjectposition.sideHit == 5) {
            i++;
          }
          if (!player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStack)) {
            return itemStack;
          }
          if ((tryPlaceContainedColourfulLiquid(world, i, j, k)) && (!player.capabilities.isCreativeMode)) {
            return new ItemStack(ColourfulPortalsMod.bucketColourfulWaterEmpty);
          }
        }
      }
      return itemStack;
    }
    if (!this.isMixed)
    {
      int xpRequirement = ColourfulPortalsMod.xpLevelMixingCost;
      if (this.isEnchanted) {
        xpRequirement = ColourfulPortalsMod.xpLevelRemixingCost;
      }
      if (player.experienceLevel >= xpRequirement)
      {
        player.addExperienceLevel(-xpRequirement);
        
        ItemStack toReturn = new ItemStack(this.isMixed ? ColourfulPortalsMod.bucketColourfulWaterEmpty : ColourfulPortalsMod.bucketColourfulWater);
        if (--itemStack.stackSize <= 0) {
          return toReturn;
        }
        if (!player.inventory.addItemStackToInventory(toReturn)) {
          player.dropPlayerItemWithRandomChoice(toReturn, false);
        }
        return itemStack;
      }
      return itemStack;
    }
    return itemStack;
  }
  
  public boolean tryPlaceContainedColourfulLiquid(World world, int x, int y, int z)
  {
    if (!this.isFull) {
      return false;
    }
    if (((!world.isAirBlock(x, y, z)) && (world.getBlock(x, y, z).getMaterial().isSolid())) || (ColourfulPortalsMod.isCPBlock(world.getBlock(x, y, z)))) {
      return false;
    }
    boolean hasCreatedPortal = false;
    if ((ColourfulPortalsMod.isFrameBlock(world.getBlock(x, y - 1, z))) && (!ColourfulPortalsMod.isCPBlock(world.getBlock(x, y, z))) && (ColourfulPortalsMod.canCreatePortal(world, x, y, z, ColourfulPortalsMod.getCPBlockByFrameBlock(world.getBlock(x, y - 1, z)), world.getBlockMetadata(x, y - 1, z)))) {
      hasCreatedPortal = BlockColourfulPortal.tryToCreatePortal(world, x, y, z);
    }
    if (!hasCreatedPortal) {
      if ((world.provider.isHellWorld) && (this.isFull))
      {
        world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
        for (int var11 = 0; var11 < 8; var11++) {
          world.spawnParticle("largesmoke", x + Math.random(), y + Math.random(), z + Math.random(), 0.0D, 0.0D, 0.0D);
        }
      }
      else
      {
        world.setBlock(x, y, z, this.isFull ? ColourfulPortalsMod.colourfulWater : Blocks.air, 0, 3);
      }
    }
    return true;
  }
}