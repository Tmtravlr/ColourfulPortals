package com.tmtravlr.colourfulportalsmod;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import com.mojang.realmsclient.util.Pair;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;

public class BlockColourfulPortal
extends BlockBreakable
{
	private static final boolean debug = false;
	private static final int PLAYER_MIN_DELAY = 0;
	private static final int PLAYER_MAX_DELAY = 10;
	private static LinkedList<Entity> entitiesTeleported = new LinkedList();
	public static BlockColourfulPortal instance = new BlockColourfulPortal("", Material.portal);
	private static int goCrazyX = -1;
	private static int goCrazyY = -1;
	private static int goCrazyZ = -1;
	private static HashMap<Entity, Entity> toStack = new HashMap<Entity, Entity>();
	private static int stackDelay = 0;

	public BlockColourfulPortal(String texture, Material material)
	{
		super(texture, material, false);
		
		setTickRandomly(true);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		this.blockIcon = iconRegister.registerIcon("colourfulPortalsMod:portal_colour");
	}

	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
	{
		super.updateTick(par1World, par2, par3, par4, par5Random);
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
	{
		return null;
	}

	public void setBlockBoundsBasedOnState(IBlockAccess iba, int x, int y, int z)
	{
		float xDiff = 0.5F;
		float zDiff = 0.5F;
		float yDiff = 0.5F;
		if ((!ColourfulPortalsMod.isPortalOrFrameBlock(iba, x - 1, y, z)) || (!ColourfulPortalsMod.isPortalOrFrameBlock(iba, x + 1, y, z))) {
			xDiff = 0.125F;
		} else if ((!ColourfulPortalsMod.isPortalOrFrameBlock(iba, x, y, z - 1)) || (!ColourfulPortalsMod.isPortalOrFrameBlock(iba, x, y, z + 1))) {
			zDiff = 0.125F;
		} else if ((!ColourfulPortalsMod.isPortalOrFrameBlock(iba, x, y - 1, z)) || (!ColourfulPortalsMod.isPortalOrFrameBlock(iba, x, y + 1, z))) {
			yDiff = 0.125F;
		}
		setBlockBounds(0.5F - xDiff, 0.5F - yDiff, 0.5F - zDiff, 0.5F + xDiff, 0.5F + yDiff, 0.5F + zDiff);
	}

	public boolean isOpaqueCube()
	{
		return false;
	}

	public boolean renderAsNormalBlock()
	{
		return false;
	}

	public void onBlockPreDestroy(World world, int x, int y, int z, int oldMeta)
	{
		ColourfulPortalsMod.deletePortal(new ColourfulPortalsMod.ColourfulPortalLocation(x, y, z, world.provider.dimensionId, ColourfulPortalsMod.getShiftedCPMetadata(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z))));
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, Block other)
	{
		boolean xDir = true;
		boolean yDir = true;
		boolean zDir = true;
		int i = 0;
		int maxSize = ColourfulPortalsMod.maxPortalSizeCheck * ColourfulPortalsMod.maxPortalSizeCheck + 1;
		for (i = 0; (i < maxSize) && (ColourfulPortalsMod.isCPBlock(world.getBlock(x + i, y, z))); i++) {}
		if (!ColourfulPortalsMod.isFrameBlock(world.getBlock(x + i, y, z)))
		{
			zDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && (ColourfulPortalsMod.isCPBlock(world.getBlock(x - i, y, z))); i++) {}
		if (!ColourfulPortalsMod.isFrameBlock(world.getBlock(x - i, y, z)))
		{
			zDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && (ColourfulPortalsMod.isCPBlock(world.getBlock(x, y + i, z))); i++) {}
		if (!ColourfulPortalsMod.isFrameBlock(world.getBlock(x, y + i, z)))
		{
			zDir = false;
			xDir = false;
		}
		for (i = 0; (i < maxSize) && (ColourfulPortalsMod.isCPBlock(world.getBlock(x, y - i, z))); i++) {}
		if (!ColourfulPortalsMod.isFrameBlock(world.getBlock(x, y - i, z)))
		{
			zDir = false;
			xDir = false;
		}
		for (i = 0; (i < maxSize) && (ColourfulPortalsMod.isCPBlock(world.getBlock(x, y, z + i))); i++) {}
		if (!ColourfulPortalsMod.isFrameBlock(world.getBlock(x, y, z + i)))
		{
			xDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && (ColourfulPortalsMod.isCPBlock(world.getBlock(x, y, z - i))); i++) {}
		if (!ColourfulPortalsMod.isFrameBlock(world.getBlock(x, y, z - i)))
		{
			xDir = false;
			yDir = false;
		}
		if ((!xDir) && (!yDir) && (!zDir))
		{
			ColourfulPortalsMod.CPLSet visited = new ColourfulPortalsMod.CPLSet();
			Stack<ColourfulPortalsMod.ColourfulPortalLocation> toVisit = new Stack();

			toVisit.push(new ColourfulPortalsMod.ColourfulPortalLocation(x, y, z, world.provider.dimensionId, ColourfulPortalsMod.getShiftedCPMetadata(world, x, y, z)));

			visited.add(toVisit.peek());
			while (!toVisit.empty())
			{
				ColourfulPortalsMod.ColourfulPortalLocation current = (ColourfulPortalsMod.ColourfulPortalLocation)toVisit.pop();

				int[][] dispArray = { { 0, 0, -1 }, { 0, 0, 1 }, { 0, -1, 0 }, { 0, 1, 0 }, { -1, 0, 0 }, { 1, 0, 0 } };
				for (int[] disps : dispArray) {
					if (ColourfulPortalsMod.isFramedCPBlock(world.getBlock(current.xPos + disps[0], current.yPos + disps[1], current.zPos + disps[2])))
					{
						ColourfulPortalsMod.ColourfulPortalLocation temp = new ColourfulPortalsMod.ColourfulPortalLocation(current.xPos + disps[0], current.yPos + disps[1], current.zPos + disps[2], world.provider.dimensionId, ColourfulPortalsMod.getShiftedCPMetadata(world, current.xPos + disps[0], current.yPos + disps[1], current.zPos + disps[2]));
						if (!visited.contains(temp))
						{
							toVisit.push(temp);
							visited.add(temp);
						}
					}
				}
			}
			for (ColourfulPortalsMod.ColourfulPortalLocation toDelete : visited) {
				world.setBlockToAir(toDelete.xPos, toDelete.yPos, toDelete.zPos);
			}
		}
	}

	public boolean shouldSideBeRendered(IBlockAccess iba, int x, int y, int z, int side)
	{
		if (((side == 0) && (this.minY > 0.0D)) || ((side == 1) && (this.maxY < 1.0D)) || ((side == 2) && (this.minZ > 0.0D)) || ((side == 3) && (this.maxZ < 1.0D)) || ((side == 4) && (this.minX > 0.0D)) || ((side == 5) && (this.maxX < 1.0D))) {
			return true;
		}
		if (ColourfulPortalsMod.isPortalOrFrameBlock(iba, x, y, z)) {
			return false;
		}
		return true;
	}

	public int quantityDropped(Random par1Random)
	{
		return 0;
	}

	public int getRenderBlockPass()
	{
		return 1;
	}

	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int blockX, int blockY, int blockZ, Random rand)
	{
		int max = 2;
		boolean crazy = false;
		if ((goCrazyX == blockX) && (goCrazyY == blockY) && (goCrazyZ == blockZ))
		{
			max = 50;
			crazy = true;

			goCrazyX = BlockColourfulPortal.goCrazyZ = BlockColourfulPortal.goCrazyY = -1;
		}
		for (int i = 0; i < max; i++)
		{
			float x = blockX + rand.nextFloat();
			float y = blockY + rand.nextFloat();
			float z = blockZ + rand.nextFloat();
			float xVel = (rand.nextFloat() - 0.5F) * 0.5F;
			float yVel = (rand.nextFloat() - 0.5F) * 0.5F;
			float zVel = (rand.nextFloat() - 0.5F) * 0.5F;
			int dispX = rand.nextInt(2) * 2 - 1;
			int dispZ = rand.nextInt(2) * 2 - 1;

			x = blockX + 0.5F + 0.25F * dispX;
			xVel = rand.nextFloat() * 2.0F * dispX;

			z = blockZ + 0.5F + 0.25F * dispZ;
			zVel = rand.nextFloat() * 2.0F * dispZ;


			EntityFX entityfx = new EntityCPortalFX(world, x, y, z, xVel, yVel, zVel, crazy);
			Minecraft.getMinecraft().effectRenderer.addEffect(entityfx);
		}
	}

	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
	{
		return null;
	}

	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		if ((entity instanceof EntityLivingBase))
		{
			EntityLivingBase livingEntity = (EntityLivingBase)entity;
			if (livingEntity.getActivePotionEffect(Potion.confusion) == null) {}
			livingEntity.addPotionEffect(new PotionEffect(Potion.confusion.id, 80, 0, true));
		}
		
		if (!world.isRemote)
		{
			//Check for colourful ender pearls
			if ((entity instanceof EntityItem))
			{
				ItemStack item = ((EntityItem)entity).getEntityItem();
				if (Item.getIdFromItem(item.getItem()) == Item.getIdFromItem(ColourfulPortalsMod.enderPearlColoured))
				{
					tryToCreateDestination(world, x, y, z, true);

					entity.setDead();
				}
				else if (Item.getIdFromItem(item.getItem()) == Item.getIdFromItem(ColourfulPortalsMod.enderPearlColouredReflective))
				{
					tryToCreateDestination(world, x, y, z, false);

					entity.setDead();
				}
			}

			//Find the bottom entity of the stack
			
			while(entity.ridingEntity != null) {
				entity = entity.ridingEntity;
			}
			
			//Go through to the top entity of the stack
			
			boolean doTeleport = true;
			Entity nextEntity = entity;
			
			//Go through the stack and make sure all entities can teleport.
			
			do {
				entity = nextEntity;
				
				if(!entitySatisfiesTeleportConditions(world, x, y, z, entity)) {
					
					entity.getEntityData().setInteger("ColourfulPortalDelay", 10);
					if (!(entity instanceof EntityPlayer))
					{
						entity.getEntityData().setBoolean("InColourfulPortal", true);
					}
					
					doTeleport = false;
				}
				
				if (!entitiesTeleported.contains(entity))
				{
					entitiesTeleported.add(entity);
				}
				
				nextEntity = entity.riddenByEntity;
			}
			while(nextEntity != null);
			
			if (doTeleport && entitySatisfiesTeleportConditions(world, x, y, z, entity))
			{
				
				teleportColourfully(world, x, y, z, entity);

			}
			else
			{
				entity.getEntityData().setInteger("ColourfulPortalDelay", 10);
				if (!(entity instanceof EntityPlayer))
				{
					entity.getEntityData().setBoolean("InColourfulPortal", true);
				}
			}
			if (!entitiesTeleported.contains(entity))
			{
				entitiesTeleported.add(entity);
			}
			
			
		}
		
	}

	public static boolean tryToCreatePortal(World par1World, int par2, int par3, int par4)
	{
		return tryToCreatePortal(par1World, par2, par3, par4, true);
	}

	public static boolean tryToCreatePortal(World world, int x, int y, int z, boolean addLocationToList)
	{
		if (!world.isRemote)
		{
			int maxSize = ColourfulPortalsMod.maxPortalSizeCheck * ColourfulPortalsMod.maxPortalSizeCheck - 1;
			if ((world.getBlock(x, y + 1, z) != Blocks.air) && (world.getBlock(z, y + 1, z) != ColourfulPortalsMod.colourfulWater)) {
				return false;
			}
			if (!ColourfulPortalsMod.isFrameBlock(world.getBlock(x, y - 1, z))) {
				return false;
			}
			Block frameBlock = world.getBlock(x, y - 1, z);
			int frameMeta = world.getBlockMetadata(x, y - 1, z);



			boolean[] dirs = { true, true, true };
			int i = 0;
			int thisId = 0;
			for (i = 0; (i < maxSize + 1) && ((world.getBlock(x + i, y, z) == Blocks.air) || (world.getBlock(x + i, y, z) == ColourfulPortalsMod.colourfulWater)); i++) {}
			if ((world.getBlock(x + i, y, z) != frameBlock) || (world.getBlockMetadata(x + i, y, z) != frameMeta))
			{
				dirs[2] = false;
				dirs[1] = false;
			}
			for (i = 0; (i < maxSize + 1) && ((world.getBlock(x - i, y, z) == Blocks.air) || (world.getBlock(x - i, y, z) == ColourfulPortalsMod.colourfulWater)); i++) {}
			if ((world.getBlock(x - i, y, z) != frameBlock) || (world.getBlockMetadata(x - i, y, z) != frameMeta))
			{
				dirs[2] = false;
				dirs[1] = false;
			}
			for (i = 0; (i < maxSize + 1) && ((world.getBlock(x, y + i, z) == Blocks.air) || (world.getBlock(x, y + i, z) == ColourfulPortalsMod.colourfulWater)); i++) {}
			if ((world.getBlock(x, y + i, z) != frameBlock) || (world.getBlockMetadata(x, y + i, z) != frameMeta))
			{
				dirs[2] = false;
				dirs[0] = false;
			}
			for (i = 0; (i < maxSize + 1) && ((world.getBlock(x, y - i, z) == Blocks.air) || (world.getBlock(x, y - i, z) == ColourfulPortalsMod.colourfulWater)); i++) {}
			if ((world.getBlock(x, y - i, z) != frameBlock) || (world.getBlockMetadata(x, y - i, z) != frameMeta))
			{
				dirs[2] = false;
				dirs[0] = false;
			}
			for (i = 0; (i < maxSize + 1) && ((world.getBlock(x, y, z + i) == Blocks.air) || (world.getBlock(x, y, z + i) == ColourfulPortalsMod.colourfulWater)); i++) {}
			if ((world.getBlock(x, y, z + i) != frameBlock) || (world.getBlockMetadata(x, y, z + i) != frameMeta))
			{
				dirs[0] = false;
				dirs[1] = false;
			}
			for (i = 0; (i < maxSize + 1) && ((world.getBlock(x, y, z - i) == Blocks.air) || (world.getBlock(x, y, z - i) == ColourfulPortalsMod.colourfulWater)); i++) {}
			if ((world.getBlock(x, y, z - i) != frameBlock) || (world.getBlockMetadata(x, y, z - i) != frameMeta))
			{
				dirs[0] = false;
				dirs[1] = false;
			}
			for (int d = 0; d < 3; d++) {
				if (dirs[d])
				{
					boolean xLook = false;
					boolean yLook = false;
					boolean zLook = false;
					if (d == 0) {
						xLook = true;
					} else if (d == 1) {
						yLook = true;
					} else {
						zLook = true;
					}
					ColourfulPortalsMod.CPLSet visited = new ColourfulPortalsMod.CPLSet();
					Stack<ColourfulPortalsMod.ColourfulPortalLocation> toVisit = new Stack();

					toVisit.push(new ColourfulPortalsMod.ColourfulPortalLocation(x, y, z, world.provider.dimensionId, ColourfulPortalsMod.getShiftedCPMetadata(world, x, y, z)));

					visited.add(toVisit.peek());

					int maxSizeTotal = (ColourfulPortalsMod.maxPortalSizeCheck * ColourfulPortalsMod.maxPortalSizeCheck - 1) * (ColourfulPortalsMod.maxPortalSizeCheck * ColourfulPortalsMod.maxPortalSizeCheck - 1);
					for (int j = 0; (j < maxSizeTotal) && (!toVisit.empty()) && (dirs[d]); j++)
					{
						ColourfulPortalsMod.ColourfulPortalLocation current = (ColourfulPortalsMod.ColourfulPortalLocation)toVisit.pop();
						if ((dirs[0]) || (dirs[2]))
						{
							Block nextBlock = world.getBlock(current.xPos, current.yPos + 1, current.zPos);
							int nextMeta = world.getBlockMetadata(current.xPos, current.yPos + 1, current.zPos);
							if (((nextBlock != frameBlock) && (nextMeta != frameMeta) && (nextBlock != Blocks.air) && (nextBlock != ColourfulPortalsMod.colourfulWater)) || (Math.abs(current.xPos - x) > ColourfulPortalsMod.maxPortalSizeCheck) || (Math.abs(current.yPos + 1 - y) > ColourfulPortalsMod.maxPortalSizeCheck) || (Math.abs(current.zPos - z) > ColourfulPortalsMod.maxPortalSizeCheck)) {
								if (xLook) {
									dirs[0] = false;
								} else if (zLook) {
									dirs[2] = false;
								}
							}
							nextBlock = world.getBlock(current.xPos, current.yPos - 1, current.zPos);
							nextMeta = world.getBlockMetadata(current.xPos, current.yPos - 1, current.zPos);
							if (((nextBlock != frameBlock) && (nextMeta != frameMeta) && (nextBlock != Blocks.air) && (nextBlock != ColourfulPortalsMod.colourfulWater)) || (Math.abs(current.xPos - x) > ColourfulPortalsMod.maxPortalSizeCheck) || (Math.abs(current.yPos - 1 - y) > ColourfulPortalsMod.maxPortalSizeCheck) || (Math.abs(current.zPos - z) > ColourfulPortalsMod.maxPortalSizeCheck)) {
								if (xLook) {
									dirs[0] = false;
								} else if (zLook) {
									dirs[2] = false;
								}
							}
						}
						if ((dirs[0]) || (dirs[1]))
						{
							Block nextBlock = world.getBlock(current.xPos, current.yPos, current.zPos + 1);
							int nextMeta = world.getBlockMetadata(current.xPos, current.yPos, current.zPos + 1);
							if (((nextBlock != frameBlock) && (nextMeta != frameMeta) && (nextBlock != Blocks.air) && (nextBlock != ColourfulPortalsMod.colourfulWater)) || (Math.abs(current.xPos - x) > ColourfulPortalsMod.maxPortalSizeCheck) || (Math.abs(current.yPos - y) > ColourfulPortalsMod.maxPortalSizeCheck) || (Math.abs(current.zPos + 1 - z) > ColourfulPortalsMod.maxPortalSizeCheck)) {
								if (xLook) {
									dirs[0] = false;
								} else if (yLook) {
									dirs[1] = false;
								}
							}
							nextBlock = world.getBlock(current.xPos, current.yPos, current.zPos - 1);
							nextMeta = world.getBlockMetadata(current.xPos, current.yPos, current.zPos - 1);
							if (((nextBlock != frameBlock) && (nextMeta != frameMeta) && (nextBlock != Blocks.air) && (nextBlock != ColourfulPortalsMod.colourfulWater)) || (Math.abs(current.xPos - x) > ColourfulPortalsMod.maxPortalSizeCheck) || (Math.abs(current.yPos - y) > ColourfulPortalsMod.maxPortalSizeCheck) || (Math.abs(current.zPos - 1 - z) > ColourfulPortalsMod.maxPortalSizeCheck)) {
								if (xLook) {
									dirs[0] = false;
								} else if (yLook) {
									dirs[1] = false;
								}
							}
						}
						if ((dirs[1]) || (dirs[2]))
						{
							Block nextBlock = world.getBlock(current.xPos + 1, current.yPos, current.zPos);
							int nextMeta = world.getBlockMetadata(current.xPos + 1, current.yPos, current.zPos);
							if (((nextBlock != frameBlock) && (nextMeta != frameMeta) && (nextBlock != Blocks.air) && (nextBlock != ColourfulPortalsMod.colourfulWater)) || (Math.abs(current.xPos + 1 - x) > ColourfulPortalsMod.maxPortalSizeCheck) || (Math.abs(current.yPos - y) > ColourfulPortalsMod.maxPortalSizeCheck) || (Math.abs(current.zPos - z) > ColourfulPortalsMod.maxPortalSizeCheck)) {
								if (yLook) {
									dirs[1] = false;
								} else if (zLook) {
									dirs[2] = false;
								}
							}
							nextBlock = world.getBlock(current.xPos - 1, current.yPos, current.zPos);
							nextMeta = world.getBlockMetadata(current.xPos - 1, current.yPos, current.zPos);
							if (((nextBlock != frameBlock) && (nextMeta != frameMeta) && (nextBlock != Blocks.air) && (nextBlock != ColourfulPortalsMod.colourfulWater)) || (Math.abs(current.xPos - 1 - x) > ColourfulPortalsMod.maxPortalSizeCheck) || (Math.abs(current.yPos - y) > ColourfulPortalsMod.maxPortalSizeCheck) || (Math.abs(current.zPos - z) > ColourfulPortalsMod.maxPortalSizeCheck)) {
								if (yLook) {
									dirs[1] = false;
								} else if (zLook) {
									dirs[2] = false;
								}
							}
						}
						if ((dirs[d]) && (Math.abs(x - current.xPos) < ColourfulPortalsMod.maxPortalSizeCheck) && (y <= 256) && (y > 0) && (Math.abs(z - current.zPos) < ColourfulPortalsMod.maxPortalSizeCheck))
						{
							if ((zLook) || (xLook))
							{
								if ((world.getBlock(current.xPos, current.yPos + 1, current.zPos) == Blocks.air) || (world.getBlock(current.xPos, current.yPos + 1, current.zPos) == ColourfulPortalsMod.colourfulWater))
								{
									ColourfulPortalsMod.ColourfulPortalLocation temp = new ColourfulPortalsMod.ColourfulPortalLocation(current.xPos, current.yPos + 1, current.zPos, world.provider.dimensionId, ColourfulPortalsMod.getShiftedCPMetadataByFrameBlock(frameBlock, frameMeta));
									if (!visited.contains(temp))
									{
										toVisit.push(temp);
										visited.add(temp);
									}
								}
								if ((world.getBlock(current.xPos, current.yPos - 1, current.zPos) == Blocks.air) || (world.getBlock(current.xPos, current.yPos - 1, current.zPos) == ColourfulPortalsMod.colourfulWater))
								{
									ColourfulPortalsMod.ColourfulPortalLocation temp = new ColourfulPortalsMod.ColourfulPortalLocation(current.xPos, current.yPos - 1, current.zPos, world.provider.dimensionId, ColourfulPortalsMod.getShiftedCPMetadataByFrameBlock(frameBlock, frameMeta));
									if (!visited.contains(temp))
									{
										toVisit.push(temp);
										visited.add(temp);
									}
								}
							}
							if ((zLook) || (yLook))
							{
								if ((world.getBlock(current.xPos + 1, current.yPos, current.zPos) == Blocks.air) || (world.getBlock(current.xPos + 1, current.yPos, current.zPos) == ColourfulPortalsMod.colourfulWater))
								{
									ColourfulPortalsMod.ColourfulPortalLocation temp = new ColourfulPortalsMod.ColourfulPortalLocation(current.xPos + 1, current.yPos, current.zPos, world.provider.dimensionId, ColourfulPortalsMod.getShiftedCPMetadataByFrameBlock(frameBlock, frameMeta));
									if (!visited.contains(temp))
									{
										toVisit.push(temp);
										visited.add(temp);
									}
								}
								if ((world.getBlock(current.xPos - 1, current.yPos, current.zPos) == Blocks.air) || (world.getBlock(current.xPos - 1, current.yPos, current.zPos) == ColourfulPortalsMod.colourfulWater))
								{
									ColourfulPortalsMod.ColourfulPortalLocation temp = new ColourfulPortalsMod.ColourfulPortalLocation(current.xPos - 1, current.yPos, current.zPos, world.provider.dimensionId, ColourfulPortalsMod.getShiftedCPMetadataByFrameBlock(frameBlock, frameMeta));
									if (!visited.contains(temp))
									{
										toVisit.push(temp);
										visited.add(temp);
									}
								}
							}
							if ((yLook) || (xLook))
							{
								if ((world.getBlock(current.xPos, current.yPos, current.zPos + 1) == Blocks.air) || (world.getBlock(current.xPos, current.yPos, current.zPos + 1) == ColourfulPortalsMod.colourfulWater))
								{
									ColourfulPortalsMod.ColourfulPortalLocation temp = new ColourfulPortalsMod.ColourfulPortalLocation(current.xPos, current.yPos, current.zPos + 1, world.provider.dimensionId, ColourfulPortalsMod.getShiftedCPMetadataByFrameBlock(frameBlock, frameMeta));
									if (!visited.contains(temp))
									{
										toVisit.push(temp);
										visited.add(temp);
									}
								}
								if ((world.getBlock(current.xPos, current.yPos, current.zPos - 1) == Blocks.air) || (world.getBlock(current.xPos, current.yPos, current.zPos - 1) == ColourfulPortalsMod.colourfulWater))
								{
									ColourfulPortalsMod.ColourfulPortalLocation temp = new ColourfulPortalsMod.ColourfulPortalLocation(current.xPos, current.yPos, current.zPos - 1, world.provider.dimensionId, ColourfulPortalsMod.getShiftedCPMetadataByFrameBlock(frameBlock, frameMeta));
									if (!visited.contains(temp))
									{
										toVisit.push(temp);
										visited.add(temp);
									}
								}
							}
						}
					}
					if (dirs[d])
					{
						for (ColourfulPortalsMod.ColourfulPortalLocation cpl : visited) {
							if (((dirs[0]) && (cpl.xPos == x)) || ((dirs[1]) && (cpl.yPos == y)) || ((dirs[2]) && (cpl.zPos == z))) {
								ColourfulPortalsMod.setFramedCPBlock(world, cpl.xPos, cpl.yPos, cpl.zPos, frameBlock, frameMeta, 2);
							}
						}
						int shiftedMeta = ColourfulPortalsMod.getShiftedCPMetadata(world, x, y, z);
						boolean creationSuccess = true;
						if (addLocationToList) {
							creationSuccess = ColourfulPortalsMod.addPortalToList(new ColourfulPortalsMod.ColourfulPortalLocation(x, y, z, world.provider.dimensionId, shiftedMeta));
						}
						return creationSuccess;
					}
				}
			}
		}
		return false;
	}

	public static void tryToCreateDestination(World world, int x, int y, int z, boolean sameDim)
	{
		boolean creationSuccess = false;
		if (!ColourfulPortalsMod.tooManyPortals(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z)))
		{
			ColourfulPortalsMod.ColourfulPortalLocation destination = createDestination(sameDim, world.provider.dimensionId, ColourfulPortalsMod.getShiftedCPMetadata(world, x, y, z));
			if (destination == null) {
				return;
			}
			creationSuccess = ColourfulPortalsMod.addPortalToList(destination);
		}
		float soundPitch = 1.8F;
		if (sameDim) {
			soundPitch = 1.5F;
		}
		world.playSoundEffect(x, y, z, "colourfulportalsmod:teleport", 1.0F, soundPitch);
		goCrazyX = x;
		goCrazyZ = z;
		goCrazyY = y;
	}

	private static ColourfulPortalsMod.ColourfulPortalLocation createDestination(boolean isSameDim, int oldDim, int meta)
	{
		int unshiftedMeta = ColourfulPortalsMod.unshiftCPMetadata(meta);
		Block portalBlock = ColourfulPortalsMod.getCPBlockByShiftedMetadata(meta);
		Block frameBlock = ColourfulPortalsMod.getFrameBlockByShiftedMetadata(meta);

		byte var2 = 16;
		double var3 = -1.0D;

		int maxDistance = ColourfulPortalsMod.maxPortalGenerationDistance;
		if (maxDistance < 0) {
			maxDistance = -maxDistance;
		}
		if (maxDistance > 29999872) {
			maxDistance = 29999872;
		}
		Random rand = new Random();
		int var5 = rand.nextInt(maxDistance * 2);
		var5 -= maxDistance;
		int var6 = 0;
		int var7 = rand.nextInt(maxDistance * 2);
		var7 -= maxDistance;
		int dimension;
		WorldServer worldServer;    
		if (isSameDim)
		{
			worldServer = MinecraftServer.getServer().worldServerForDimension(oldDim);
			dimension = oldDim;
		}
		else
		{
			WorldServer[] wServers = MinecraftServer.getServer().worldServers;
			int indexStart = rand.nextInt(wServers.length);
			int index = indexStart;

			worldServer = null;
			do
			{
				if (ColourfulPortalsMod.isDimensionValidForDestination(wServers[index].provider.dimensionId)) {
					worldServer = wServers[index];
				}
				index++;
				if (index >= wServers.length) {
					index -= wServers.length;
				}
			} while ((index != indexStart) && (worldServer == null));
			if (worldServer == null) {
				return null;
			}
			dimension = worldServer.provider.dimensionId;
		}
		int var8 = var5;
		int var9 = var6;
		int var10 = var7;
		int var11 = 0;
		int var12 = rand.nextInt(4);
		for (int var13 = var5 - var2; var13 <= var5 + var2; var13++)
		{
			double var14 = var13 + 0.5D - var5;
			for (int var16 = var7 - var2; var16 <= var7 + var2; var16++)
			{
				double var17 = var16 + 0.5D - var7;
				label609:
					for (int var19 = worldServer.getActualHeight() - 1; var19 >= 0; var19--) {
						if (worldServer.isAirBlock(var13, var19, var16))
						{
							while ((var19 > 0) && (worldServer.isAirBlock(var13, var19 - 1, var16))) {
								var19--;
							}
							for (int var20 = var12; var20 < var12 + 4; var20++)
							{
								int var21 = var20 % 2;
								int var22 = 1 - var21;
								if (var20 % 4 >= 2)
								{
									var21 = -var21;
									var22 = -var22;
								}
								for (int var23 = 0; var23 < 3; var23++) {
									for (int var24 = 0; var24 < 4; var24++) {
										for (int var25 = -1; var25 < 4; var25++)
										{
											int var26 = var13 + (var24 - 1) * var21 + var23 * var22;
											int var27 = var19 + var25;
											int var28 = var16 + (var24 - 1) * var22 - var23 * var21;
											if (((var25 < 0) && (!worldServer.getBlock(var26, var27, var28).getMaterial().isSolid())) || ((var25 >= 0) && (!worldServer.isAirBlock(var26, var27, var28)))) {
												break label609;
											}
										}
									}
								}
								double var32 = var19 + 0.5D - var6;
								double var31 = var14 * var14 + var32 * var32 + var17 * var17;
								if ((var3 < 0.0D) || (var31 < var3))
								{
									var3 = var31;
									var8 = var13;
									var9 = var19;
									var10 = var16;
									var11 = var20 % 4;
								}
							}
						}
					}
			}
		}
		if (var3 < 0.0D) {
			for (int var13 = var5 - var2; var13 <= var5 + var2; var13++)
			{
				double var14 = var13 + 0.5D - var5;
				for (int var16 = var7 - var2; var16 <= var7 + var2; var16++)
				{
					double var17 = var16 + 0.5D - var7;
					label957:
						for (int var19 = worldServer.getActualHeight() - 1; var19 >= 0; var19--) {
							if (worldServer.isAirBlock(var13, var19, var16))
							{
								while ((var19 > 0) && (worldServer.isAirBlock(var13, var19 - 1, var16))) {
									var19--;
								}
								for (int var20 = var12; var20 < var12 + 2; var20++)
								{
									int var21 = var20 % 2;
									int var22 = 1 - var21;
									for (int var23 = 0; var23 < 4; var23++) {
										for (int var24 = -1; var24 < 4; var24++)
										{
											int var25 = var13 + (var23 - 1) * var21;
											int var26 = var19 + var24;
											int var27 = var16 + (var23 - 1) * var22;
											if (((var24 < 0) && (!worldServer.getBlock(var25, var26, var27).getMaterial().isSolid())) || ((var24 >= 0) && (!worldServer.isAirBlock(var25, var26, var27)))) {
												break label957;
											}
										}
									}
									double var32 = var19 + 0.5D - var6;
									double var31 = var14 * var14 + var32 * var32 + var17 * var17;
									if ((var3 < 0.0D) || (var31 < var3))
									{
										var3 = var31;
										var8 = var13;
										var9 = var19;
										var10 = var16;
										var11 = var20 % 2;
									}
								}
							}
						}
				}
			}
		}
		int var29 = var8;
		int var15 = var9;
		int var16 = var10;
		int var30 = var11 % 2;
		int var18 = 1 - var30;
		if (var11 % 4 >= 2)
		{
			var30 = -var30;
			var18 = -var18;
		}
		if (var3 < 0.0D)
		{
			if (var9 < 70) {
				var9 = 70;
			}
			if (var9 > worldServer.getActualHeight() - 10) {
				var9 = worldServer.getActualHeight() - 10;
			}
			var15 = var9;
			for (int var19 = -1; var19 <= 1; var19++) {
				for (int var20 = 1; var20 < 3; var20++) {
					for (int var21 = -1; var21 < 3; var21++)
					{
						int var22 = var29 + (var20 - 1) * var30 + var19 * var18;
						int var23 = var15 + var21;
						int var24 = var16 + (var20 - 1) * var18 - var19 * var30;
						boolean var33 = var21 < 0;
						worldServer.setBlock(var22, var23, var24, var33 ? frameBlock : Blocks.air);
					}
				}
			}
		}
		for (int var19 = 0; var19 < 4; var19++)
		{
			for (int var20 = 0; var20 < 4; var20++) {
				for (int var21 = -1; var21 < 4; var21++)
				{
					int var22 = var29 + (var20 - 1) * var30;
					int var23 = var15 + var21;
					int var24 = var16 + (var20 - 1) * var18;
					boolean var33 = (var20 == 0) || (var20 == 3) || (var21 == -1) || (var21 == 3);
					worldServer.setBlock(var22, var23, var24, var33 ? frameBlock : portalBlock, unshiftedMeta, 2);
				}
			}
			for (int var20 = 0; var20 < 4; var20++) {
				for (int var21 = -1; var21 < 4; var21++)
				{
					int var22 = var29 + (var20 - 1) * var30;
					int var23 = var15 + var21;
					int var24 = var16 + (var20 - 1) * var18;
					worldServer.notifyBlocksOfNeighborChange(var22, var23, var24, worldServer.getBlock(var22, var23, var24));
				}
			}
		}
		return new ColourfulPortalsMod.ColourfulPortalLocation(var29, var15, var16, dimension, meta);
	}

	public static void playColourfulTeleportSound(World world, double x, double y, double z)
	{
		world.playSoundEffect(x, y, z, "colourfulportalsmod:teleport", 1.0F, 1.0F);
	}

	private boolean entitySatisfiesTeleportConditions(World world, int x, int y, int z, Entity entity)
	{
		if (world.isRemote) {
			return false;
		}
		if (((entity instanceof EntityPlayer)) && (entity.getEntityData().getInteger("ColourfulPortalPlayerDelay") < 0)) {
			return false;
		}
		if (((entity instanceof EntityPlayer)) && (entity.getEntityData().getInteger("ColourfulPortalPlayerDelay") >= 10) && (entity.isSneaking())) {
			return true;
		}
		return !entity.getEntityData().getBoolean("InColourfulPortal");
	}

	private Entity teleportColourfully(World world, int xStart, int yStart, int zStart, Entity entity)
	{
		ColourfulPortalsMod.ColourfulPortalLocation destination = ColourfulPortalsMod.getColourfulDestination(world, xStart, yStart, zStart);
		//Make sure the dimension we are trying to teleport to exists first!
		if(MinecraftServer.getServer().worldServerForDimension(destination.dimension) == null) {
			return entity;
		}
		int meta = destination.portalMetadata;
		double x = destination.xPos + 0.5D;
		double y = destination.yPos + 0.1D + (ColourfulPortalsMod.isStandaloneCPBlock(MinecraftServer.getServer().worldServerForDimension(destination.dimension).getBlock(destination.xPos, destination.yPos, destination.zPos)) ? 1.0D : 0.0D);
		double z = destination.zPos + 0.5D;
		WorldServer newWorldServer = MinecraftServer.getServer().worldServerForDimension(destination.dimension);
		
		Entity ridingEntity = entity.ridingEntity;
		if(ridingEntity != null) {
			entity.mountEntity(null);
			ridingEntity = teleportColourfully(world, xStart, yStart, zStart, ridingEntity);
		}

		entity.getEntityData().setInteger("ColourfulPortalDelay", 10);
		entity.getEntityData().setBoolean("InColourfulPortal", true);
		
		EntityPlayerMP player = null;
		if ((entity instanceof EntityPlayer))
		{
			Iterator iterator = MinecraftServer.getServer().getConfigurationManager().playerEntityList.iterator();
			EntityPlayerMP entityplayermp = null;
			do
			{
				if (!iterator.hasNext()) {
					break;
				}
				entityplayermp = (EntityPlayerMP)iterator.next();
			} while (!entityplayermp.getCommandSenderName().equalsIgnoreCase(entity.getCommandSenderName()));
			player = entityplayermp;
		}
		if (player != null)
		{
			player.getEntityData().setInteger("ColourfulPortalPlayerDelay", 0);

			teleportPlayerColourfully(newWorldServer, x, y, z, player, destination);
		}
		else
		{
			entity = teleportEntityColourfully(newWorldServer, x, y, z, entity, destination);
		}
		
		doAfterTeleportStuff(world, x, y, z, meta, entity, newWorldServer, destination.xPos, destination.yPos, destination.zPos);

		//This isn't working for some reason.
		
		if(ridingEntity != null) {
			toStack.put(ridingEntity, entity);
			stackDelay = 2;
		}
		
		return entity;
	}

	private static Entity teleportEntityColourfully(World world, double x, double y, double z, Entity entity, ColourfulPortalsMod.ColourfulPortalLocation destination)
	{
		int meta = destination.portalMetadata;
		int dimension = destination.dimension;
		int currentDimension = entity.worldObj.provider.dimensionId;
		if (dimension != currentDimension)
		{
			entitiesTeleported.remove(entity);
			return transferEntityToDimension(entity, dimension, x, y, z);
		}
		
		entity.setLocationAndAngles(x, y, z, entity.rotationYaw, 0.0F);
		
		return entity;
	}

	private static void teleportPlayerColourfully(World world, double x, double y, double z, EntityPlayerMP player, ColourfulPortalsMod.ColourfulPortalLocation destination)
	{
		int meta = destination.portalMetadata;
		int dimension = destination.dimension;
		int currentDimension = player.worldObj.provider.dimensionId;
		if (currentDimension != dimension)
		{
			if (!world.isRemote) {
				if (currentDimension != 1) {
					player.mcServer.getConfigurationManager().transferPlayerToDimension(player, dimension, new ColourfulTeleporter(player.mcServer.worldServerForDimension(dimension), x, y, z));
				} else {
					forceTeleportPlayerFromEnd(player, dimension, new ColourfulTeleporter(player.mcServer.worldServerForDimension(dimension), x, y, z));
				}
				player.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));				
			}
		}
		else {
			player.playerNetServerHandler.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
			world.updateEntityWithOptionalForce(player, false);
		}
	}

	private static void doAfterTeleportStuff(World world, double x, double y, double z, int meta, Entity entity, World newWorld, double newX, double newY, double newZ)
	{
		playColourfulTeleportSound(world, x, y, z);
		playColourfulTeleportSound(newWorld, newX, newY, newZ);
		if (!entitiesTeleported.contains(entity)) {
			entitiesTeleported.add(entity);
		}
	}

	private static void forceTeleportPlayerFromEnd(EntityPlayerMP player, int newDimension, Teleporter colourfulTeleporter)
	{
		int j = player.dimension;
		WorldServer worldServerOld = player.mcServer.worldServerForDimension(player.dimension);
		player.dimension = newDimension;
		WorldServer worldServerNew = player.mcServer.worldServerForDimension(player.dimension);
		player.playerNetServerHandler.sendPacket(new S07PacketRespawn(player.dimension, player.worldObj.difficultySetting, player.worldObj.getWorldInfo().getTerrainType(), player.theItemInWorldManager.getGameType()));
		worldServerOld.removePlayerEntityDangerously(player);
		player.isDead = false;

		WorldProvider pOld = worldServerOld.provider;
		WorldProvider pNew = worldServerNew.provider;
		double moveFactor = pOld.getMovementFactor() / pNew.getMovementFactor();
		double d0 = player.posX * moveFactor;
		double d1 = player.posZ * moveFactor;
		double d3 = player.posX;
		double d4 = player.posY;
		double d5 = player.posZ;
		float f = player.rotationYaw;

		worldServerOld.theProfiler.startSection("placing");
		d0 = MathHelper.clamp_int((int)d0, -29999872, 29999872);
		d1 = MathHelper.clamp_int((int)d1, -29999872, 29999872);
		if (player.isEntityAlive())
		{
			player.setLocationAndAngles(d0, player.posY, d1, player.rotationYaw, player.rotationPitch);
			colourfulTeleporter.placeInPortal(player, d3, d4, d5, f);
			worldServerNew.spawnEntityInWorld(player);
			worldServerNew.updateEntityWithOptionalForce(player, false);
		}
		worldServerOld.theProfiler.endSection();

		player.setWorld(worldServerNew);

		player.mcServer.getConfigurationManager().func_72375_a(player, worldServerOld);
		player.playerNetServerHandler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
		player.theItemInWorldManager.setWorld(worldServerNew);
		player.mcServer.getConfigurationManager().updateTimeAndWeatherForPlayer(player, worldServerNew);
		player.mcServer.getConfigurationManager().syncPlayerInventory(player);
		Iterator iterator = player.getActivePotionEffects().iterator();
		while (iterator.hasNext())
		{
			PotionEffect potioneffect = (PotionEffect)iterator.next();
			player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), potioneffect));
		}
		FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, j, newDimension);
	}

	public static Entity transferEntityToDimension(Entity toTeleport, int newDimension, double x, double y, double z)
	{
		if (!toTeleport.isDead)
		{
			toTeleport.worldObj.theProfiler.startSection("changeDimension");
			MinecraftServer minecraftserver = MinecraftServer.getServer();
			int oldDimension = toTeleport.dimension;
			WorldServer worldServerOld = minecraftserver.worldServerForDimension(oldDimension);
			WorldServer worldServerNew = minecraftserver.worldServerForDimension(newDimension);
			toTeleport.dimension = newDimension;

			toTeleport.worldObj.removeEntity(toTeleport);
			toTeleport.isDead = false;
			toTeleport.worldObj.theProfiler.startSection("reposition");
			if (oldDimension == 1) {
				forceTeleportEntityFromEnd(toTeleport, newDimension, new ColourfulTeleporter(worldServerOld, x, y, z), worldServerNew);
			} else {
				minecraftserver.getConfigurationManager().transferEntityToWorld(toTeleport, oldDimension, worldServerOld, worldServerNew, new ColourfulTeleporter(worldServerOld, x, y, z));
			}
			toTeleport.worldObj.theProfiler.endStartSection("reloading");
			Entity entity = EntityList.createEntityByName(EntityList.getEntityString(toTeleport), worldServerNew);
			if (entity != null)
			{
				entity.copyDataFrom(toTeleport, true);
				worldServerNew.spawnEntityInWorld(entity);
			}
			toTeleport.isDead = true;
			toTeleport.worldObj.theProfiler.endSection();
			worldServerOld.resetUpdateEntityTick();
			worldServerNew.resetUpdateEntityTick();
			toTeleport.worldObj.theProfiler.endSection();

			return entity;
		}
		return toTeleport;
	}

	private static void forceTeleportEntityFromEnd(Entity entity, int newDimension, Teleporter colourfulTeleporter, WorldServer worldServerNew)
	{
		worldServerNew.spawnEntityInWorld(entity);
		entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
		worldServerNew.updateEntityWithOptionalForce(entity, false);
		colourfulTeleporter.placeInPortal(entity, 0.0D, 0.0D, 0.0D, 0.0F);

		entity.setWorld(worldServerNew);
	}

	public void serverTick()
	{
		ArrayList<Entity> toRemove = new ArrayList();
		for (Entity entity : entitiesTeleported) {
			if (entity.isDead)
			{
				toRemove.add(entity);
			}
			else
			{
				World world = entity.worldObj;
				boolean inCP = true;
				if ((!ColourfulPortalsMod.isCPBlock(entity.worldObj.getBlock((int)Math.floor(entity.posX), (int)Math.floor(entity.posY), (int)Math.floor(entity.posZ)))) && (!ColourfulPortalsMod.isCPBlock(entity.worldObj.getBlock((int)Math.floor(entity.posX), (int)Math.floor(entity.posY - 1.0D), (int)Math.floor(entity.posZ)))) && (entity.getEntityData().getInteger("ColourfulPortalDelay") > 0) && (inCP)) {
					entity.getEntityData().setInteger("ColourfulPortalDelay", entity.getEntityData().getInteger("ColourfulPortalDelay") - 1);
				}
				if (entity.getEntityData().getInteger("ColourfulPortalDelay") <= 0) {
					inCP = false;
				}
				if ((entity instanceof EntityPlayer))
				{
					int delay = entity.getEntityData().getInteger("ColourfulPortalPlayerDelay");
					if (delay < 10) {
						entity.getEntityData().setInteger("ColourfulPortalPlayerDelay", delay + 1);
					}
				}
				if (!inCP)
				{
					entity.getEntityData().setBoolean("InColourfulPortal", false);
					if ((entity instanceof EntityPlayer)) {
						entity.getEntityData().setInteger("ColourfulPortalPlayerDelay", 0);
					}
					toRemove.add(entity);
				}
			}
		}
		for (Entity entity : toRemove) {
			entitiesTeleported.remove(entity);
		}
		
		if(stackDelay <= 0) {
			//Restack any stacked entities
			for(Entity mount : toStack.keySet()) {
				Entity riding = toStack.get(mount);
				if(riding instanceof EntityPlayer) {
					riding.worldObj.updateEntityWithOptionalForce(riding, true);
				}
				riding.mountEntity(mount);
			}
			
			toStack.clear();
		}
		else {
			stackDelay--;
		}
	}
}