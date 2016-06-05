package com.tmtravlr.colourfulportalsmod;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class ItemStandaloneCP
extends ItemBlock
{
	public ItemStandaloneCP(Block block)
	{
		super(block);
		setMaxDamage(0);
		this.maxStackSize = 16;
		setHasSubtypes(true);
	}

//	public IIcon getIconFromDamage(int meta)
//	{
//		return ColourfulPortalsMod.getFrameBlockByShiftedMetadata(ColourfulPortalsMod.getShiftedCPMetadata(Block.getBlockFromItem(this), meta)).getIcon(0, meta);
//	}

	public int getMetadata(int meta)
	{
		return meta;
	}

	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean notSure)
	{
		String frameDisplayName = "?";
		try {
			frameDisplayName = new ItemStack(ColourfulPortalsMod.getFrameBlockByShiftedMetadata(ColourfulPortalsMod.getShiftedCPMetadata(Block.getBlockFromItem(itemStack.getItem()).getStateFromMeta(itemStack.getItemDamage()))), 1, itemStack.getItemDamage()).getDisplayName();
		}
		catch (Exception e) {
			//Do nothing if an exception is thrown
		}
		
		list.add(EnumChatFormatting.ITALIC + frameDisplayName);
	}
}