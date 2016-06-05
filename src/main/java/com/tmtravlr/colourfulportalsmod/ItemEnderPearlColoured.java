package com.tmtravlr.colourfulportalsmod;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemEnderPearlColoured
  extends ItemEnderPearl
{
  boolean isReflective = false;
  
  public ItemEnderPearlColoured(boolean reflective)
  {
    setUnlocalizedName(reflective ? "colourfulEnderPearlReflective" : "colourfulEnderPearl");
    this.isReflective = reflective;
    setMaxStackSize(1);
    setCreativeTab(ColourfulPortalsMod.cpTab);
  }
  
  public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean notSure)
  {
    list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("item.colourfulEnderPearl.info.1"));
    list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("item.colourfulEnderPearl.info.2"));
    list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal(this.isReflective ? "item.colourfulEnderPearl.info.3.reflective" : "item.colourfulEnderPearl.info.3"));
    list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("item.colourfulEnderPearl.info.4"));
  }
  
  public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float par8, float par9, float par10)
  {
    if ((ColourfulPortalsMod.isCPBlock(world.getBlockState(pos).getBlock())) && (!world.isRemote))
    {
      if (this.isReflective) {
        BlockColourfulPortal.tryToCreateDestination(world, pos, world.getBlockState(pos), false);
      } else {
        BlockColourfulPortal.tryToCreateDestination(world, pos, world.getBlockState(pos), true);
      }
      itemStack.stackSize -= 1;
      return true;
    }
    return super.onItemUse(itemStack, player, world, pos, side, par8, par9, par10);
  }
  
//  @SideOnly(Side.CLIENT)
//  public void registerIcons(IIconRegister iconRegister)
//  {
//    this.itemIcon = iconRegister.registerIcon("colourfulPortalsMod:enderpearlColour" + (this.isReflective ? "Reflective" : ""));
//  }
}