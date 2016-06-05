package com.tmtravlr.colourfulportalsmod;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockColourfulWater
  extends BlockFluidClassic
{
  private IIcon[] textures;
  private static final boolean debug = false;
  
  public BlockColourfulWater()
  {
    super(ColourfulPortalsMod.colourfulFluid, Material.water);
    setBlockName("colourfulWater");
    ColourfulPortalsMod.colourfulFluid.setBlock(this).setUnlocalizedName(getUnlocalizedName());
  }
  
  @SideOnly(Side.CLIENT)
  public IIcon getIcon(int side, int meta)
  {
    return (side != 0) && (side != 1) ? this.textures[1] : this.textures[0];
  }
  
  @SideOnly(Side.CLIENT)
  public void registerBlockIcons(IIconRegister par1IconRegister)
  {
    this.textures = new IIcon[] { par1IconRegister.registerIcon("colourfulPortalsMod:colourful_water"), par1IconRegister.registerIcon("colourfulPortalsMod:colourful_water_flow") };
    ColourfulPortalsMod.colourfulFluid.setStillIcon(this.textures[0]);
    ColourfulPortalsMod.colourfulFluid.setFlowingIcon(this.textures[1]);
  }
  
  public void updateTick(World world, int x, int y, int z, Random rand)
  {
    super.updateTick(world, x, y, z, rand);
    if ((world.getBlockMetadata(x, y, z) == 0) && (world.getBlock(x, y, z) == this) && (ColourfulPortalsMod.isFrameBlock(world.getBlock(x, y - 1, z))) && (ColourfulPortalsMod.canCreatePortal(world, x, y, z, ColourfulPortalsMod.getCPBlockByFrameBlock(world.getBlock(x, y - 1, z)), world.getBlockMetadata(x, y - 1, z)))) {
      BlockColourfulPortal.tryToCreatePortal(world, x, y, z);
    }
  }
  
  public boolean displaceIfPossible(World world, int x, int y, int z)
  {
    if (world.getBlock(x, y, z).getMaterial() == Material.water) {
      return false;
    }
    return super.displaceIfPossible(world, x, y, z);
  }
}


/* Location:           C:\Seb-CloudGame6\Projects\mod\ColorFullPortal-Decomp\inspiration\ColourfulPortals-1.4.2_for_1.7.X-DEV.jar
 * Qualified Name:     com.tmtravlr.colourfulportalsmod.BlockColourfulWater
 * JD-Core Version:    0.7.0.1
 */