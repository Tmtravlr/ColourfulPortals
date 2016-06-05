package com.tmtravlr.colourfulportalsmod;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import java.util.TreeSet;

import scala.actors.threadpool.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IRegistry;
import net.minecraft.util.MathHelper;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapelessOreRecipe;

@Mod(modid="colourfulportalsmod", name="Colourful Portals Mod", version="1.4.3")
public class ColourfulPortalsMod
{
	@Mod.Instance("colourfulPortalsMod")
	public static ColourfulPortalsMod colourfulPortalsMod;
	@SidedProxy(clientSide="com.tmtravlr.colourfulportalsmod.ClientProxy", serverSide="com.tmtravlr.colourfulportalsmod.CommonProxy")
	public static CommonProxy proxy;
	public static final ColourfulFluid colourfulFluid = new ColourfulFluid();
	public static Item bucketColourfulWaterEmpty;
	public static Item bucketColourfulWater;
	public static Item bucketColourfulWaterUnmixed;
	public static Item bucketColourfulWaterPartMixed;
	public static Item bucketColourfulWaterFirst;
	public static Item enderPearlColoured;
	public static Item enderPearlColouredReflective;
	public static Block colourfulWater;
	public static HashMap<Integer, BlockColourfulPortal> cpBlocks = new HashMap();
	public static HashMap<Integer, BlockStandaloneCP> scpBlocks = new HashMap();
	public static HashMap<Integer, Block> frameBlocks = new HashMap();
	private static HashMap<Integer, String> frameNames = new HashMap();
	private static final boolean debug = false;
	public static int colourfulPortalRenderId;
	public static int maxPortalGenerationDistance = 3000;
	public static int maxPortalsPerType = -1;
	public static int maxPortalSizeCheck = 16;
	public static int xpLevelMixingCost = 5;
	public static int xpLevelRemixingCost = 2;
	public static boolean useDestinationBlackList = true;
	public static boolean useDestinationWhiteList = false;
	public static boolean xpBottleCrafting = false;
	public static int[] destinationBlackList = { 1 };
	public static int[] destinationWhiteList = { 0, -1 };
	public static boolean useDimensionBlackList = false;
	public static boolean useDimensionWhiteList = false;
	public static int[] dimensionBlackList = new int[0];
	public static int[] dimensionWhiteList = { 0, 1, -1 };
	public static String[] frameBlockNames = { "wool", "stained_hardened_clay", "stained_glass" };
	private static String bucketColourfulWaterEmptyId = "bucket_colourful_water_empty";
	private static String bucketColourfulWaterId = "bucket_colourful_water";
	private static String bucketColourfulWaterUnmixedId = "bucket_colourful_water_unmixed";
	private static String bucketColourfulWaterPartMixedId = "bucket_colourful_water_part_mixed";
	private static String bucketColourfulWaterFirstId = "bucket_colourful_water_first";
	private static String colourfulEnderPearlId = "colourful_ender_pearl";
	private static String colourfulEnderPearlReflectiveId = "colourful_ender_pearl_reflective";
	private static String colourfulWaterId = "colourful_water";
	private static String colourfulPortalIdPrefix = "cp_";
	private static String standaloneCPIdPrefix = "scp_";
	public static CreativeTabs cpTab = new CreativeTabs("colourfulPortals")
	{
		public Item getTabIconItem()
		{
			return ColourfulPortalsMod.bucketColourfulWater;
		}
	};
	private static LinkedList<ColourfulPortalLocation> colourfulPortals = new LinkedList();
	private File saveLocation;
	private boolean loaded;
	public String currentFolder;

	public ColourfulPortalsMod()
	{
		this.loaded = false;
		this.currentFolder = "";
	}

	//public static RenderStandaloneCP standaloneRenderer = new RenderStandaloneCP();

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		colourfulPortalsMod = this;
		boolean addColourfulWaterToDungeonChests = true;

		Configuration config = new Configuration(event.getSuggestedConfigurationFile());

		config.load();


		maxPortalGenerationDistance = config.getInt("Random Portal Generation Max Distance", "other", 3000, 0, 30000000, "Maximum distance away from you a randomized portal will generate.");
		maxPortalsPerType = config.getInt("Maximum Number of Portals per Type (Colour and Material)", "other", -1, -1, Integer.MAX_VALUE, "Maximum number of portals you can make per type. -1 means unlimited.");
		maxPortalSizeCheck = config.getInt("Maximum Portal Size (Make Bigger for Larger Portals)", "other", 16, 1, Integer.MAX_VALUE, "Limit on the maximum size of portal to prevent lag from accidentally creating massive portals.\nThe portal will create up to this number of blocks away from where you place the colourful water.");
		xpLevelMixingCost = config.getInt("Number of XP Levels Needed to Mix Colourful Water", "other", 5, 0, Integer.MAX_VALUE, "Levels of XP you need to mix the colourful water from a bucket of dyes.");
		xpLevelRemixingCost = config.getInt("Number of XP Levels Needed to Re-Mix Colourful Water", "other", 2, 0, Integer.MAX_VALUE, "Levels of XP that you need to mix the partially enchanted bucket of dyes.");
		xpBottleCrafting = config.getBoolean("Allow crafting of colourful water with XP bottles (for automation)", "other", false, "Adds a crafting recipe for the colourful water using XP bottles.");
		addColourfulWaterToDungeonChests = config.getBoolean("Add Buckets of Colourful Water to Dungeon Chests?", "other", true, "If set to true, full and empty buckets of colourful water will occasionally spawn in chests.");
		if (xpLevelRemixingCost > xpLevelMixingCost) {
			xpLevelRemixingCost = xpLevelMixingCost;
		}
		config.addCustomCategoryComment("random_destination_blacklist", "If set to true, random destination portals with random dimensions\nwill not generate in any of the dimensions in this list. They can\nstill be created with same-dimension random destinations or placed\nmanually. Defaults to true. Takes precedence over the whitelist.");

		config.addCustomCategoryComment("random_destination_whitelist", "If set to true, random destination portals with random dimensions\nwill only generate in these dimensions. Ones with the same dimension\ncan still generate elsewhere. Defaults to false.");

		config.addCustomCategoryComment("dimension_blacklist", "If set to true, portals cannot be created in these dimensions\nat all, whether framed, single block, or 'random destination'\nportals. Takes precedence over the whitelist. Defaults to false.");

		config.addCustomCategoryComment("dimension_whitelist", "If set to true, portals can ONLY be created in the given dimensions,\nwhether framed, single block, or 'random destination' portals.\nDefaults to false.");

		int[] defaultBlackList = { 1 };
		int[] defaultWhiteList = { 0, -1 };
		int[] fullDefaultList = { 0, 1, -1 };
		int[] emptyList = new int[0];

		useDestinationBlackList = config.getBoolean("Use this Blacklist?", "random_destination_blacklist", true, "");
		useDestinationWhiteList = config.getBoolean("Use this Whitelist?", "random_destination_whitelist", false, "");
		destinationBlackList = config.get("random_destination_blacklist", "List of Blacklisted Dimensions for Random Generation", defaultBlackList).getIntList();
		destinationWhiteList = config.get("random_destination_whitelist", "List of Whitelisted Dimensions for Random Generation", defaultWhiteList).getIntList();
		
		useDimensionBlackList = config.getBoolean("Use this Blacklist?", "dimension_blacklist", false, "");
		useDimensionWhiteList = config.getBoolean("Use this Whitelist?", "dimension_whitelist", false, "");
		dimensionBlackList = config.get("dimension_blacklist", "List of Blacklisted Dimensions for all Portals", emptyList).getIntList();
		dimensionWhiteList = config.get("dimension_whitelist", "List of Whitelisted Dimensions for all Portals", fullDefaultList).getIntList();

		config.addCustomCategoryComment("portal_frame_types", "Blocks that can be used to make portals out of.\nThey should have 16 metadata types that represent\ncolours in the same way as wool.");

		String[] defaultPortalTypes = { "wool", "stained_hardened_clay", "stained_glass" };
		//No support for custom portal types at the moment!
		frameBlockNames = defaultPortalTypes;//= config.get("portal_frame_types", "Portal Frame Blocks", defaultPortalTypes).getStringList();
		bucketColourfulWaterEmpty = new ItemBucketColourfulWater(true, true, false).setUnlocalizedName("bucketColourfulWaterEmpty");
		bucketColourfulWater = new ItemBucketColourfulWater(true, true, true).setUnlocalizedName("bucketColourfulWater");
		bucketColourfulWaterUnmixed = new ItemBucketColourfulWater(false, false, true).setUnlocalizedName("bucketColourfulWaterUnmixed");
		bucketColourfulWaterPartMixed = new ItemBucketColourfulWater(true, false, true).setUnlocalizedName("bucketColourfulWaterPartMixed");
		bucketColourfulWaterFirst = new ItemBucketColourfulWater(false, true, false).setUnlocalizedName("bucketColourfulWaterFirst");
		enderPearlColoured = new ItemEnderPearlColoured(false);
		enderPearlColouredReflective = new ItemEnderPearlColoured(true);
		colourfulWater = new BlockColourfulWater().setDensity(colourfulFluid.getDensity()).setHardness(100.0F).setLightOpacity(3);
		
		config.save();

		GameRegistry.registerItem(bucketColourfulWaterEmpty, bucketColourfulWaterEmptyId);
		GameRegistry.registerItem(bucketColourfulWater, bucketColourfulWaterId);
		GameRegistry.registerItem(bucketColourfulWaterUnmixed, bucketColourfulWaterUnmixedId);
		GameRegistry.registerItem(bucketColourfulWaterPartMixed, bucketColourfulWaterPartMixedId);
		GameRegistry.registerItem(bucketColourfulWaterFirst, bucketColourfulWaterFirstId);
		GameRegistry.registerItem(enderPearlColoured, colourfulEnderPearlId);
		GameRegistry.registerItem(enderPearlColouredReflective, colourfulEnderPearlReflectiveId);

		GameRegistry.registerBlock(colourfulWater, colourfulWaterId);
		FluidContainerRegistry.registerFluidContainer(colourfulFluid, new ItemStack(bucketColourfulWater), new ItemStack(bucketColourfulWaterEmpty));


		BlockDispenser.dispenseBehaviorRegistry.putObject(bucketColourfulWaterEmpty, new BehaviorDefaultDispenseItem()
		{
			private final BehaviorDefaultDispenseItem field_150840_b = new BehaviorDefaultDispenseItem();
			
			public ItemStack dispenseStack(IBlockSource blockSource, ItemStack itemStack)
			{
				EnumFacing enumfacing = BlockDispenser.getFacing(blockSource.getBlockMetadata());
				World world = blockSource.getWorld();
				int x = MathHelper.floor_double(blockSource.getX() + enumfacing.getFrontOffsetX());
				int y = MathHelper.floor_double(blockSource.getY() + enumfacing.getFrontOffsetY());
				int z = MathHelper.floor_double(blockSource.getZ() + enumfacing.getFrontOffsetZ());
				BlockPos pos = new BlockPos(x, y, z);
				int meta = getMeta(world, pos);
				Item item = itemStack.getItem();
				if ((world.getBlockState(pos) == ColourfulPortalsMod.colourfulWater) && (meta == 0)) {
					item = ColourfulPortalsMod.bucketColourfulWater;
				} else {
					return super.dispenseStack(blockSource, itemStack);
				}
				world.setBlockToAir(pos);
				if (--itemStack.stackSize == 0)
				{
					itemStack.setItem(item);
					itemStack.stackSize = 1;
				}
				else if (((TileEntityDispenser)blockSource.getBlockTileEntity()).addItemStack(new ItemStack(item)) < 0)
				{
					this.field_150840_b.dispense(blockSource, new ItemStack(item));
				}
				return itemStack;
			}
		});
		BlockDispenser.dispenseBehaviorRegistry.putObject(bucketColourfulWater, new BehaviorDefaultDispenseItem()
		{
			private final BehaviorDefaultDispenseItem field_150840_b = new BehaviorDefaultDispenseItem();
			
			public ItemStack dispenseStack(IBlockSource blockSource, ItemStack itemStack)
			{
				EnumFacing enumfacing = BlockDispenser.getFacing(blockSource.getBlockMetadata());
				World world = blockSource.getWorld();
				int x = MathHelper.floor_double(blockSource.getX() + enumfacing.getFrontOffsetX());
				int y = MathHelper.floor_double(blockSource.getY() + enumfacing.getFrontOffsetY());
				int z = MathHelper.floor_double(blockSource.getZ() + enumfacing.getFrontOffsetZ());
				int meta = getMeta(world, new BlockPos(x, y, z));
				Item item = itemStack.getItem();
				if ((world.isAirBlock(new BlockPos(x, y, z))) && (meta == 0)) {
					item = ColourfulPortalsMod.bucketColourfulWaterEmpty;
				} else {
					return super.dispenseStack(blockSource, itemStack);
				}
				world.setBlockState(new BlockPos(x, y, z), ColourfulPortalsMod.colourfulWater.getDefaultState());
				if (--itemStack.stackSize == 0)
				{
					itemStack.setItem(item);
					itemStack.stackSize = 1;
				}
				else if (((TileEntityDispenser)blockSource.getBlockTileEntity()).addItemStack(new ItemStack(item)) < 0)
				{
					this.field_150840_b.dispense(blockSource, new ItemStack(item));
				}
				return itemStack;
			}
		});
		if (addColourfulWaterToDungeonChests)
		{
			ChestGenHooks.getInfo("pyramidDesertyChest").addItem(new WeightedRandomChestContent(bucketColourfulWaterEmpty, 0, 1, 1, 3));
			ChestGenHooks.getInfo("pyramidJungleChest").addItem(new WeightedRandomChestContent(bucketColourfulWaterEmpty, 0, 1, 1, 2));
			ChestGenHooks.getInfo("strongholdCrossing").addItem(new WeightedRandomChestContent(bucketColourfulWaterEmpty, 0, 1, 1, 1));
			ChestGenHooks.getInfo("strongholdCorridor").addItem(new WeightedRandomChestContent(bucketColourfulWaterEmpty, 0, 1, 1, 1));
			ChestGenHooks.getInfo("dungeonChest").addItem(new WeightedRandomChestContent(bucketColourfulWaterEmpty, 0, 1, 1, 1));

			ChestGenHooks.getInfo("pyramidDesertyChest").addItem(new WeightedRandomChestContent(bucketColourfulWater, 0, 1, 1, 2));
			ChestGenHooks.getInfo("pyramidJungleChest").addItem(new WeightedRandomChestContent(bucketColourfulWater, 0, 1, 1, 1));
			ChestGenHooks.getInfo("strongholdCrossing").addItem(new WeightedRandomChestContent(bucketColourfulWater, 0, 1, 1, 1));
			ChestGenHooks.getInfo("strongholdCorridor").addItem(new WeightedRandomChestContent(bucketColourfulWater, 0, 1, 1, 1));
			ChestGenHooks.getInfo("dungeonChest").addItem(new WeightedRandomChestContent(bucketColourfulWater, 0, 1, 1, 1));
		}
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent event)
	{
		for (int i = 0; i < frameBlockNames.length; i++)
		{
			Block frameBlock = Block.getBlockFromName(frameBlockNames[i]);
			
			if(frameBlock == null || frameBlock == Blocks.air) {
				FMLLog.warning("[Colourful Portals] Error! Couldn't find a block with name '" + frameBlockNames[i] + "'!");
				continue;
			}
			
			cpBlocks.put(i, (BlockColourfulPortal)new BlockColourfulPortal("portal_colour", Material.portal).setHardness(-1.0F).setStepSound(Block.soundTypeGlass).setLightLevel(0.75F).setUnlocalizedName("colourfulPortal"));
			scpBlocks.put(i, (BlockStandaloneCP)new BlockStandaloneCP("portal_colour", frameBlock.getMaterial()).setHardness(0.8F).setStepSound(frameBlock.stepSound).setLightLevel(0.75F).setUnlocalizedName("standaloneColourfulPortal"));
			frameBlocks.put(i, frameBlock);
			
			int colonIndex = frameBlockNames[i].indexOf(":");
			if (colonIndex != -1) {
				frameNames.put(i, frameBlockNames[i].substring(0, colonIndex) + "_" + frameBlockNames[i].substring(colonIndex + 1));
			}
			else {
				frameNames.put(i, frameBlockNames[i]);
			}
		}
		
		for (int i = 0; i < cpBlocks.size(); i++)
		{
			GameRegistry.registerBlock((Block)cpBlocks.get(i), colourfulPortalIdPrefix + frameNames.get(i));
			GameRegistry.registerBlock((Block)scpBlocks.get(i), ItemStandaloneCP.class, standaloneCPIdPrefix + frameNames.get(i));
		}

		
		for (int f = 0; f < frameBlocks.size(); f++) {
			for (int i = 0; i < 16; i++)
			{
				ItemStack frame = new ItemStack(frameBlocks.get(f), 1, i);
				ItemStack sCPStack = new ItemStack(scpBlocks.get(f), 1, i);

				GameRegistry.addRecipe(sCPStack, new Object[] { "WWW", "WBW", "WWW", Character.valueOf('W'), frame, Character.valueOf('B'), bucketColourfulWater });
			}
		}
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(bucketColourfulWaterPartMixed, 1), new Object[] { Items.water_bucket, bucketColourfulWaterEmpty, "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyeYellow", "dyeWhite" }));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(bucketColourfulWaterUnmixed, 1), new Object[] { Items.water_bucket, bucketColourfulWaterFirst, "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyeYellow", "dyeWhite" }));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(enderPearlColoured, 1), new Object[] { Items.ender_pearl, "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyeYellow", "dyeWhite" }));
		GameRegistry.addRecipe(new ItemStack(bucketColourfulWaterFirst, 1), new Object[] { "G", "B", Character.valueOf('G'), Items.gold_ingot, Character.valueOf('B'), Items.bucket });
		GameRegistry.addRecipe(new ItemStack(bucketColourfulWaterFirst, 1), new Object[] { "IGI", " I ", Character.valueOf('G'), Items.gold_ingot, Character.valueOf('I'), Items.iron_ingot });
		GameRegistry.addRecipe(new ItemStack(enderPearlColouredReflective, 1), new Object[] { " Q ", "QPQ", " Q ", Character.valueOf('Q'), Items.quartz, Character.valueOf('P'), enderPearlColoured });
		GameRegistry.addShapelessRecipe(new ItemStack(bucketColourfulWaterFirst), new Object[] { new ItemStack(bucketColourfulWaterEmpty) });
		GameRegistry.addShapelessRecipe(new ItemStack(Items.bucket), new Object[] { new ItemStack(bucketColourfulWaterFirst) });
		
		if(xpBottleCrafting) {
			GameRegistry.addShapelessRecipe(new ItemStack(bucketColourfulWater), new Object[] {new ItemStack(bucketColourfulWaterUnmixed), new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle)});
			GameRegistry.addShapelessRecipe(new ItemStack(bucketColourfulWater), new Object[] {new ItemStack(bucketColourfulWaterPartMixed), new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle)});
		}

		proxy.registerSounds();
		proxy.registerRenderers();
		proxy.registerEventHandlers();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {}

	public static boolean isStandaloneCPBlock(Block block)
	{
		return scpBlocks.containsValue(block);
	}

	public static boolean isFramedCPBlock(Block block)
	{
		return cpBlocks.containsValue(block);
	}

	public static boolean isCPBlock(Block block)
	{
		return (isStandaloneCPBlock(block)) || (isFramedCPBlock(block));
	}

	public static boolean isFrameBlock(Block block)
	{
		return frameBlocks.containsValue(block);
	}

	public static boolean isPortalOrFrameBlock(IBlockAccess iba, BlockPos pos)
	{
		return (isFramedCPBlock(iba.getBlockState(pos).getBlock())) || (isFrameBlock(iba.getBlockState(pos).getBlock()));
	}

	public static int getShiftedCPMetadata(IBlockAccess iba, BlockPos pos)
	{
		IBlockState state = iba.getBlockState(pos); 
		return getShiftedCPMetadata(state);
	}

	public static int getIndexFromShiftedMetadata(int meta)
	{
		return (int)Math.floor(meta / 16);
	}

	public static int getShiftedCPMetadata(IBlockState state)
	{
		for (int i = 0; i < frameBlocks.size(); i++) {
			if ((cpBlocks.get(i) == state.getBlock()) || (scpBlocks.get(i) == state.getBlock())) {
				return getMeta(state) + 16 * i;
			}
		}
		return -1;
	}

	public static int getShiftedCPMetadataByFrameBlock(IBlockState state)
	{
		for (int i = 0; i < frameBlocks.size(); i++) {
			if (frameBlocks.get(i) == state.getBlock()) {
				return getMeta(state) + 16 * i;
			}
		}
		return -1;
	}

	public static Block getCPBlockByShiftedMetadata(int meta)
	{
		return (Block)cpBlocks.get(getIndexFromShiftedMetadata(meta));
	}

	public static Block getStandaloneCPBlockByShiftedMetadata(int meta)
	{
		return (Block)scpBlocks.get(getIndexFromShiftedMetadata(meta));
	}

	public static Block getFrameBlockByShiftedMetadata(int meta)
	{
		return frameBlocks.get(getIndexFromShiftedMetadata(meta));
	}

	public static Block getCPBlockByFrameBlock(Block frameBlock)
	{
		for (int i = 0; i < frameBlocks.size(); i++) {
			if (frameBlocks.get(i) == frameBlock) {
				return (Block)cpBlocks.get(i);
			}
		}
		return null;
	}

	public static int unshiftCPMetadata(int meta)
	{
		return meta % 16;
	}

	public static void setFramedCPBlock(World world, BlockPos pos, Block frameBlock, int meta, int flag)
	{
		int index = getIndexFromShiftedMetadata(getShiftedCPMetadataByFrameBlock(frameBlock.getStateFromMeta(meta)));
		Block block = (Block)cpBlocks.get(index);
		world.setBlockState(pos, block.getStateFromMeta(meta), flag);
	}
	
	public static int getMeta(IBlockAccess iba, BlockPos pos) {
		return getMeta(iba.getBlockState(pos));
	}
	
	public static int getMeta(IBlockState state) {
		return state.getBlock().getMetaFromState(state);
	}

	public static boolean isDimensionValidForDestination(int dimension)
	{
		if (!isDimensionValidAtAll(dimension)) {
			return false;
		}
		if (useDestinationWhiteList)
		{
			if (destinationWhiteList.length == 0) {
				return false;
			}
			boolean inWhiteList = false;
			for (int i = 0; i < destinationWhiteList.length; i++) {
				if (dimension == destinationWhiteList[i]) {
					inWhiteList = true;
				}
			}
			if (!inWhiteList) {
				return false;
			}
		}
		if (useDestinationBlackList) {
			for (int i = 0; i < destinationBlackList.length; i++) {
				if (dimension == destinationBlackList[i]) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean isDimensionValidAtAll(int dimension)
	{
		if (useDimensionWhiteList)
		{
			if (dimensionWhiteList.length == 0) {
				return false;
			}
			boolean inWhiteList = false;
			for (int i = 0; i < dimensionWhiteList.length; i++) {
				if (dimension == dimensionWhiteList[i]) {
					inWhiteList = true;
				}
			}
			if (!inWhiteList) {
				return false;
			}
		}
		if (useDimensionBlackList) {
			for (int i = 0; i < dimensionBlackList.length; i++) {
				if (dimension == dimensionBlackList[i]) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean canCreatePortal(World world, BlockPos pos, IBlockState state)
	{
		if (tooManyPortals(state)) {
			return false;
		}
		if (!isDimensionValidAtAll(world.provider.getDimensionId())) {
			return false;
		}
		return true;
	}

	public static boolean tooManyPortals(IBlockState state)
	{
		if (maxPortalsPerType < 0) {
			return false;
		}
		if (maxPortalsPerType == 0) {
			return true;
		}
		int portalsWithType = 0;
		for (int i = 0; i < colourfulPortals.size(); i++) {
			if (((ColourfulPortalLocation)colourfulPortals.get(i)).portalMetadata == getShiftedCPMetadata(state)) {
				portalsWithType++;
			}
		}
		if (portalsWithType >= maxPortalsPerType) {
			return true;
		}
		return false;
	}

	public void loadPortalsList()
	{
		FileInputStream fInput = null;
		ObjectInputStream oInput = null;
		try
		{
			this.saveLocation = proxy.getSaveLocation();
			if (this.saveLocation.exists())
			{
				fInput = new FileInputStream(this.saveLocation);
				oInput = new ObjectInputStream(fInput);
				colourfulPortals = (LinkedList)oInput.readObject();
				oInput.close();
				fInput.close();

				checkForPortalChanges();
			}
			else
			{
				this.saveLocation.createNewFile();





				colourfulPortals = new LinkedList();
			}
		}
		catch (Exception e)
		{
			if (!(e instanceof EOFException)) {
				e.printStackTrace();
			}
			try
			{
				if (oInput != null) {
					oInput.close();
				}
				if (fInput != null) {
					fInput.close();
				}
			}
			catch (IOException ioe) {}
		}
	}

	private void checkForPortalChanges()
	{
		ArrayList<ColourfulPortalLocation> toDelete = new ArrayList();
		for (ColourfulPortalLocation portal : colourfulPortals)
		{
			WorldServer currentWS = MinecraftServer.getServer().worldServerForDimension(portal.dimension);
			BlockPos currentPos = new BlockPos(portal.xPos, portal.yPos, portal.zPos);
			if ((getCPBlockByShiftedMetadata(portal.portalMetadata) != currentWS.getBlockState(currentPos).getBlock()) && (getStandaloneCPBlockByShiftedMetadata(portal.portalMetadata) != currentWS.getBlockState(currentPos).getBlock())) {
				if (!BlockColourfulPortal.tryToCreatePortal(currentWS, currentPos, false)) {
					toDelete.add(portal);
				}
			}
		}
		for (ColourfulPortalLocation deleted : toDelete) {
			colourfulPortals.remove(deleted);
		}
		savePortals();
	}

	public static ColourfulPortalLocation getColourfulDestination(World world, BlockPos pos)
	{
		if (colourfulPortals.size() > 0)
		{
			ColourfulPortalLocation start = findCPLocation(world, pos);


			int originalPos = colourfulPortals.indexOf(start);
			if (originalPos == -1) {
				return new ColourfulPortalLocation(pos, world.provider.getDimensionId(), getShiftedCPMetadata(world, pos));
			}
			int size = colourfulPortals.size();
			for (int i = 0; i < size; i++)
			{
				int index = i + originalPos + 1;
				if (index >= size) {
					index -= size;
				}
				ColourfulPortalLocation current = (ColourfulPortalLocation)colourfulPortals.get(index);
				if (current.portalMetadata == start.portalMetadata) {
					if(MinecraftServer.getServer().worldServerForDimension(current.dimension) != null) {
						return current;
					}
				}
			}
			return start;
		}
		return new ColourfulPortalLocation(pos, world.provider.getDimensionId(), getShiftedCPMetadata(world, pos));
	}

	public static ColourfulPortalLocation findCPLocation(World world, BlockPos pos)
	{
		if (!isCPBlock(world.getBlockState(pos).getBlock())) {
			return null;
		}
		if (isStandaloneCPBlock(world.getBlockState(pos).getBlock())) {
			return new ColourfulPortalLocation(pos, world.provider.getDimensionId(), getShiftedCPMetadata(world, pos));
		}
		boolean xDir = true;
		boolean yDir = true;
		boolean zDir = true;
		int i = 0;
		int maxSize = maxPortalSizeCheck * maxPortalSizeCheck + 1;
		for (i = 0; (i < maxSize) && (isCPBlock(world.getBlockState(pos.add(i, 0, 0)).getBlock())); i++) {}
		if (!isFrameBlock(world.getBlockState(pos.add(i, 0, 0)).getBlock()))
		{
			zDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && (isCPBlock(world.getBlockState(pos.add(-i, 0, 0)).getBlock())); i++) {}
		if (!isFrameBlock(world.getBlockState(pos.add(-i, 0, 0)).getBlock()))
		{
			zDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && (isCPBlock(world.getBlockState(pos.add(0, i, 0)).getBlock())); i++) {}
		if (!isFrameBlock(world.getBlockState(pos.add(0, i, 0)).getBlock()))
		{
			zDir = false;
			xDir = false;
		}
		for (i = 0; (i < maxSize) && (isCPBlock(world.getBlockState(pos.add(0, -i, 0)).getBlock())); i++) {}
		if (!isFrameBlock(world.getBlockState(pos.add(0, -i, 0)).getBlock()))
		{
			zDir = false;
			xDir = false;
		}
		for (i = 0; (i < maxSize) && (isCPBlock(world.getBlockState(pos.add(0, 0, i)).getBlock())); i++) {}
		if (!isFrameBlock(world.getBlockState(pos.add(0, 0, i)).getBlock()))
		{
			xDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && (isCPBlock(world.getBlockState(pos.add(0, 0, -i)).getBlock())); i++) {}
		if (!isFrameBlock(world.getBlockState(pos.add(0, 0, -i)).getBlock()))
		{
			xDir = false;
			yDir = false;
		}
		if ((!xDir) && (!yDir) && (!zDir)) {
			return null;
		}
		CPLSet visited = new CPLSet();
		Stack<ColourfulPortalLocation> toVisit = new Stack();

		toVisit.push(new ColourfulPortalLocation(pos, world.provider.getDimensionId(), getShiftedCPMetadata(world, pos)));

		visited.add(toVisit.peek());
		while (!toVisit.empty())
		{
			ColourfulPortalLocation current = (ColourfulPortalLocation)toVisit.pop();
			if (colourfulPortals.contains(current)) {
				return current;
			}
			BlockPos currentPos = new BlockPos(current.xPos, current.yPos, current.zPos);
			if ((zDir) || (xDir))
			{
				if (isCPBlock(world.getBlockState(currentPos.add(0, 1, 0)).getBlock()))
				{
					ColourfulPortalLocation temp = new ColourfulPortalLocation(currentPos.add(0, 1, 0), world.provider.getDimensionId(), getShiftedCPMetadata(world, currentPos.add(0, 1, 0)));
					if (!visited.contains(temp))
					{
						toVisit.push(temp);
						visited.add(temp);
					}
				}
				if (isCPBlock(world.getBlockState(currentPos.add(0, -1, 0)).getBlock()))
				{
					ColourfulPortalLocation temp = new ColourfulPortalLocation(currentPos.add(0, -1, 0), world.provider.getDimensionId(), getShiftedCPMetadata(world, currentPos.add(0, -1, 0)));
					if (!visited.contains(temp))
					{
						toVisit.push(temp);
						visited.add(temp);
					}
				}
			}
			if ((zDir) || (yDir))
			{
				if (isCPBlock(world.getBlockState(currentPos.add(1, 0, 0)).getBlock()))
				{
					ColourfulPortalLocation temp = new ColourfulPortalLocation(currentPos.add(1, 0, 0), world.provider.getDimensionId(), getShiftedCPMetadata(world, currentPos.add(1, 0, 0)));
					if (!visited.contains(temp))
					{
						toVisit.push(temp);
						visited.add(temp);
					}
				}
				if (isCPBlock(world.getBlockState(currentPos.add(-1, 0, 0)).getBlock()))
				{
					ColourfulPortalLocation temp = new ColourfulPortalLocation(currentPos.add(-1, 0, 0), world.provider.getDimensionId(), getShiftedCPMetadata(world, currentPos.add(-1, 0, 0)));
					if (!visited.contains(temp))
					{
						toVisit.push(temp);
						visited.add(temp);
					}
				}
			}
			if ((yDir) || (xDir))
			{
				if (isCPBlock(world.getBlockState(currentPos.add(0, 0, 1)).getBlock()))
				{
					ColourfulPortalLocation temp = new ColourfulPortalLocation(currentPos.add(0, 0, 1), world.provider.getDimensionId(), getShiftedCPMetadata(world, currentPos.add(0, 0, 1)));
					if (!visited.contains(temp))
					{
						toVisit.push(temp);
						visited.add(temp);
					}
				}
				if (isCPBlock(world.getBlockState(currentPos.add(0, 0, -1)).getBlock()))
				{
					ColourfulPortalLocation temp = new ColourfulPortalLocation(currentPos.add(0, 0, -1), world.provider.getDimensionId(), getShiftedCPMetadata(world, currentPos.add(0, 0, -1)));
					if (!visited.contains(temp))
					{
						toVisit.push(temp);
						visited.add(temp);
					}
				}
			}
		}
		return null;
	}

	public static void deletePortal(ColourfulPortalLocation locToDelete)
	{
		if (colourfulPortals.remove(locToDelete)) {
			savePortals();
		}
	}

	public static boolean addPortalToList(ColourfulPortalLocation newLocation)
	{
		if (!colourfulPortals.contains(newLocation))
		{
			colourfulPortals.add(newLocation);





			savePortals();
			return true;
		}
		return false;
	}

	private static void savePortals()
	{
		try
		{
			FileOutputStream fOut = new FileOutputStream(colourfulPortalsMod.saveLocation);
			ObjectOutputStream oOut = new ObjectOutputStream(fOut);
			oOut.writeObject(colourfulPortals);
			oOut.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static class CPLSet
	extends TreeSet<ColourfulPortalsMod.ColourfulPortalLocation>
	{
		public CPLSet()
		{
			super(CPLcomparator);
		}
	}

	public static Comparator<ColourfulPortalLocation> CPLcomparator = new Comparator<ColourfulPortalLocation>()
			{

		@Override
		public int compare(ColourfulPortalsMod.ColourfulPortalLocation first, ColourfulPortalsMod.ColourfulPortalLocation second)
		{
			if (first.portalMetadata != second.portalMetadata) {
				return second.portalMetadata - first.portalMetadata;
			}
			if (first.dimension != second.dimension) {
				return second.dimension - first.dimension;
			}
			if (first.xPos != second.xPos) {
				return second.xPos - first.xPos;
			}
			if (first.yPos != second.yPos) {
				return second.yPos - first.yPos;
			}
			if (first.zPos != second.zPos) {
				return second.zPos - first.zPos;
			}
			return 0;
		}
			};

			public static class ColourfulPortalLocation
			implements Serializable
			{
				int xPos;
				int yPos;
				int zPos;
				int dimension;
				int portalMetadata;

				public ColourfulPortalLocation(BlockPos pos, int dim, int meta)
				{
					this.xPos = pos.getX();
					this.yPos = pos.getY();
					this.zPos = pos.getZ();
					this.dimension = dim;
					this.portalMetadata = meta;
				}

				public boolean equals(Object o)
				{
					if ((o == null) || (!(o instanceof ColourfulPortalLocation))) {
						return false;
					}
					ColourfulPortalLocation other = (ColourfulPortalLocation)o;
					return (this.xPos == other.xPos) && (this.yPos == other.yPos) && (this.zPos == other.zPos) && (this.dimension == other.dimension) && (this.portalMetadata == other.portalMetadata);
				}

				public String toString()
				{
					return "CPL[meta=" + this.portalMetadata + ", x=" + this.xPos + ", y=" + this.yPos + ", z=" + this.zPos + ", dim=" + this.dimension + "]";
				}
			}
}