package com.tmtravlr.colourfulportalsmod;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
  public boolean hasEffect(ItemStack stack)
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
  
//  @SideOnly(Side.CLIENT)
//  public void registerIcons(IIconRegister iconRegister)
//  {
//    if (!this.isFull) {
//      this.itemIcon = iconRegister.registerIcon("colourfulportalsmod:bucketColourfulWaterFirst");
//    } else if (!this.isMixed) {
//      this.itemIcon = iconRegister.registerIcon("colourfulportalsmod:bucketColourfulWaterUnmixed");
//    } else {
//      this.itemIcon = iconRegister.registerIcon("colourfulportalsmod:bucketColourfulWater");
//    }
//  }
  
  public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
  {
    if ((this.isMixed) && (this.isEnchanted))
    {
      boolean empty = !this.isFull;
      MovingObjectPosition movingobjectposition = getMovingObjectPositionFromPlayer(world, player, empty);
      if (movingobjectposition == null) {
        return itemStack;
      }
      
      if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
      {
    	  BlockPos pos = movingobjectposition.getBlockPos();
    	  
        if (!world.canMineBlockBody(player, pos)) {
          return itemStack;
        }
        if (empty)
        {
          if (!player.canPlayerEdit(pos, movingobjectposition.sideHit, itemStack)) {
            return itemStack;
          }
          IBlockState state = world.getBlockState(pos);
          Block block = state.getBlock();
          int l = block.getMetaFromState(state);
          if (block == ColourfulPortalsMod.colourfulWater)
          {
            world.setBlockToAir(pos);
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
          
          pos = pos.offset(movingobjectposition.sideHit);
          
          if (!player.canPlayerEdit(pos, movingobjectposition.sideHit, itemStack)) {
            return itemStack;
          }
          if ((tryPlaceContainedColourfulLiquid(world, pos)) && (!player.capabilities.isCreativeMode)) {
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
  
  public boolean tryPlaceContainedColourfulLiquid(World world, BlockPos pos)
  {
    if (!this.isFull) {
      return false;
    }
    Block block = world.getBlockState(pos).getBlock();
    Block frameBlock = world.getBlockState(pos.down()).getBlock();
    if (((!world.isAirBlock(pos)) && (block.getMaterial().isSolid())) || (ColourfulPortalsMod.isCPBlock(block))) {
      return false;
    }
    boolean hasCreatedPortal = false;
    if (ColourfulPortalsMod.isFrameBlock(frameBlock) && !ColourfulPortalsMod.isCPBlock(block) && ColourfulPortalsMod.canCreatePortal(world, pos, ColourfulPortalsMod.getCPBlockByFrameBlock(frameBlock).getDefaultState())) {
      hasCreatedPortal = BlockColourfulPortal.tryToCreatePortal(world, pos);
    }
    if (!hasCreatedPortal) {
      if (world.provider.doesWaterVaporize())
      {
    	  int i = pos.getX();
          int j = pos.getY();
          int k = pos.getZ();
          world.playSoundEffect((double)((float)i + 0.5F), (double)((float)j + 0.5F), (double)((float)k + 0.5F), "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

          for (int l = 0; l < 8; ++l)
          {
              world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0D, 0.0D, 0.0D, new int[0]);
          }
      }
      else
      {
    	  Material material = world.getBlockState(pos).getBlock().getMaterial();
    	  
    	  if (!world.isRemote && !material.isSolid() && !material.isLiquid())
          {
              world.destroyBlock(pos, true);
          }

          world.setBlockState(pos, ColourfulPortalsMod.colourfulWater.getDefaultState(), 3);
      }
    }
    return true;
  }
}