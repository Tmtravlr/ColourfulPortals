package com.tmtravlr.colourfulportalsmod;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public class BlockStandaloneCP
  extends BlockColourfulPortal
{
  @SideOnly(Side.CLIENT)
  private IIcon[] iconArray;
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
  
  public boolean shouldSideBeRendered(IBlockAccess iba, int x, int y, int z, int side)
  {
    return true;
  }
  
  public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
  {
    return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 0.8D, z + 1);
  }
  
  public void setBlockBoundsBasedOnState(IBlockAccess iba, int x, int y, int z)
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
  public void randomDisplayTick(World world, int blockX, int blockY, int blockZ, Random rand)
  {
    super.randomDisplayTick(world, blockX, blockY, blockZ, rand);
    for (int i = 0; i < 2; i++)
    {
      float x = blockX + rand.nextFloat() * 2.0F - 0.5F;
      float z = blockZ + rand.nextFloat() * 2.0F - 0.5F;
      float y = blockY + rand.nextFloat() + 0.5F;
      
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
  
  @SideOnly(Side.CLIENT)
  public void registerBlockIcons(IIconRegister iconRegister)
  {
    super.registerBlockIcons(iconRegister);
  }
  
  public IIcon getIcon(int side, int meta)
  {
    return this.blockIcon;
  }
  
  public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
  {
    return new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
  }
  
  public void onBlockAdded(World world, int x, int y, int z)
  {
    if (Block.getIdFromBlock(world.getBlock(x, y, z)) == Block.getIdFromBlock(this)) {
      if (!ColourfulPortalsMod.canCreatePortal(world, x, y, z, world.getBlock(x, y, z), world.getBlockMetadata(x, y, z)))
      {
        dropBlockAsItem(world, x, y, z, new ItemStack(this, 1, world.getBlockMetadata(x, y, z)));
        world.setBlockToAir(x, y, z);
      }
      else
      {
        ColourfulPortalsMod.addPortalToList(new ColourfulPortalsMod.ColourfulPortalLocation(x, y, z, world.provider.dimensionId, ColourfulPortalsMod.getShiftedCPMetadata(world, x, y, z)));
      }
    }
  }
  
  public void onBlockPreDestroy(World world, int x, int y, int z, int oldMeta)
  {
    if (Block.getIdFromBlock(world.getBlock(x, y, z)) == Block.getIdFromBlock(this)) {
      ColourfulPortalsMod.deletePortal(new ColourfulPortalsMod.ColourfulPortalLocation(x, y, z, world.provider.dimensionId, ColourfulPortalsMod.getShiftedCPMetadata(world, x, y, z)));
    }
  }
}