package com.tmtravlr.colourfulportalsmod;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockStandaloneCP
  extends BlockColourfulPortal
{
  @SideOnly(Side.CLIENT)
  //private IIcon[] iconArray;
  private static final boolean debug = false;
  public static final double HEIGHT = 0.8D;
  
  public BlockStandaloneCP(String texture, Material material)
  {
    super(texture, material);
    setCreativeTab(ColourfulPortalsMod.cpTab);
  }
  
  public void onNeighborBlockChange(World world, int x, int y, int z, Block other) {}
  
  public int getRenderType()
  {
    return ColourfulPortalsMod.colourfulPortalRenderId;
  }
  
  public int quantityDropped(Random rand)
  {
    return 1;
  }
  
  public boolean shouldSideBeRendered(IBlockAccess iba, BlockPos pos, EnumFacing side)
  {
    return true;
  }
  
  @Override
  public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
  {
    return new AxisAlignedBB(pos, pos.add(1, 0.8D, 1));
  }
  
  public void setBlockBoundsBasedOnState(IBlockAccess iba, BlockPos pos)
  {
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.8F, 1.0F);
  }
  
  public boolean canRenderInPass(int pass)
  {
    ClientProxy.renderPass = pass;
    
    return true;
  }
  
  public int getRenderBlockPass()
  {
    return 1;
  }
  
  @SideOnly(Side.CLIENT)
  public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand)
  {
    super.randomDisplayTick(world, pos, state, rand);
    for (int i = 0; i < 2; i++)
    {
      float x = pos.getX() + rand.nextFloat() * 2.0F - 0.5F;
      float z = pos.getY() + rand.nextFloat() * 2.0F - 0.5F;
      float y = pos.getZ() + rand.nextFloat() + 0.5F;
      
      float xVel = rand.nextFloat() * 0.2F;
      float zVel = rand.nextFloat() * 0.2F;
      float yVel = rand.nextFloat() * 0.2F;
      if (rand.nextBoolean()) {
        xVel = -xVel;
      }
      if (rand.nextBoolean()) {
        zVel = -zVel;
      }
      EntityFX entityfx = new EntityCPortalFX(world, x, y, z, xVel, yVel, zVel);
      Minecraft.getMinecraft().effectRenderer.addEffect(entityfx);
    }
  }
  
  @SideOnly(Side.CLIENT)
  public void getSubBlocks(Item item, CreativeTabs creativeTabs, List list)
  {
    for (int j = 0; j < 16; j++) {
      list.add(new ItemStack(item, 1, j));
    }
  }
  
  public int damageDropped(int meta)
  {
    return meta;
  }
  
//  @SideOnly(Side.CLIENT)
//  public void registerBlockIcons(IIconRegister iconRegister)
//  {
//    super.registerBlockIcons(iconRegister);
//  }
  
//  public IIcon getIcon(int side, int meta)
//  {
//    return this.blockIcon;
//  }
  
  public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos)
  {
    return new ItemStack(this, 1, ColourfulPortalsMod.getMeta(world, pos));
  }
  
  public void onBlockAdded(World world, BlockPos pos, IBlockState state)
  {
    if (state.getBlock() == this) {
      if (!ColourfulPortalsMod.canCreatePortal(world, pos, state))
      {
        dropBlockAsItem(world, pos, state, ColourfulPortalsMod.getMeta(state));
        world.setBlockToAir(pos);
      }
      else
      {
        ColourfulPortalsMod.addPortalToList(new ColourfulPortalsMod.ColourfulPortalLocation(pos, world.provider.getDimensionId(), ColourfulPortalsMod.getShiftedCPMetadata(world, pos)));
      }
    }
  }
  
  public void breakBlock(World world, BlockPos pos, IBlockState state)
  {
    if (state.getBlock() == this) {
      ColourfulPortalsMod.deletePortal(new ColourfulPortalsMod.ColourfulPortalLocation(pos, world.provider.getDimensionId(), ColourfulPortalsMod.getShiftedCPMetadata(world, pos)));
    }
  }
}